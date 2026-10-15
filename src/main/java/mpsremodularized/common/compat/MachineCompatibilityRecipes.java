package mpsremodularized.common.compat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import mpsremodularized.common.base.MPSLogger;
import mpsremodularized.common.config.MPSCommonConfig;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.ModList;

/** Optional recipe JSON is decoded only when its provider is installed and enabled. */
public final class MachineCompatibilityRecipes {
    private static volatile Set<ResourceLocation> covered = Set.of();
    private static final String PREFIX = "compat/machines/";
    private record Spec(ResourceLocation id, JsonObject json) {}
    private record Existing(ResourceLocation id, MachineRecipeCollisions.Signature signature) {}
    private MachineCompatibilityRecipes() {}
    public static void clear() { covered = Set.of(); }
    public static boolean covers(ResourceLocation id) { return covered.contains(id); }

    public static void install(RecipeManager manager, HolderLookup.Provider registries, ResourceManager resources,
            CompatibilityPolicy policy) {
        CompatibilityRecipes.withPolicy(policy, () -> { installScoped(manager, registries, resources, policy); return null; });
    }

    private static void installScoped(RecipeManager manager, HolderLookup.Provider registries, ResourceManager resources,
            CompatibilityPolicy policy) {
        var specs = new ArrayList<Spec>();
        resources.listResources("machine_recipes", id -> id.getNamespace().equals(MPSConstants.MOD_ID) && id.getPath().endsWith(".json"))
                .entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    try (var reader = entry.getValue().openAsReader()) {
                        var json = JsonParser.parseReader(reader).getAsJsonObject();
                        String path = entry.getKey().getPath().substring("machine_recipes/".length()).replaceFirst("\\.json$", "");
                        String mod = json.get("mod").getAsString();
                        if (java.util.Arrays.stream(CompatibleMod.values()).noneMatch(provider -> provider.modId.equals(mod))
                                || !path.startsWith(mod + "/")) throw new IllegalArgumentException("Unknown or mismatched provider");
                        specs.add(new Spec(ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, PREFIX + path), json));
                    } catch (Exception exception) {
                        MPSLogger.logger.warn("Invalid MPS machine specification {}: {}", entry.getKey(), exception.toString());
                    }
                });
        var defaults = new LinkedHashMap<String, Map<String, Integer>>();
        var limits = new HashMap<String, Map<String, Integer>>();
        for (var spec : specs) {
            try {
                var result = output(spec.json);
                String mod = spec.json.get("mod").getAsString();
                String itemId = result.get(result.has("id") ? "id" : "item").getAsString();
                if (!ResourceLocation.parse(itemId).getNamespace().equals(MPSConstants.MOD_ID)) throw new IllegalArgumentException("Output must be an MPS item");
                var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(itemId)).filter(i -> i != Items.AIR).orElseThrow();
                String countKey = result.has("amount") ? "amount" : "count";
                int count = result.has(countKey) ? result.get(countKey).getAsInt() : 1;
                if (count < 1 || count > item.getDefaultMaxStackSize()) throw new IllegalArgumentException("Default output exceeds item stack limits");
                defaults.computeIfAbsent(mod, ignored -> new LinkedHashMap<>()).put(spec.id.toString(), count);
                limits.computeIfAbsent(mod, ignored -> new HashMap<>()).put(spec.id.toString(), item.getDefaultMaxStackSize());
            } catch (RuntimeException exception) {
                MPSLogger.logger.warn("Invalid MPS output specification {}: {}", spec.id, exception.toString());
            }
        }
        var outputs = new HashMap<String, Integer>();
        defaults.forEach((mod, values) -> outputs.putAll(MachineRecipeOutputs.load(mod, values, limits.get(mod))));
        var retained = manager.getRecipes().stream().filter(holder -> !injected(holder.id())).collect(Collectors.toCollection(ArrayList::new));
        var occupied = new ArrayList<Existing>();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        for (var holder : retained) {
            // Skip other recipe kinds before invoking their codecs.
            String serializer = BuiltInRegistries.RECIPE_SERIALIZER.getKey(holder.value().getSerializer()).toString();
            if (!defaults.containsKey(serializer.split(":")[0])) continue;
            try {
                var json = Recipe.CODEC.encodeStart(ops, holder.value()).getOrThrow().getAsJsonObject();
                var signature = MachineRecipeCollisions.signature(json, registries);
                if (signature != null) occupied.add(new Existing(holder.id(), signature));
            } catch (RuntimeException exception) {
                MPSLogger.logger.warn("Cannot inspect optional recipe {} for input overlap: {}", holder.id(), exception.toString());
            }
        }
        var enabled = new LinkedHashMap<ResourceLocation, RecipeHolder<?>>();
        var enabledSpecs = new HashMap<ResourceLocation, Spec>();
        for (var spec : specs) {
            try {
                int count = outputs.getOrDefault(spec.id.toString(), 0);
                if (count == 0 || !available(spec.json)) continue;
                var result = output(spec.json);
                result.addProperty(result.has("amount") ? "amount" : "count", count);
                var json = spec.json.getAsJsonObject("recipe");
                var signature = MachineRecipeCollisions.signature(json, registries);
                var collision = occupied.stream().filter(other -> MachineRecipeCollisions.overlap(signature, other.signature)).findFirst();
                if (collision.isPresent()) {
                    MPSLogger.logger.warn("Skipping MPS machine recipe {}: inputs overlap {} (quantities and energy cannot distinguish recipes)", spec.id, collision.get().id);
                    continue;
                }
                Recipe<?> recipe = Recipe.CODEC.parse(ops, json).getOrThrow();
                if (recipe.getIngredients().stream().filter(ingredient -> ingredient != net.minecraft.world.item.crafting.Ingredient.EMPTY)
                        .anyMatch(ingredient -> ingredient.getItems().length == 0))
                    throw new IllegalArgumentException("Unavailable preparation ingredient");
                enabled.put(spec.id, new RecipeHolder<>(spec.id, recipe));
                enabledSpecs.put(spec.id, spec);
                if (signature != null) occupied.add(new Existing(spec.id, signature));
            } catch (RuntimeException exception) {
                MPSLogger.logger.warn("Skipping unsupported MPS machine recipe {}: {}", spec.id, exception.toString());
            }
        }
        // Preparations are usable only when their finishing recipe survived validation/collision checks.
        boolean changed;
        do {
            int before = enabled.size();
            enabledSpecs.values().forEach(spec -> {
                if (spec.json.has("requires_recipe") && !enabled.containsKey(ResourceLocation.parse(spec.json.get("requires_recipe").getAsString()))) enabled.remove(spec.id);
                if (spec.json.has("requires_recipes") && java.util.stream.StreamSupport.stream(spec.json.getAsJsonArray("requires_recipes").spliterator(), false)
                        .anyMatch(value -> !enabled.containsKey(ResourceLocation.parse(value.getAsString())))) enabled.remove(spec.id);
                if (spec.json.has("requires_any_recipe") && java.util.stream.StreamSupport.stream(spec.json.getAsJsonArray("requires_any_recipe").spliterator(), false)
                        .noneMatch(value -> enabled.containsKey(ResourceLocation.parse(value.getAsString())))) enabled.remove(spec.id);
            });
            changed = enabled.size() != before;
        } while (changed);
        var coverage = new java.util.HashSet<ResourceLocation>();
        enabled.keySet().forEach(id -> {
            var spec = enabledSpecs.get(id);
            if (spec.json.has("native_recipes")) java.util.stream.StreamSupport.stream(spec.json.getAsJsonArray("native_recipes").spliterator(), false)
                    .map(value -> ResourceLocation.parse(value.getAsString())).filter(recipeId -> recipeId.getNamespace().equals(MPSConstants.MOD_ID)).forEach(coverage::add);
        });
        // A press needs a mold, and this mold costs Wiring. Preserve a bootstrap craft
        // when server output settings leave pressing as the only Wiring producer.
        var wiringId = ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "component_wiring_standard");
        boolean independentWiring = enabled.keySet().stream().map(enabledSpecs::get).anyMatch(spec ->
                spec.json.has("native_recipes") && spec.json.getAsJsonArray("native_recipes").asList().stream()
                        .anyMatch(value -> value.getAsString().equals(wiringId.toString()))
                        && !spec.json.getAsJsonObject("recipe").get("type").getAsString().equals("immersiveengineering:metal_press"));
        if (!independentWiring) coverage.remove(wiringId);
        covered = Set.copyOf(coverage);
        retained.addAll(enabled.values());
        manager.replaceRecipes(retained);
        CompatibilityRecipes.apply(manager, registries, policy);
        MPSLogger.logger.info("MPS optional machines installed {} recipes/preparations covering {} native recipe IDs", enabled.size(), covered.size());
    }

    public static boolean injected(ResourceLocation id) {
        return id.getNamespace().equals(MPSConstants.MOD_ID) && id.getPath().startsWith(PREFIX);
    }

    private static JsonObject output(JsonObject spec) {
        var recipe = spec.getAsJsonObject("recipe");
        String field = spec.get("output_field").getAsString();
        var result = recipe.get(field);
        return result.isJsonArray() ? result.getAsJsonArray().get(0).getAsJsonObject() : result.getAsJsonObject();
    }

    private static boolean available(JsonObject spec) {
        String modId = spec.get("mod").getAsString();
        if (!ModList.get().isLoaded(modId) || MPSCommonConfig.COMPATIBILITY_SETTINGS.enabledMods().stream().noneMatch(mod -> mod.modId.equals(modId))) return false;
        if (spec.has("required_items")) for (var item : spec.getAsJsonArray("required_items"))
            if (BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(item.getAsString())).filter(i -> i != Items.AIR).isEmpty()) return false;
        if (spec.has("required_tags")) for (var tag : spec.getAsJsonArray("required_tags"))
            if (BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, ResourceLocation.parse(tag.getAsString())))
                    .map(holders -> holders.size() == 0).orElse(true)) return false;
        if (spec.has("electrum")) {
            boolean present = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/electrum")))
                    .map(holders -> holders.size() > 0).orElse(false);
            if (present != spec.get("electrum").getAsBoolean()) return false;
        }
        return true;
    }
}
