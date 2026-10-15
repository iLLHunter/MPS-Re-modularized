package mpsremodularized.common.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.RecipeSerializersRegistry;
import net.minecraft.core.HolderLookup;
import mpsremodularized.common.utils.TagUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.stream.Stream;

public class SmithingUpgradeRecipe implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final SizedIngredient addition;
    final ItemStack result;

    public SmithingUpgradeRecipe(Ingredient template, Ingredient base, SizedIngredient addition, ItemStack result) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    public SizedIngredient getAdditionalIngredient() {
        return addition;
    }

    public Ingredient templateIngredient() {
        return this.template;
    }

    public Ingredient baseIngredient() {
        return this.base;
    }

    public ItemStack additionIngredientStack() {
        ItemStack[] ingredients = this.addition.getItems();

        if (ingredients.length > 0) {
            ItemStack out = ingredients[0];
            for (int i = 1; i < ingredients.length; i++) {
                ItemStack current = ingredients[i];
                if(out.is(current.getItem())) {
                    out.setCount(out.getCount() + current.getCount());
                }
            }
            return out;
        }

        return ItemStack.EMPTY;
    }

    public ItemStack resultStack() {
        return this.result;
    }

    public boolean matches(SmithingRecipeInput input, Level level) {
        return ingredientsMatch(input) && !upgrade(input.base()).isEmpty();
    }

    private boolean ingredientsMatch(SmithingRecipeInput input) {
        return template.test(input.template()) && base.test(input.base()) && addition.test(input.addition());
    }

    /**
     * Preserve components and installed items; refuse upgrades that cannot hold every module.
     * @param input
     * @param registries
     * @return
     */
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        return ingredientsMatch(input) ? upgrade(input.base()) : ItemStack.EMPTY;
    }

    private ItemStack upgrade(ItemStack source) {
        ItemStack itemstack = source.transmuteCopy(this.result.getItem(), this.result.getCount());
        itemstack.applyComponents(this.result.getComponentsPatch());
        if (MPSCapabilities.getModularItemOrModeChangingCapability(source) == null) return itemstack;

        var contents = source.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        // Rebuild from complete serialized contents: copied out-of-range cells
        // must not survive alongside relocated copies in a smaller destination.
        itemstack.remove(DataComponents.CONTAINER);
        var destination = MPSCapabilities.getModularItemOrModeChangingCapability(itemstack);
        if (destination == null) return ItemStack.EMPTY;
        for (int slot = 0; slot < Math.min(contents.getSlots(), destination.getSlots()); slot++) {
            ItemStack module = contents.getStackInSlot(slot);
            if (!module.isEmpty()) destination.updateModuleInSlot(slot, module.copy());
        }
        int selected = TagUtils.getModularItemIntOrDefault(source, IModeChangingItem.TAG_MODE, -1);
        for (int originalSlot = destination.getSlots(); originalSlot < contents.getSlots(); originalSlot++) {
            ItemStack module = contents.getStackInSlot(originalSlot);
            if (module.isEmpty()) continue;
            boolean moved = false;
            for (int slot = 0; slot < destination.getSlots(); slot++) {
                if (destination.isModuleValidForPlacement(slot, module) && destination.insertItem(slot, module, false).isEmpty()) {
                    if (originalSlot == selected && destination instanceof IModeChangingItem modes) modes.setActiveMode(slot);
                    moved = true;
                    break;
                }
            }
            if (!moved) return ItemStack.EMPTY;
        }
        return itemstack;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    public boolean isTemplateIngredient(ItemStack stack) {
        return this.template.test(stack);
    }

    public boolean isBaseIngredient(ItemStack stack) {
        return this.base.test(stack);
    }

    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }

    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersRegistry.SMITHING_UPGRADE_SERIALIZER.get();
    }

    public boolean isIncomplete() {
        return Stream.of(this.template, this.base).anyMatch(Ingredient::hasNoItems) || addition.count() == 0;
    }

    public static class SmithingUpgradeSerializer implements RecipeSerializer<SmithingUpgradeRecipe> {
        private static final MapCodec<SmithingUpgradeRecipe> CODEC =
            RecordCodecBuilder.mapCodec((instance) -> instance.group(
                    Ingredient.CODEC.fieldOf("template").forGetter((recipe) -> recipe.template),
                    Ingredient.CODEC.fieldOf("base").forGetter((recipe) -> recipe.base),
                    SizedIngredient.NESTED_CODEC.fieldOf("addition").forGetter((recipe) -> recipe.addition),
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter((recipe) -> recipe.result)
                ).apply(instance, SmithingUpgradeRecipe::new)
            );

        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingUpgradeRecipe> STREAM_CODEC =
            StreamCodec.of(SmithingUpgradeSerializer::toNetwork, SmithingUpgradeSerializer::fromNetwork);

        public SmithingUpgradeSerializer() {
        }

        public MapCodec<SmithingUpgradeRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, SmithingUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static SmithingUpgradeRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient template = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient base = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            SizedIngredient addition = SizedIngredient.STREAM_CODEC.decode(buffer);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
            return new SmithingUpgradeRecipe(template, base, addition, itemstack);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, SmithingUpgradeRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.template);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.base);
            SizedIngredient.STREAM_CODEC.encode(buffer, recipe.addition);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }
}
