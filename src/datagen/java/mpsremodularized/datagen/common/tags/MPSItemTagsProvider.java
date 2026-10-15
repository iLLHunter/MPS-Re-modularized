package mpsremodularized.datagen.common.tags;

import mpsremodularized.common.compat.CompatibleComponent;
import mpsremodularized.common.compat.CompatibleCraftingRecipe;
import mpsremodularized.datagen.common.recipes.MPSRecipeGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MPSItemTagsProvider extends ItemTagsProvider {
    public MPSItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags) {
        super(pOutput, pLookupProvider, pBlockTags);
    }

    public MPSItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, modId, existingFileHelper);
    }

    public MPSItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Item>> pParentProvider, CompletableFuture<TagLookup<Block>> pBlockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pParentProvider, pBlockTags, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        // Direct enchantable-slot tags avoid vanilla's durability-tag inheritance.
        for (var entry : mpsremodularized.common.registration.MPSItems.MPS_ITEMS.getEntries()) {
            if (entry.get() instanceof mpsremodularized.common.item.electric.armor.AbstractElectricArmor armor) {
                String slot = switch (armor.getEquipmentSlot()) {
                    case HEAD -> "head_armor";
                    case CHEST -> "chest_armor";
                    case LEGS -> "leg_armor";
                    case FEET -> "foot_armor";
                    default -> throw new IllegalStateException("Unexpected MPS armor slot");
                };
                tag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                        net.minecraft.resources.ResourceLocation.withDefaultNamespace("enchantable/" + slot))).add(entry.get());
                // Equipment-wide enchantments must also work without inheriting durability eligibility.
                tag(net.minecraft.tags.ItemTags.EQUIPPABLE_ENCHANTABLE).add(entry.get());
                tag(net.minecraft.tags.ItemTags.VANISHING_ENCHANTABLE).add(entry.get());
            }
        }
        for (var module : mpsremodularized.common.compat.IntermodModules.ENTRIES) {
            tag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mpsremodularized", "native_modules/" + module.id())))
                    .addOptional(net.minecraft.resources.ResourceLocation.parse(module.donor()));
        }
        tag(MPSRecipeGenerator.ELECTRUM_INGOTS);
        for (CompatibleComponent component : CompatibleComponent.values()) {
            var discovery = tag(component.discoveryTag()).add(component.nativeItem.get());
            component.alternatives.forEach(alternative -> discovery.addOptional(alternative.itemId()));
        }
        for (var recipe : CompatibleCraftingRecipe.values()) {
            var discovery = tag(recipe.discoveryTag());
            recipe.inputs.forEach(input -> discovery.addOptional(input.itemId()));
        }
    }
}
