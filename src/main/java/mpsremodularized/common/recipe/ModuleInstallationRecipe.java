package mpsremodularized.common.recipe;

import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.RecipeSerializersRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Installs one item from each module ingredient only when the entire grid fits. */
public class ModuleInstallationRecipe extends CustomRecipe {
    public ModuleInstallationRecipe(CraftingBookCategory category) { super(category); }

    @Override
    public boolean matches(CraftingInput input, Level level) { return !installAll(input).isEmpty(); }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) { return installAll(input); }

    private ItemStack installAll(CraftingInput input) {
        ItemStack source = ItemStack.EMPTY;
        List<ItemStack> modules = new ArrayList<>();
        for (int cell = 0; cell < input.size(); cell++) {
            ItemStack ingredient = input.getItem(cell);
            if (ingredient.isEmpty()) continue;
            if (MPSCapabilities.getModularItemOrModeChangingCapability(ingredient) != null) {
                if (!source.isEmpty()) return ItemStack.EMPTY;
                source = ingredient;
            } else if (ingredient.getCapability(MPSCapabilities.Module.POWER_MODULE) != null) {
                modules.add(ingredient.copyWithCount(1));
            } else return ItemStack.EMPTY;
        }
        if (source.isEmpty() || modules.isEmpty()) return ItemStack.EMPTY;

        // Mutate only a preview copy, so failed or competing inputs spend nothing.
        ItemStack result = source.copyWithCount(1);
        var host = MPSCapabilities.getModularItemOrModeChangingCapability(result);
        for (ItemStack module : modules) {
            if (host.isModuleInstalled(module)) return ItemStack.EMPTY;
            boolean installed = false;
            for (int slot = 0; slot < host.getSlots(); slot++) {
                if (host.isModuleValidForPlacement(slot, module) && host.insertItem(slot, module, false).isEmpty()) {
                    installed = true;
                    break;
                }
            }
            if (!installed) return ItemStack.EMPTY;
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<?> getSerializer() { return RecipeSerializersRegistry.ATTACHMENT.get(); }
}
