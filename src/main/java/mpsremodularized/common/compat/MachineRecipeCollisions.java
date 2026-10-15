package mpsremodularized.common.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/** Conservative input overlap guard. Counts, output, duration and energy never separate recipes. */
public final class MachineRecipeCollisions {
    public record Signature(String type, String selector, List<Set<Item>> inputs) {}
    private MachineRecipeCollisions() {}

    public static Signature signature(JsonObject json, HolderLookup.Provider registries) {
        String type = json.get("type").getAsString();
        var inputs = new ArrayList<Set<Item>>();
        String selector = "";
        switch (type) {
            case "modern_industrialization:assembler" -> collect(json.get("item_inputs"), registries, inputs);
            case "oritech:assembler", "powah:energizing" -> collect(json.get("ingredients"), registries, inputs);
            case "pneumaticcraft:pressure_chamber" -> collect(json.get("inputs"), registries, inputs);
            case "pneumaticcraft:assembly_laser" -> {
                collect(json.get("input"), registries, inputs);
                selector = json.has("program") ? json.get("program").getAsString() : "laser";
            }
            case "industrialforegoing:dissolution_chamber" -> {
                collect(json.get("input"), registries, inputs);
                selector = fluids(json.get("inputFluid"));
            }
            case "immersiveengineering:blueprint" -> {
                collect(json.get("inputs"), registries, inputs);
                selector = json.get("category").getAsString();
            }
            case "immersiveengineering:metal_press" -> {
                collect(json.get("input"), registries, inputs);
                selector = json.get("mold").getAsString();
            }
            case "mekanism:combining" -> {
                // Main and extra slots are distinct: swapping them is not a collision.
                collect(json.get("main_input"), registries, inputs);
                collect(json.get("extra_input"), registries, inputs);
            }
            case "enderio:alloy_smelting" -> collect(json.get("inputs"), registries, inputs);
            default -> { return null; }
        }
        // Repeated copies are amounts, not a distinct input identity.
        return new Signature(type, selector, inputs.stream().distinct().toList());
    }

    private static String fluids(JsonElement value) {
        if (value == null) return "";
        if (value.isJsonObject()) {
            var object = value.getAsJsonObject();
            if (object.has("fluid")) return "fluid:" + object.get("fluid").getAsString();
            if (object.has("tag")) return "tag:" + object.get("tag").getAsString();
        }
        return value.toString();
    }

    private static void collect(JsonElement value, HolderLookup.Provider registries, List<Set<Item>> inputs) {
        if (value == null) return;
        if (value.isJsonArray()) {
            value.getAsJsonArray().forEach(element -> collect(element, registries, inputs));
            return;
        }
        var object = value.getAsJsonObject();
        if (object.has("basePredicate")) { collect(object.get("basePredicate"), registries, inputs); return; }
        if (object.has("ingredient")) { collect(object.get("ingredient"), registries, inputs); return; }
        var ingredient = Ingredient.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), object).getOrThrow();
        var items = new HashSet<Item>();
        for (var stack : ingredient.getItems()) items.add(stack.getItem());
        if (items.isEmpty()) throw new IllegalArgumentException("Unavailable input ingredient: " + object);
        inputs.add(Set.copyOf(items));
    }

    public static boolean overlap(Signature a, Signature b) {
        if (a == null || b == null || !a.type.equals(b.type) || !a.selector.equals(b.selector)
                || a.inputs.isEmpty() || b.inputs.isEmpty()) return false;
        if (a.type.equals("mekanism:combining")) {
            return a.inputs.size() == b.inputs.size() && java.util.stream.IntStream.range(0, a.inputs.size())
                    .allMatch(i -> intersects(a.inputs.get(i), b.inputs.get(i)));
        }
        return subset(a.inputs, b.inputs) && subset(b.inputs, a.inputs);
    }

    private static boolean subset(List<Set<Item>> a, List<Set<Item>> b) {
        return a.stream().allMatch(input -> b.stream().anyMatch(other -> intersects(input, other)));
    }

    private static boolean intersects(Set<Item> a, Set<Item> b) { return a.stream().anyMatch(b::contains); }
}
