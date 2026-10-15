package mpsremodularized.common.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import mpsremodularized.common.config.MPSCommonConfig;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Registry and component codecs keep integrations optional on both physical sides. */
public final class WrenchCompatibility {
    private static final TagKey<Item> WRENCHES = TagKey.create(Registries.ITEM, ResourceLocation.parse("c:tools/wrench"));
    private static final Map<String, List<String>> NATIVE_WRENCHES = Map.of(
            "create", List.of("wrench"),
            "ae2", List.of("certus_quartz_wrench", "nether_quartz_wrench"),
            "mekanism", List.of("configurator"),
            "pneumaticcraft", List.of("pneumatic_wrench"),
            "immersiveengineering", List.of("hammer"),
            "enderio", List.of("yeta_wrench"),
            "modern_industrialization", List.of("wrench"),
            "oritech", List.of("wrench"),
            "powah", List.of("wrench"));

    private WrenchCompatibility() { }

    public static ItemStack findWrench(BlockState state) {
        return findWrench(state, false);
    }

    public static ItemStack findWrench(BlockState state, boolean sneaking) {
        String namespace = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace();
        if (namespace.equals("minecraft") || namespace.equals("mpsremodularized")) return ItemStack.EMPTY;
        for (CompatibleMod mod : CompatibleMod.values()) {
            if (mod.modId.equals(namespace) && !MPSCommonConfig.COMPATIBILITY_SETTINGS.enabledMods().contains(mod)) {
                return ItemStack.EMPTY;
            }
        }
        for (String path : NATIVE_WRENCHES.getOrDefault(namespace, List.of())) {
            var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(namespace, path));
            if (item.isPresent()) return prepare(new ItemStack(item.get()), namespace, sneaking);
        }
        // Additional mods can participate through the shared tag without MPS knowing their classes.
        return BuiltInRegistries.ITEM.getTag(WRENCHES).stream().flatMap(tag -> tag.stream())
                .filter(holder -> BuiltInRegistries.ITEM.getKey(holder.value()).getNamespace().equals(namespace))
                .sorted(java.util.Comparator.comparing(holder -> BuiltInRegistries.ITEM.getKey(holder.value()).toString()))
                .map(holder -> prepare(new ItemStack(holder.value()), namespace, sneaking)).filter(stack -> !stack.isEmpty())
                .findFirst().orElse(ItemStack.EMPTY);
    }

    private static ItemStack prepare(ItemStack wrench, String namespace, boolean sneaking) {
        if (namespace.equals("mekanism")) {
            // Rotation and dismantling are different native modes; never select inventory-empty mode.
            if (!setComponent(wrench, "mekanism:configurator_mode", new JsonPrimitive(sneaking ? "wrench" : "rotate"))) return ItemStack.EMPTY;
        } else if (namespace.equals("powah")) {
            if (!setComponent(wrench, "powah:wrench_mode", new JsonPrimitive("rotate"))) return ItemStack.EMPTY;
        } else if (namespace.equals("pneumaticcraft")) {
            // One FE-funded transaction supplies a temporary pressure reserve; no air item is awarded.
            if (!setComponent(wrench, "pneumaticcraft:air", new JsonPrimitive(30000))) return ItemStack.EMPTY;
        }
        return wrench;
    }

    private static <T> boolean setComponent(ItemStack stack, DataComponentType<T> type, JsonElement value) {
        if (type.codec() == null) return false;
        var decoded = type.codec().parse(JsonOps.INSTANCE, value).result();
        decoded.ifPresent(component -> stack.set(type, component));
        return decoded.isPresent();
    }

    private static boolean setComponent(ItemStack stack, String id, JsonElement value) {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(ResourceLocation.parse(id))
                .map(type -> setComponent(stack, type, value)).orElse(false);
    }
}
