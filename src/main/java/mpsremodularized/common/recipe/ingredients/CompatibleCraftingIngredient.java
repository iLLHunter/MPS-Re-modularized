package mpsremodularized.common.recipe.ingredients;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import mpsremodularized.common.compat.CompatibleCraftingRecipe;
import mpsremodularized.common.compat.CompatibilityRecipes;
import mpsremodularized.common.registration.MPSIngredientTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/** Snapshot the enabled server material choices; recipe shape enforces quantity. */
public record CompatibleCraftingIngredient(CompatibleCraftingRecipe recipe, List<Item> items) implements ICustomIngredient {
    public static final MapCodec<CompatibleCraftingIngredient> CODEC = Codec.STRING
            .xmap(CompatibleCraftingRecipe::byKey, recipe -> recipe.key).fieldOf("recipe")
            .xmap(CompatibleCraftingIngredient::new, CompatibleCraftingIngredient::recipe);

    public CompatibleCraftingIngredient { items = List.copyOf(items); }

    public CompatibleCraftingIngredient(CompatibleCraftingRecipe recipe) { this(recipe, CompatibilityRecipes.policy().inputs(recipe)); }

    @Override public boolean test(ItemStack stack) { return !stack.isEmpty() && items.contains(stack.getItem()); }
    @Override public Stream<ItemStack> getItems() { return items.stream().map(ItemStack::new); }
    @Override public boolean isSimple() { return true; }
    @Override public IngredientType<?> getType() { return MPSIngredientTypes.COMPATIBLE_CRAFTING.get(); }
}
