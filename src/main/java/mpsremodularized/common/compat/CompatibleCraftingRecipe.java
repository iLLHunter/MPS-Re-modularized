package mpsremodularized.common.compat;

import java.util.List;
import java.util.function.Supplier;
import mpsremodularized.common.registration.MPSItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import mpsremodularized.common.constants.MPSConstants;

/** Foreign materials require their full assembly cost before becoming a component. */
public enum CompatibleCraftingRecipe {
    IRON_LARGE_PLATES("iron_large_plates", MPSItems.IRON_PLATING_MODULE,
            CompatibleComponent.from(CompatibleMod.MODERN_INDUSTRIALIZATION, "iron_large_plate")),
    DIAMOND_LARGE_PLATES("diamond_large_plates", MPSItems.DIAMOND_PLATING_MODULE,
            CompatibleComponent.from(CompatibleMod.MODERN_INDUSTRIALIZATION, "diamond_large_plate")),
    RUBBER_HOSE("rubber_hose", MPSItems.RUBBER_HOSE,
            CompatibleComponent.from(CompatibleMod.INDUSTRIAL_FOREGOING, "dryrubber"),
            CompatibleComponent.from(CompatibleMod.MODERN_INDUSTRIALIZATION, "rubber_sheet"));

    public final String key;
    public final Supplier<? extends Item> output;
    public final List<CompatibleComponent.Alternative> inputs;

    CompatibleCraftingRecipe(String key, Supplier<? extends Item> output, CompatibleComponent.Alternative... inputs) {
        this.key = key;
        this.output = output;
        this.inputs = List.of(inputs);
    }

    public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "compat/crafting/" + key);
    }

    public ResourceLocation outputId() { return BuiltInRegistries.ITEM.getKey(output.get()); }

    public TagKey<Item> discoveryTag() {
        return TagKey.create(Registries.ITEM, id().withPrefix("recipe_inputs/"));
    }

    public static CompatibleCraftingRecipe byKey(String key) {
        for (var recipe : values()) if (recipe.key.equals(key)) return recipe;
        throw new IllegalArgumentException("Unknown compatible crafting recipe: " + key);
    }
}
