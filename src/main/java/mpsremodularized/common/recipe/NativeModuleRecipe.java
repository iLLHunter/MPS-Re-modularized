package mpsremodularized.common.recipe;

import com.mojang.serialization.MapCodec;
import mpsremodularized.common.compat.NativeModuleItem;
import mpsremodularized.common.registration.RecipeSerializersRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** The usual approved shaped pattern, plus a lossless copy of its one completed donor. */
public final class NativeModuleRecipe extends ShapedRecipe {
    public NativeModuleRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.getResultItem(null), recipe.showNotification());
    }
    @Override public boolean matches(CraftingInput input, Level level) {
        if (!(getResultItem(null).getItem() instanceof NativeModuleItem item) || !item.available() || !super.matches(input, level)) return false;
        for (int i = input.size() - 1; i >= 0; i--) {
            var source = input.getItem(i);
            if (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(source.getItem()).equals(item.donor)
                    && !item.acceptsSource(source)) return false;
        }
        return true;
    }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        ItemStack result = super.assemble(input, provider);
        if (!(result.getItem() instanceof NativeModuleItem item)) return ItemStack.EMPTY;
        // The original donor remains in the lower row; added top-row anvils are extra materials.
        for (int i = input.size() - 1; i >= 0; i--) {
            ItemStack donor = input.getItem(i);
            if (!donor.isEmpty() && net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(donor.getItem()).equals(item.donor)) {
                if (!item.acceptsSource(donor)) return ItemStack.EMPTY;
                item.store(result, donor.copyWithCount(1), provider);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }
    @Override public RecipeSerializer<?> getSerializer() { return RecipeSerializersRegistry.NATIVE_MODULE.get(); }

    public static final class Serializer implements RecipeSerializer<NativeModuleRecipe> {
        private static final MapCodec<NativeModuleRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(NativeModuleRecipe::new, r -> r);
        private static final StreamCodec<RegistryFriendlyByteBuf, NativeModuleRecipe> STREAM = ShapedRecipe.Serializer.STREAM_CODEC.map(NativeModuleRecipe::new, r -> r);
        @Override public MapCodec<NativeModuleRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, NativeModuleRecipe> streamCodec() { return STREAM; }
    }
}
