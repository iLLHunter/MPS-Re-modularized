package mpsremodularized.datagen.common.recipes;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import mpsremodularized.common.base.MPSLogger;
import mpsremodularized.common.registration.MPSItems;
import mpsremodularized.common.registration.MPSDataComponents;
import mpsremodularized.common.compat.CompatibleComponent;
import mpsremodularized.common.compat.CompatibleCraftingRecipe;
import mpsremodularized.common.compat.CompatibilityRecipes;
import mpsremodularized.common.recipe.ingredients.CompatibleCraftingIngredient;
import mpsremodularized.common.recipe.ingredients.ComponentIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import mpsremodularized.datagen.common.recipes.ShapedEnchantmentRecipeBuilder;
import mpsremodularized.datagen.common.recipes.SmithingUpgradeRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;


public class MPSRecipeGenerator extends RecipeProvider {
    public static final TagKey<Item> ELECTRUM_INGOTS = TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/electrum"));
    CompletableFuture<HolderLookup.Provider> registries;

    public MPSRecipeGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
        this.registries = registries;
    }

    @Override
    protected void buildRecipes(@Nonnull RecipeOutput output) {
        buildFirstPassModules(output);
        buildComponentRecipes(output);
        buildCompatibilityConversions(output);
        buildCompatibleCraftingRecipes(output);
        HolderLookup.Provider provider = null;
        try {
            provider = registries.get();
        } catch(Exception e) {
            MPSLogger.logException("failed to get HolderLookup.Provider during MPS recipe datagen: ", e);
        }

        // Tinker Table -------------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.TINKER_TABLE_ITEM.get())
            .pattern("IBI")
            .pattern("BCB")
            .pattern("GBG")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('B', Tags.Items.DYES_BLUE)
            .define('C', Items.CRAFTING_TABLE)
            .define('G', Tags.Items.INGOTS_GOLD)
            .unlockedBy(getHasName(Items.CRAFTING_TABLE), has(Items.CRAFTING_TABLE))
            .save(output);

        // Power Fist ---------------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.POWER_FIST_1.get())
            .pattern(" W ")
            .pattern("WIC")
            .pattern(" IW")
            .define('I', Tags.Items.INGOTS_COPPER)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.WIRING_STANDARD.get()), has(MPSItems.WIRING_STANDARD.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_FIST_1.get()),
                SizedIngredient.of(Items.IRON_INGOT, 4),
                RecipeCategory.TOOLS,
                MPSItems.POWER_FIST_2.get())
            .unlockedBy(getHasName(MPSItems.TIER_2_SMITHING_UPGRADE.get()), has(MPSItems.TIER_2_SMITHING_UPGRADE.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_FIST_2.get()),
                SizedIngredient.of(Items.DIAMOND, 4),
                RecipeCategory.TOOLS,
                MPSItems.POWER_FIST_3.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_FIST_3.get()),
                SizedIngredient.of(Items.NETHERITE_INGOT, 4),
                RecipeCategory.TOOLS,
                MPSItems.POWER_FIST_4.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);

//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.POWER_FIST_2.get())
//            .pattern(" W ")
//            .pattern("WXC")
//            .pattern(" IW")
//            .define('X', MPSItems.POWER_FIST_1.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_STANDARD.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_2.get())
//            .unlockedBy(getHasName(MPSItems.POWER_FIST_1.get()), has(MPSItems.POWER_FIST_1.get()))
//            .save(output);
//
//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.POWER_FIST_3.get())
//            .pattern(" W ")
//            .pattern("WXC")
//            .pattern(" IW")
//            .define('X', MPSItems.POWER_FIST_2.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_3.get())
//            .unlockedBy(getHasName(MPSItems.POWER_FIST_2.get()), has(MPSItems.POWER_FIST_2.get()))
//            .save(output);
//
//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.POWER_FIST_4.get())
//            .pattern(" W ")
//            .pattern("WXC")
//            .pattern(" IW")
//            .define('X', MPSItems.POWER_FIST_3.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_4.get())
//            .unlockedBy(getHasName(MPSItems.POWER_FIST_3.get()), has(MPSItems.POWER_FIST_3.get()))
//            .save(output);

        // Armor ==========================================================================================================
        // Helmet
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_HELMET_1.get())
            .pattern("III")
            .pattern("WCW")
            .define('I', Tags.Items.INGOTS_COPPER)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.WIRING_STANDARD.get()), has(MPSItems.WIRING_STANDARD.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_HELMET_1.get()),
                SizedIngredient.of(Items.IRON_INGOT, 4),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_HELMET_2.get())
            .unlockedBy(getHasName(MPSItems.TIER_2_SMITHING_UPGRADE.get()), has(MPSItems.TIER_2_SMITHING_UPGRADE.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_HELMET_2.get()),
                SizedIngredient.of(Items.DIAMOND, 4),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_HELMET_3.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_HELMET_3.get()),
                SizedIngredient.of(Items.NETHERITE_INGOT, 4),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_HELMET_4.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);


//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_HELMET_2.get())
//            .pattern("III")
//            .pattern("WCW")
//            .pattern(" X ")
//            .define('X', MPSItems.POWER_ARMOR_HELMET_1.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_STANDARD.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_2.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_HELMET_1.get()), has(MPSItems.POWER_ARMOR_HELMET_1.get()))
//            .save(output);
//
//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_HELMET_3.get())
//            .pattern("III")
//            .pattern("WCW")
//            .pattern(" X ")
//            .define('X', MPSItems.POWER_ARMOR_HELMET_2.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_3.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_HELMET_2.get()), has(MPSItems.POWER_ARMOR_HELMET_2.get()))
//            .save(output);
//
//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_HELMET_4.get())
//            .pattern("III")
//            .pattern("WCW")
//            .pattern(" X ")
//            .define('X', MPSItems.POWER_ARMOR_HELMET_3.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_4.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_HELMET_3.get()), has(MPSItems.POWER_ARMOR_HELMET_3.get()))
//            .save(output);

        // ChestPlate
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_CHESTPLATE_1.get())
            .pattern("ICI")
            .pattern("WIW")
            .pattern("III")
            .define('I', Tags.Items.INGOTS_COPPER)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.WIRING_STANDARD.get()), has(MPSItems.WIRING_STANDARD.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_CHESTPLATE_1.get()),
                SizedIngredient.of(Items.IRON_INGOT, 5),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_CHESTPLATE_2.get())
            .unlockedBy(getHasName(MPSItems.TIER_2_SMITHING_UPGRADE.get()), has(MPSItems.TIER_2_SMITHING_UPGRADE.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_CHESTPLATE_2.get()),
                SizedIngredient.of(Items.DIAMOND, 5),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_CHESTPLATE_3.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_CHESTPLATE_3.get()),
                SizedIngredient.of(Items.NETHERITE_INGOT, 5),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_CHESTPLATE_4.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);


//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_CHESTPLATE_2.get())
//            .pattern("ICI")
//            .pattern("WXW")
//            .pattern("III")
//            .define('X', MPSItems.POWER_ARMOR_CHESTPLATE_1.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_STANDARD.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_2.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_CHESTPLATE_1.get()), has(MPSItems.POWER_ARMOR_CHESTPLATE_1.get()))
//            .save(output);
//
//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_CHESTPLATE_3.get())
//            .pattern("ICI")
//            .pattern("WXW")
//            .pattern("III")
//            .define('X', MPSItems.POWER_ARMOR_CHESTPLATE_2.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_3.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_CHESTPLATE_2.get()), has(MPSItems.POWER_ARMOR_CHESTPLATE_2.get()))
//            .save(output);
//
//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_CHESTPLATE_4.get())
//            .pattern("ICI")
//            .pattern("WXW")
//            .pattern("III")
//            .define('X', MPSItems.POWER_ARMOR_CHESTPLATE_3.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_4.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_CHESTPLATE_3.get()), has(MPSItems.POWER_ARMOR_CHESTPLATE_3.get()))
//            .save(output);

        // Leggings
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_LEGGINGS_1.get())
            .pattern("III")
            .pattern("WCW")
            .pattern("I I")
            .define('I', Tags.Items.INGOTS_COPPER)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.WIRING_STANDARD.get()), has(MPSItems.WIRING_STANDARD.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_LEGGINGS_1.get()),
                SizedIngredient.of(Items.IRON_INGOT, 5),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_LEGGINGS_2.get())
            .unlockedBy(getHasName(MPSItems.TIER_2_SMITHING_UPGRADE.get()), has(MPSItems.TIER_2_SMITHING_UPGRADE.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_LEGGINGS_2.get()),
                SizedIngredient.of(Items.DIAMOND, 5),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_LEGGINGS_3.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_LEGGINGS_3.get()),
                SizedIngredient.of(Items.NETHERITE_INGOT, 5),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_LEGGINGS_4.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);

//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_LEGGINGS_2.get())
//            .pattern("III")
//            .pattern("WCW")
//            .pattern("IXI")
//            .define('X', MPSItems.POWER_ARMOR_LEGGINGS_1.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_STANDARD.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_2.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_LEGGINGS_1.get()), has(MPSItems.POWER_ARMOR_LEGGINGS_1.get()))
//            .save(output);
//
//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_LEGGINGS_3.get())
//            .pattern("III")
//            .pattern("WCW")
//            .pattern("IXI")
//            .define('X', MPSItems.POWER_ARMOR_LEGGINGS_2.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_3.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_LEGGINGS_2.get()), has(MPSItems.POWER_ARMOR_LEGGINGS_2.get()))
//            .save(output);
//
//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_LEGGINGS_4.get())
//            .pattern("III")
//            .pattern("WCW")
//            .pattern("IXI")
//            .define('X', MPSItems.POWER_ARMOR_LEGGINGS_3.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_4.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_LEGGINGS_3.get()), has(MPSItems.POWER_ARMOR_LEGGINGS_3.get()))
//            .save(output);

        // Boots
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_BOOTS_1.get())
            .pattern("ICI")
            .pattern("W W")
            .define('I', Tags.Items.INGOTS_COPPER)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.WIRING_STANDARD.get()), has(MPSItems.WIRING_STANDARD.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_BOOTS_1.get()),
                SizedIngredient.of(Items.IRON_INGOT, 2),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_BOOTS_2.get())
            .unlockedBy(getHasName(MPSItems.TIER_2_SMITHING_UPGRADE.get()), has(MPSItems.TIER_2_SMITHING_UPGRADE.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_BOOTS_2.get()),
                SizedIngredient.of(Items.DIAMOND, 2),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_BOOTS_3.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.POWER_ARMOR_BOOTS_3.get()),
                SizedIngredient.of(Items.NETHERITE_INGOT, 2),
                RecipeCategory.TOOLS,
                MPSItems.POWER_ARMOR_BOOTS_4.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);

//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_BOOTS_2.get())
//            .pattern("ICI")
//            .pattern("WXW")
//            .define('X', MPSItems.POWER_ARMOR_BOOTS_1.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_STANDARD.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_2.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_BOOTS_1.get()), has(MPSItems.POWER_ARMOR_BOOTS_1.get()))
//            .save(output);
//
//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_BOOTS_3.get())
//            .pattern("ICI")
//            .pattern("WXW")
//            .define('X', MPSItems.POWER_ARMOR_BOOTS_2.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_3.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_BOOTS_2.get()), has(MPSItems.POWER_ARMOR_BOOTS_2.get()))
//            .save(output);
//
//        ShapedModularItemUpgradeRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.POWER_ARMOR_BOOTS_4.get())
//            .pattern("ICI")
//            .pattern("WXW")
//            .define('X', MPSItems.POWER_ARMOR_BOOTS_3.get())
//            .define('I', Tags.Items.INGOTS_IRON)
//            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
//            .define('C', MPSItems.CONTROL_CIRCUIT_4.get())
//            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_BOOTS_3.get()), has(MPSItems.POWER_ARMOR_BOOTS_3.get()))
//            .save(output);

        // Modules ========================================================================================================
        // Armor --------------------------------------------------------------------------------------------
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, MPSItems.IRON_PLATING_MODULE.get())
            .requires(ComponentIngredient.of(MPSItems.PLATING_IRON.get()), 3)
            .unlockedBy(getHasName(MPSItems.PLATING_IRON.get()), has(CompatibleComponent.IRON_PLATING.discoveryTag()))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, MPSItems.DIAMOND_PLATING_MODULE.get())
            .requires(MPSItems.PLATING_DIAMOND.get(), 3)
            .unlockedBy(getHasName(MPSItems.PLATING_DIAMOND.get()), has(CompatibleComponent.DIAMOND_PLATING.discoveryTag()))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, MPSItems.NETHERITE_PLATING_MODULE.get())
            .requires(MPSItems.PLATING_NETHERITE.get(), 3)
            .unlockedBy(getHasName(MPSItems.PLATING_NETHERITE.get()), has(MPSItems.PLATING_NETHERITE.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.ENERGY_SHIELD_MODULE.get())
            .pattern("WEW")
            .pattern("WCW")
            .pattern("WEW")
            .define('E', MPSItems.FORCE_FIELD_EMITTER.get())
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
            .unlockedBy(getHasName(MPSItems.FORCE_FIELD_EMITTER.get()), has(MPSItems.FORCE_FIELD_EMITTER.get()))
            .save(output);

        // Cosmetic -----------------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.TRANSPARENT_ARMOR_MODULE.get())
            .pattern("WW")
            .pattern("LC")
            .pattern("WW")
            .define('L', MPSItems.HOLOGRAM_EMITTER.get())
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
            .unlockedBy(getHasName(MPSItems.FORCE_FIELD_EMITTER.get()), has(MPSItems.FORCE_FIELD_EMITTER.get()))
            .save(output);


        // Energy Generation --------------------------------------------------------------------------
        // Thermal
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.THERMAL_GENERATOR_MODULE_1.get())
            .pattern("IOI")
            .pattern("SWO")
            .pattern("IOI")
            .define('I', ComponentIngredient.of(MPSItems.PLATING_IRON.get()))
            .define('O', Tags.Items.INGOTS_COPPER)
            .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
            .define('W', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.POWER_ARMOR_CHESTPLATE_1.get()), has(MPSItems.POWER_ARMOR_CHESTPLATE_1.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.THERMAL_GENERATOR_MODULE_1.get()),
                SizedIngredient.of(Items.GOLD_INGOT, 4),
                RecipeCategory.TOOLS,
                MPSItems.THERMAL_GENERATOR_MODULE_2.get())
            .unlockedBy(getHasName(MPSItems.TIER_2_SMITHING_UPGRADE.get()), has(MPSItems.TIER_2_SMITHING_UPGRADE.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.THERMAL_GENERATOR_MODULE_2.get()),
                SizedIngredient.of(Tags.Items.GEMS_DIAMOND, 4),
                RecipeCategory.TOOLS,
                MPSItems.THERMAL_GENERATOR_MODULE_3.get())
            .unlockedBy(getHasName(MPSItems.TIER_3_SMITHING_UPGRADE.get()), has(MPSItems.TIER_3_SMITHING_UPGRADE.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.THERMAL_GENERATOR_MODULE_3.get()),
                SizedIngredient.of(Tags.Items.INGOTS_NETHERITE, 4),
                RecipeCategory.TOOLS,
                MPSItems.THERMAL_GENERATOR_MODULE_4.get())
            .unlockedBy(getHasName(MPSItems.TIER_4_SMITHING_UPGRADE.get()), has(MPSItems.TIER_4_SMITHING_UPGRADE.get()))
            .save(output);

        // Solar ---------------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.SOLAR_GENERATOR_MODULE_1.get())
            .pattern("SIS")
            .pattern("WCI")
            .pattern("SIS")
            .define('I', Tags.Items.INGOTS_COPPER)
            .define('S', ComponentIngredient.of(MPSItems.SOLAR_PANEL.get()))
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_1.get()), has(CompatibleComponent.CIRCUIT_1.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.SOLAR_GENERATOR_MODULE_1.get()),
                SizedIngredient.of(Items.GOLD_INGOT, 4),
                RecipeCategory.TOOLS,
                MPSItems.SOLAR_GENERATOR_MODULE_2.get())
            .unlockedBy(getHasName(MPSItems.TIER_2_SMITHING_UPGRADE.get()), has(MPSItems.TIER_2_SMITHING_UPGRADE.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.SOLAR_GENERATOR_MODULE_2.get()),
                SizedIngredient.of(Tags.Items.GEMS_DIAMOND, 4),
                RecipeCategory.TOOLS,
                MPSItems.SOLAR_GENERATOR_MODULE_3.get())
            .unlockedBy(getHasName(MPSItems.TIER_3_SMITHING_UPGRADE.get()), has(MPSItems.TIER_3_SMITHING_UPGRADE.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.SOLAR_GENERATOR_MODULE_3.get()),
                SizedIngredient.of(Tags.Items.INGOTS_NETHERITE, 4),
                RecipeCategory.TOOLS,
                MPSItems.SOLAR_GENERATOR_MODULE_4.get())
            .unlockedBy(getHasName(MPSItems.TIER_4_SMITHING_UPGRADE.get()), has(MPSItems.TIER_4_SMITHING_UPGRADE.get()))
            .save(output);

        // Environmental -------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.ACTIVE_CAMOUFLAGE_MODULE.get())
            .pattern("LCL")
            .pattern("CMC")
            .pattern("LCL")
            .define('L', MPSItems.HOLOGRAM_EMITTER.get())
            .define('M', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
            .define('C', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.AUTO_FEEDER_MODULE.get())
            .pattern("FOF")
            .pattern("RCR")
            .pattern("SMS")
            .define('F', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('O', Tags.Items.CHESTS)
            .define('R', MPSItems.WIRING_STANDARD.get())
            .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
            .define('M', ComponentIngredient.of(MPSItems.RUBBER_HOSE.get()))
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.MAGNET_MODULE.get())
            .pattern("IMI")
            .pattern("WCW")
            .pattern("IMI")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('M', MPSItems.MAGNET.get())
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.MOB_REPULSOR_MODULE.get())
            .pattern("FWF")
            .pattern("WCW")
            .pattern("FWF")
            .define('F', MPSItems.FORCE_FIELD_EMITTER.get())
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.PIGLIN_PACIFICATION_MODULE.get())
            .pattern(" S ")
            .pattern(" G ")
            .define('S', Tags.Items.STRINGS)
            .define('G', Tags.Items.NUGGETS_GOLD)
            .unlockedBy(getHasName(Items.GOLD_NUGGET), has(Items.GOLD_NUGGET))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.WATER_ELECTROLYZER_MODULE.get())
            .pattern("WLW")
            .pattern("OCO")
            .pattern("WRW")
            .define('L', ComponentIngredient.of(MPSItems.CAPACITOR_1.get()))
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('R', ComponentIngredient.of(MPSItems.RUBBER_HOSE.get()))
            .define('O', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        // Fluid Tanks ---------------------------------------------------------------------------------

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.COOLANT_TANK_MODULE_1.get())
            .pattern("PGP")
            .pattern("RCR")
            .pattern("PGP")
            .define('P', ComponentIngredient.of(MPSItems.PLATING_IRON.get()))
            .define('G', Tags.Items.GLASS_PANES)
            .define('R', ComponentIngredient.of(MPSItems.RUBBER_HOSE.get()))
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_1.get()), has(CompatibleComponent.CIRCUIT_1.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.COOLANT_TANK_MODULE_1.get()),
                SizedIngredient.of(Tags.Items.INGOTS_GOLD, 2),
                RecipeCategory.MISC,
                MPSItems.COOLANT_TANK_MODULE_2.get())
            .unlockedBy(getHasName(MPSItems.TIER_2_SMITHING_UPGRADE.get()), has(MPSItems.TIER_2_SMITHING_UPGRADE.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.COOLANT_TANK_MODULE_2.get()),
                SizedIngredient.of(Tags.Items.GEMS_DIAMOND, 2),
                RecipeCategory.MISC,
                MPSItems.COOLANT_TANK_MODULE_3.get())
            .unlockedBy(getHasName(MPSItems.TIER_3_SMITHING_UPGRADE.get()), has(MPSItems.TIER_3_SMITHING_UPGRADE.get()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.COOLANT_TANK_MODULE_3.get()),
                SizedIngredient.of(Tags.Items.INGOTS_NETHERITE, 2),
                RecipeCategory.MISC,
                MPSItems.COOLANT_TANK_MODULE_4.get())
            .unlockedBy(getHasName(MPSItems.TIER_4_SMITHING_UPGRADE.get()), has(MPSItems.TIER_4_SMITHING_UPGRADE.get()))
            .save(output);

        // Mining Enchantments ------------------------------------------------------------------------
        if(provider != null) {
            provider.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.AQUA_AFFINITY)
                .ifPresent(enchantmentReference -> ShapedEnchantmentRecipeBuilder
                    .shaped(RecipeCategory.MISC, MPSItems.AQUA_AFFINITY_MODULE.get())
                    .pattern("ENE")
                    .pattern("WCW")
                    .pattern("EEE")
                    .define('E', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
                    .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
                    .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
                    .define('N', enchantmentReference, 1)
                    .unlockedBy(getHasName(Items.ENCHANTED_BOOK), has(Items.ENCHANTED_BOOK))
                    .save(output));

            provider.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.FORTUNE)
                .ifPresent(enchantmentReference -> ShapedEnchantmentRecipeBuilder
                    .shaped(RecipeCategory.MISC, MPSItems.FORTUNE_MODULE.get())
                    .pattern("ENE")
                    .pattern("WCW")
                    .pattern("EEE")
                    .define('E', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
                    .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
                    .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
                    .define('N', enchantmentReference, 3)
                    .unlockedBy(getHasName(Items.ENCHANTED_BOOK), has(Items.ENCHANTED_BOOK))
                    .save(output));

            provider.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.SILK_TOUCH)
                .ifPresent(enchantmentReference -> ShapedEnchantmentRecipeBuilder
                    .shaped(RecipeCategory.MISC, MPSItems.SILK_TOUCH_MODULE.get())
                    .pattern("ENE")
                    .pattern("WCW")
                    .pattern("EEE")
                    .define('E', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
                    .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
                    .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
                    .define('N', enchantmentReference, 1)
                    .unlockedBy(getHasName(Items.ENCHANTED_BOOK), has(Items.ENCHANTED_BOOK))
                    .save(output));
        }

        // Mining Enhancements ------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.TUNNEL_BORE_MODULE.get())
            .pattern("SES")
            .pattern("ECE")
            .pattern("SES")
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
            .define('E', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.VEIN_MINER_MODULE.get())
            .pattern("CSC")
            .pattern("SMS")
            .pattern("CSC")
            .define('C', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
            .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
            .define('M', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.SELECTIVE_MINER_MODULE.get())
            .pattern("OCO")
            .pattern("GMG")
            .pattern("SGS")
            .define('C', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
            .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
            .define('O', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('M', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
            .define('G', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        // Movement -----------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.BLINK_DRIVE_MODULE.get())
            .pattern("CIC")
            .pattern("FPF")
            .pattern("WIW")
            .define('C', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
            .define('P', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
            .define('F', MPSItems.FORCE_FIELD_EMITTER.get())
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('I', ComponentIngredient.of(MPSItems.ION_THRUSTER.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.UPHILL_STEP_ASSIST_MODULE.get())
            .pattern("W W")
            .pattern("SCS")
            .pattern("WAW")
            .define('A', MPSItems.ARTIFICIAL_MUSCLE.get())
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_1.get()), has(CompatibleComponent.CIRCUIT_1.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.DIMENSIONAL_TEAR_MODULE.get())
            .pattern("ECE")
            .pattern("COC")
            .pattern("ECE")
            .define('C', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
            .define('E', Items.ENDER_EYE)
            .define('O', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.FLIGHT_CONTROL_MODULE.get())
            .pattern("CWC")
            .pattern("WOW")
            .pattern("CWC")
            .define('C', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('O', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.GLIDER_MODULE.get())
            .pattern("SWS")
            .pattern("GCG")
            .pattern("SWS")
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
            .define('G', MPSItems.GLIDER_WING.get())
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.JET_BOOTS_MODULE.get())
            .pattern("S S")
            .pattern("CMC")
            .pattern("IWI")
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('M', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
            .define('C', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
            .define('I', ComponentIngredient.of(MPSItems.ION_THRUSTER.get()))
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.JETPACK_MODULE.get())
            .pattern("ISI")
            .pattern("WCW")
            .pattern("ISI")
            .define('I', ComponentIngredient.of(MPSItems.ION_THRUSTER.get()))
            .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
            .define('W', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.JUMP_ASSIST_MODULE.get())
            .pattern("SWS")
            .pattern("WCW")
            .pattern("SAS")
            .define('A', MPSItems.ARTIFICIAL_MUSCLE.get())
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('W', Items.WHITE_WOOL)
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.SHOCK_ABSORBER_MODULE.get())
            .pattern("S S")
            .pattern("SCS")
            .pattern("WAW")
            .define('A', MPSItems.ARTIFICIAL_MUSCLE.get())
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('W', Items.WHITE_WOOL)
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_1.get()), has(CompatibleComponent.CIRCUIT_1.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.SPRINT_ASSIST_MODULE.get())
            .pattern("SNS")
            .pattern("NMN")
            .pattern("ACA")
            .define('A', MPSItems.ARTIFICIAL_MUSCLE.get())
            .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
            .define('N', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('C', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
            .define('M', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.SWIM_BOOST_MODULE.get())
            .pattern("SHS")
            .pattern("WCW")
            .pattern("HAH")
            .define('A', MPSItems.ARTIFICIAL_MUSCLE.get())
            .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
            .define('H', ComponentIngredient.of(MPSItems.RUBBER_HOSE.get()))
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        // Tools ------------------------------------------------------------------------------------------------
        // Axes -----------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.STONE_AXE_MODULE.get())
            .pattern("IIW")
            .pattern("ICW")
            .pattern("AXA")
            .define('A', MPSItems.ARTIFICIAL_MUSCLE.get())
            .define('I', Tags.Items.COBBLESTONES)
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('X', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_1.get()), has(CompatibleComponent.CIRCUIT_1.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.STONE_AXE_MODULE.get()),
                SizedIngredient.of(Items.IRON_INGOT, 3),
                RecipeCategory.TOOLS,
                MPSItems.IRON_AXE_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.IRON_AXE_MODULE.get()),
                SizedIngredient.of(Items.DIAMOND, 3),
                RecipeCategory.TOOLS,
                MPSItems.DIAMOND_AXE_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.DIAMOND_AXE_MODULE.get()),
                SizedIngredient.of(Items.NETHERITE_INGOT, 3),
                RecipeCategory.TOOLS,
                MPSItems.NETHERITE_AXE_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);

        // Pickaxes -------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.STONE_PICKAXE_MODULE.get())
            .pattern("III")
            .pattern("WCW")
            .pattern("AXA")
            .define('A', MPSItems.ARTIFICIAL_MUSCLE.get())
            .define('I', Tags.Items.COBBLESTONES)
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('X', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_1.get()), has(CompatibleComponent.CIRCUIT_1.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.STONE_PICKAXE_MODULE.get()),
                SizedIngredient.of(Items.IRON_INGOT, 3),
                RecipeCategory.TOOLS,
                MPSItems.IRON_PICKAXE_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.IRON_PICKAXE_MODULE.get()),
                SizedIngredient.of(Items.DIAMOND, 3),
                RecipeCategory.TOOLS,
                MPSItems.DIAMOND_PICKAXE_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.DIAMOND_PICKAXE_MODULE.get()),
                SizedIngredient.of(Items.NETHERITE_INGOT, 3),
                RecipeCategory.TOOLS,
                MPSItems.NETHERITE_PICKAXE_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);

        // Rototillers --------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.STONE_ROTOTILLER_MODULE.get())
            .pattern("SS ")
            .pattern("WCW")
            .pattern("AXA")
            .define('A', MPSItems.ARTIFICIAL_MUSCLE.get())
            .define('S', Tags.Items.COBBLESTONES)
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('X', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_1.get()), has(CompatibleComponent.CIRCUIT_1.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.STONE_ROTOTILLER_MODULE.get()),
                SizedIngredient.of(Items.IRON_INGOT, 2),
                RecipeCategory.TOOLS,
                MPSItems.IRON_ROTOTILLER_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.IRON_ROTOTILLER_MODULE.get()),
                SizedIngredient.of(Items.DIAMOND, 2),
                RecipeCategory.TOOLS,
                MPSItems.DIAMOND_ROTOTILLER_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.DIAMOND_ROTOTILLER_MODULE.get()),
                SizedIngredient.of(Items.NETHERITE_INGOT, 2),
                RecipeCategory.TOOLS,
                MPSItems.NETHERITE_ROTOTILLER_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);

        // Shovels --------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.STONE_SHOVEL_MODULE.get())
            .pattern(" S ")
            .pattern("WCW")
            .pattern("AXA")
            .define('A', MPSItems.ARTIFICIAL_MUSCLE.get())
            .define('S', Tags.Items.COBBLESTONES)
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('X', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_1.get()), has(CompatibleComponent.CIRCUIT_1.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_2_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.STONE_SHOVEL_MODULE.get()),
                SizedIngredient.of(Items.IRON_INGOT, 1),
                RecipeCategory.TOOLS,
                MPSItems.IRON_SHOVEL_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_3_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.IRON_SHOVEL_MODULE.get()),
                SizedIngredient.of(Items.DIAMOND, 1),
                RecipeCategory.TOOLS,
                MPSItems.DIAMOND_SHOVEL_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(output);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                Ingredient.of(MPSItems.DIAMOND_SHOVEL_MODULE.get()),
                SizedIngredient.of(Items.NETHERITE_INGOT, 1),
                RecipeCategory.TOOLS,
                MPSItems.NETHERITE_SHOVEL_MODULE.get())
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(output);

        // Misc tools -----------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.LEAF_BLOWER_MODULE.get())
            .pattern("I  ")
            .pattern("WCW")
            .pattern("IS ")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.LUX_CAPACITOR_MODULE.get())
            .pattern("IRI")
            .pattern("WCW")
            .pattern("GIG")
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .define('G', Tags.Items.DUSTS_GLOWSTONE)
            .define('I', Tags.Items.INGOTS_IRON)
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.SHEARS_MODULE.get())
            .pattern("I I")
            .pattern("WCW")
            .pattern(" S ")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.FLINT_AND_STEEL_MODULE.get())
            .pattern(" F ")
            .pattern("WCW")
            .pattern("ISI")
            .define('F', Items.FLINT)
            .define('I', Tags.Items.INGOTS_IRON)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_1.get()), has(CompatibleComponent.CIRCUIT_1.discoveryTag()))
            .save(output);

        // Wrench-shaped frame: a crafting component, never a standalone usable tool.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.WRENCH_MODULE_FRAME.get())
            .pattern("I I")
            .pattern(" C ")
            .pattern(" I ")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('C', Tags.Items.INGOTS_COPPER)
            .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.UNIVERSAL_WRENCH_MODULE.get())
            .pattern(" F ")
            .pattern("WCW")
            .pattern(" S ")
            .define('F', MPSItems.WRENCH_MODULE_FRAME.get())
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
            .unlockedBy(getHasName(MPSItems.WRENCH_MODULE_FRAME.get()), has(MPSItems.WRENCH_MODULE_FRAME.get()))
            .save(output);

        // Vision ------------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.BINOCULARS_MODULE.get())
            .pattern("WWW")
            .pattern("WCW")
            .pattern("WLW")
            .define('L', MPSItems.HOLOGRAM_EMITTER.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .define('W', MPSItems.WIRING_STANDARD.get())
            .unlockedBy(getHasName(MPSItems.HOLOGRAM_EMITTER.get()), has(MPSItems.HOLOGRAM_EMITTER.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.NIGHTVISION_MODULE.get())
            .pattern("WLW")
            .pattern("WCW")
            .pattern("WLW")
            .define('L', MPSItems.HOLOGRAM_EMITTER.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .define('W', MPSItems.WIRING_STANDARD.get())
            .unlockedBy(getHasName(MPSItems.HOLOGRAM_EMITTER.get()), has(MPSItems.HOLOGRAM_EMITTER.get()))
            .save(output);

        // Weapons ----------------------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.BLADE_LAUNCHER_MODULE.get())
            .pattern("WMW")
            .pattern("WCW")
            .pattern("WSW")
            .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
            .define('M', ComponentIngredient.of(MPSItems.CAPACITOR_2.get()))
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .define('W', MPSItems.WIRING_STANDARD.get())
            .unlockedBy(getHasName(MPSItems.SERVO.get()), has(CompatibleComponent.SERVO.discoveryTag()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.LIGHTNING_SUMMONER_MODULE.get())
            .pattern("WHW")
            .pattern("WFW")
            .pattern("WSW")
            .define('H', ComponentIngredient.of(MPSItems.CAPACITOR_4.get()))
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('F', MPSItems.FORCE_FIELD_EMITTER.get())
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .unlockedBy(getHasName(MPSItems.SOLENOID.get()), has(CompatibleComponent.SOLENOID.discoveryTag()))
            .save(output);

        for (boolean electrum : new boolean[]{true, false}) {
            var metal = electrum ? ELECTRUM_INGOTS : Tags.Items.INGOTS_GOLD;
            var conditional = output.withConditions(electrum ? new NotCondition(new TagEmptyCondition(ELECTRUM_INGOTS)) : new TagEmptyCondition(ELECTRUM_INGOTS));
            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.MELEE_ASSIST_MODULE.get())
                .pattern(" S ")
                .pattern("ACA")
                .pattern("IAI")
                .define('A', MPSItems.ARTIFICIAL_MUSCLE.get())
                .define('S', ComponentIngredient.of(MPSItems.SERVO.get()))
                .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
                .define('I', metal)
                .unlockedBy(getHasName(MPSItems.FORCE_FIELD_EMITTER.get()), has(MPSItems.FORCE_FIELD_EMITTER.get()))
                .save(conditional, conductiveRecipeId(MPSItems.MELEE_ASSIST_MODULE.get(), electrum));

            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.PLASMA_CANNON_MODULE.get())
                .pattern("SGC")
                .pattern("SMC")
                .pattern("SGC")
                .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
                .define('C', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
                .define('G', metal)
                .define('M', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
                .unlockedBy("has_conductive_ingot", has(metal))
                .save(conditional, conductiveRecipeId(MPSItems.PLASMA_CANNON_MODULE.get(), electrum));

            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.RAILGUN_MODULE.get())
                .pattern("SOC")
                .pattern("CMS")
                .pattern("SOC")
                .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
                .define('C', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
                .define('M', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
                .define('O', metal)
                .unlockedBy(getHasName(MPSItems.FORCE_FIELD_EMITTER.get()), has(MPSItems.FORCE_FIELD_EMITTER.get()))
                .save(conditional, conductiveRecipeId(MPSItems.RAILGUN_MODULE.get(), electrum));
        }
/*
    public static final DeferredHolder<Item, SonicWeaponModule> SONIC_WEAPON_MODULE = MPS_ITEMS.register(MPSConstants.SONIC_WEAPON_MODULE.getPath(),
            SonicWeaponModule::new);
 */
    }

    private static ResourceLocation conductiveRecipeId(Item item, boolean electrum) {
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
        return electrum ? id : id.withPath("compat/fallback/" + id.getPath() + "_gold");
    }

    private void buildCompatibleCraftingRecipes(RecipeOutput output) {
        var iron = CompatibleCraftingRecipe.IRON_LARGE_PLATES;
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.IRON_PLATING_MODULE.get())
                .pattern("PP").pattern("PP")
                .define('P', new CompatibleCraftingIngredient(iron).toVanilla())
                .unlockedBy("has_large_plate", has(iron.discoveryTag())).save(output, iron.id());
        var diamond = CompatibleCraftingRecipe.DIAMOND_LARGE_PLATES;
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, MPSItems.DIAMOND_PLATING_MODULE.get())
                .pattern("PP").pattern("PP")
                .define('P', new CompatibleCraftingIngredient(diamond).toVanilla())
                .unlockedBy("has_large_plate", has(diamond.discoveryTag())).save(output, diamond.id());
        var hose = CompatibleCraftingRecipe.RUBBER_HOSE;
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.RUBBER_HOSE.get())
                .pattern("WWW").pattern("R R").pattern("WWW")
                .define('W', Items.WHITE_WOOL).define('R', new CompatibleCraftingIngredient(hose).toVanilla())
                .unlockedBy("has_rubber", has(hose.discoveryTag())).save(output, hose.id());
    }

    private void buildFirstPassModules(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.BLOCK_REACH_MODULE.get())
                .pattern("SRS").pattern("CDT")
                .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
                .define('R', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
                .define('C', ComponentIngredient.of(MPSItems.CAPACITOR_4.get()))
                .define('D', Items.SCAFFOLDING)
                .define('T', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
                .unlockedBy("has_scaffolding", has(Items.SCAFFOLDING)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.SHOCK_COIL.get())
                .pattern("CRC").pattern("ITI").pattern("CRC")
                .define('C', ComponentIngredient.of(MPSItems.CAPACITOR_4.get())).define('R', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
                .define('I', ComponentIngredient.of(MPSItems.ION_THRUSTER.get())).define('T', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
                .unlockedBy("has_capacitor", has(CompatibleComponent.CAPACITOR_4.discoveryTag())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.SHOCK_MODULE.get())
                .pattern("TST").pattern("RMR").pattern("TST")
                .define('T', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get())).define('S', MPSItems.SHOCK_COIL.get())
                .define('R', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get())).define('M', MPSItems.MOB_REPULSOR_MODULE.get())
                .unlockedBy("has_repulsor", has(MPSItems.MOB_REPULSOR_MODULE.get())).save(output);
        Item[] circuits = {MPSItems.CONTROL_CIRCUIT_1.get(), MPSItems.CONTROL_CIRCUIT_2.get(), MPSItems.CONTROL_CIRCUIT_3.get(), MPSItems.CONTROL_CIRCUIT_4.get()};
        Item[] capacitors = {MPSItems.CAPACITOR_1.get(), MPSItems.CAPACITOR_2.get(), MPSItems.CAPACITOR_3.get(), MPSItems.CAPACITOR_4.get()};
        for (var entry : mpsremodularized.common.compat.IntermodModules.ENTRIES) {
            var id = ResourceLocation.fromNamespaceAndPath("mpsremodularized", entry.id());
            var donorTag = TagKey.create(Registries.ITEM, id.withPrefix("native_modules/"));
            var keys = new java.util.HashMap<Character, Ingredient>();
            keys.put('S', ComponentIngredient.of(MPSItems.SOLENOID.get()));
            keys.put('R', ComponentIngredient.of(circuits[entry.tier() - 1]));
            keys.put('C', ComponentIngredient.of(capacitors[entry.tier() - 1]));
            keys.put('D', entry.kind() == mpsremodularized.common.compat.NativeModuleItem.Kind.CONSTRUCTION_STICK
                    ? new mpsremodularized.common.recipe.ingredients.BatteryStickIngredient(Ingredient.of(donorTag)).toVanilla()
                    : Ingredient.of(donorTag));
            if (entry.tier() == 4) keys.put('T', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()));
            var pattern = new java.util.ArrayList<String>();
            if (entry.id().equals("anvil_module")) pattern.add("DDD");
            pattern.add("SRS");
            pattern.add(entry.tier() == 4 ? "CDT" : "CD ");
            var shaped = new net.minecraft.world.item.crafting.ShapedRecipe("", CraftingBookCategory.MISC,
                    net.minecraft.world.item.crafting.ShapedRecipePattern.of(keys, pattern),
                    new ItemStack(entry.item().get()));
            var advancement = output.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                    .addCriterion("has_donor", has(donorTag)).rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(id))
                    .requirements(AdvancementRequirements.Strategy.OR);
            (entry.provider() == null ? output : output.withConditions(new net.neoforged.neoforge.common.conditions.ModLoadedCondition(entry.provider().modId)))
                    .accept(id, new mpsremodularized.common.recipe.NativeModuleRecipe(shaped), advancement.build(id.withPrefix("recipes/misc/")));
        }
    }

    private void buildCompatibilityConversions(RecipeOutput output) {
        for (CompatibleComponent component : CompatibleComponent.values()) {
            if (!component.battery) continue;
            ItemStack result = new ItemStack(component.nativeItem.get());
            result.set(MPSDataComponents.ENERGY, 0);
            Ingredient input = new ComponentIngredient(component, true, CompatibilityRecipes.policy()).toVanilla();
            var recipe = new ShapelessRecipe("", CraftingBookCategory.MISC, result, NonNullList.of(Ingredient.EMPTY, input));
            var id = component.conversionId();
            Advancement.Builder advancement = output.advancement()
                    .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                    .addCriterion("has_component", has(component.discoveryTag()))
                    .rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(id))
                    .requirements(AdvancementRequirements.Strategy.OR);
            output.accept(id, recipe, advancement.build(id.withPrefix("recipes/misc/")));
        }
    }

    private void buildComponentRecipes(RecipeOutput recipeOutput) {
        // Charging base
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.CHARGING_BASE_ITEM.get())
            .pattern("WBW")
            .pattern("WCW")
            .pattern("PPP")
            .define('P', Items.OAK_PRESSURE_PLATE)
            .define('B', ComponentIngredient.of(MPSItems.CAPACITOR_1.get()))// Switch to capacitor
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .define('W', MPSItems.WIRING_STANDARD.get())
            .unlockedBy(getHasName(Items.OAK_PRESSURE_PLATE), has(Items.OAK_PRESSURE_PLATE))
            .save(recipeOutput);

        // Armor Stand
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MPSItems.ARMOR_STAND_ITEM.get())
            .requires(Items.ARMOR_STAND)
            .requires(Items.COAL, 2)
            .unlockedBy(getHasName(Items.ARMOR_STAND), has(Items.ARMOR_STAND))
            .save(recipeOutput);

        // Upgrades -----------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.TIER_2_SMITHING_UPGRADE.get())
            .pattern("WIW")
            .pattern("WCW")
            .pattern("WWW")
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('I', Items.IRON_INGOT)
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
            .unlockedBy(getHasName(MPSItems.WIRING_STANDARD.get()), has(MPSItems.WIRING_STANDARD.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.TIER_3_SMITHING_UPGRADE.get())
            .pattern("WIW")
            .pattern("WCW")
            .pattern("WWW")
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('I', Items.GOLD_INGOT)
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
            .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
            .unlockedBy(getHasName(MPSItems.WIRING_HIGH_CONDUCTIVITY.get()), has(MPSItems.WIRING_HIGH_CONDUCTIVITY.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.TIER_4_SMITHING_UPGRADE.get())
            .pattern("WIW")
            .pattern("WCW")
            .pattern("WWW")
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('I', Items.NETHERITE_INGOT)
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
            .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
            .unlockedBy(getHasName(MPSItems.WIRING_HIGH_CONDUCTIVITY.get()), has(MPSItems.WIRING_HIGH_CONDUCTIVITY.get()))
            .save(recipeOutput);


        // Modules ------------------------------------------------------------------------------------
        // Energy Storage

        // First tier battery derived from capacitor to avoid redstone usage
        ShapedEnergyRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.BATTERY_1.get())
            .pattern("WRW")
            .pattern("ICI")
            .pattern("IWI")
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('I', Tags.Items.INGOTS_IRON)
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_1.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_1.get()), has(CompatibleComponent.CIRCUIT_1.discoveryTag()))
            .save(recipeOutput);

        // Above tier 1, player should have access to redstone
        ShapedEnergyRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.BATTERY_2.get())
            .pattern("WBW")
            .pattern("BCB")
            .pattern("BWB")
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .define('B', ComponentIngredient.of(MPSItems.BATTERY_1.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_2.get()), has(CompatibleComponent.CIRCUIT_2.discoveryTag()))
            .save(recipeOutput);

        // Above tier 2, player should have access to gold
        ShapedEnergyRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.BATTERY_3.get())
            .pattern("WBW")
            .pattern("BCB")
            .pattern("BWB")
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_3.get()))
            .define('B', ComponentIngredient.of(MPSItems.BATTERY_2.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_3.get()), has(CompatibleComponent.CIRCUIT_3.discoveryTag()))
            .save(recipeOutput);

        ShapedEnergyRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.BATTERY_4.get())
            .pattern("WBW")
            .pattern("BCB")
            .pattern("BWB")
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_4.get()))
            .define('B', ComponentIngredient.of(MPSItems.BATTERY_3.get()))
            .unlockedBy(getHasName(MPSItems.CONTROL_CIRCUIT_4.get()), has(CompatibleComponent.CIRCUIT_4.discoveryTag()))
            .save(recipeOutput);

        // Components ---------------------------------------------------------------------------------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.ARTIFICIAL_MUSCLE.get())
            .pattern("CCC")
            .pattern("CGC")
            .pattern("CCC")
            .define('C', MPSItems.CARBON_MYOFIBER.get())
            .define('G', MPSItems.MYOFIBER_GEL.get())
            .unlockedBy(getHasName(MPSItems.CARBON_MYOFIBER.get()), has(MPSItems.CARBON_MYOFIBER.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.CARBON_MYOFIBER.get())
            .pattern("CCC")
            .pattern("CCC")
            .pattern("CCC")
            .define('C', Tags.Items.STORAGE_BLOCKS_COAL)
            .unlockedBy(getHasName(Items.COAL_BLOCK), has(Tags.Items.STORAGE_BLOCKS_COAL))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.CAPACITOR_1.get())
            .pattern("WPI")
            .pattern("WCW")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('P', Items.PAPER)
            .define('C', Items.CHARCOAL)
            .unlockedBy(getHasName(Items.CHARCOAL), has(Items.CHARCOAL))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.CAPACITOR_2.get())
            .pattern("WPI")
            .pattern("WLW")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('P', Items.PAPER)
            .define('L', Tags.Items.GEMS_LAPIS)
            .unlockedBy(getHasName(Items.LAPIS_LAZULI), has(Items.LAPIS_LAZULI))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.CAPACITOR_3.get())
            .pattern("WGD")
            .pattern("WEW")
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('G', Tags.Items.GLASS_PANES)
            .define('D', Tags.Items.DUSTS_GLOWSTONE)
            .define('E', Tags.Items.ENDER_PEARLS)
            .unlockedBy(getHasName(Items.ENDER_PEARL), has(Items.ENDER_PEARL))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.CAPACITOR_4.get())
            .pattern("WCW")
            .pattern("W!W")
            .pattern("WCW")
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('C', ComponentIngredient.of(MPSItems.CAPACITOR_3.get()))
            .define('!', ComponentIngredient.of(MPSItems.COMPUTER_CHIP.get()))
            .unlockedBy(getHasName(MPSItems.CAPACITOR_3.get()), has(CompatibleComponent.CAPACITOR_3.discoveryTag()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.COMPUTER_CHIP.get())
            .pattern("RSR")
            .pattern("CDC")
            .pattern("RLR")
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('S', Items.DAYLIGHT_DETECTOR)
            .define('C', Items.COMPARATOR)
            .define('D', Tags.Items.GEMS_DIAMOND)
            .define('L', MPSItems.HOLOGRAM_EMITTER.get())
            .unlockedBy(getHasName(Items.DAYLIGHT_DETECTOR), has(Items.DAYLIGHT_DETECTOR))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.CONTROL_CIRCUIT_1.get())
            .pattern("WCW")
            .pattern("R O")
            .pattern("ERW")
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('C', ComponentIngredient.of(MPSItems.CAPACITOR_1.get()))
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('E', Tags.Items.INGOTS_COPPER)
            .define('O', Items.YELLOW_DYE)
            .unlockedBy(getHasName(MPSItems.WIRING_STANDARD.get()), has(MPSItems.WIRING_STANDARD.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.CONTROL_CIRCUIT_2.get())
            .pattern("WCW")
            .pattern("RGO")
            .pattern("ERW")
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('C', ComponentIngredient.of(MPSItems.CAPACITOR_2.get()))
            .define('E', Tags.Items.INGOTS_COPPER)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('G', Tags.Items.DUSTS_GLOWSTONE)
            .define('O', Items.RED_DYE)
            .unlockedBy(getHasName(MPSItems.WIRING_STANDARD.get()), has(MPSItems.WIRING_STANDARD.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.CONTROL_CIRCUIT_3.get())
            .pattern("WCW")
            .pattern("RGO")
            .pattern("ERW")
            .define('C', ComponentIngredient.of(MPSItems.CAPACITOR_3.get()))
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('E', Tags.Items.INGOTS_GOLD)
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('G', Tags.Items.DUSTS_GLOWSTONE)
            .define('O', Items.BLUE_DYE)
            .unlockedBy(getHasName(MPSItems.WIRING_HIGH_CONDUCTIVITY.get()), has(MPSItems.WIRING_HIGH_CONDUCTIVITY.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.CONTROL_CIRCUIT_4.get())
            .pattern("WCW")
            .pattern("RGO")
            .pattern("ERW")
            .define('C', ComponentIngredient.of(MPSItems.CAPACITOR_4.get()))
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('E', Tags.Items.INGOTS_GOLD)
            .define('W', MPSItems.WIRING_HIGH_CONDUCTIVITY.get())
            .define('G', Tags.Items.ENDER_PEARLS)
            .define('O', Items.PURPLE_DYE)
            .unlockedBy(getHasName(MPSItems.WIRING_HIGH_CONDUCTIVITY.get()), has(MPSItems.WIRING_HIGH_CONDUCTIVITY.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.FORCE_FIELD_EMITTER.get())
            .pattern("SES")
            .pattern("ESE")
            .pattern("SES")
            .define('E', Items.ENDER_PEARL)
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .unlockedBy(getHasName(Items.ENDER_PEARL), has(Items.ENDER_PEARL))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.GLIDER_WING.get())
            .pattern(" II")
            .pattern("IAS")
            .pattern("I  ")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('A', ComponentIngredient.of(MPSItems.ARTIFICIAL_MUSCLE.get()))
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .unlockedBy(getHasName( MPSItems.SOLENOID.get()), has( MPSItems.SOLENOID.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.HOLOGRAM_EMITTER.get())
            .pattern("#G!")
            .pattern("GWG")
            .pattern("@G&")
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('#', Tags.Items.DUSTS_GLOWSTONE)
            .define('!', Tags.Items.DYES_GREEN)
            .define('@', Tags.Items.DYES_BLUE)
            .define('&', Tags.Items.DYES_RED)
            .unlockedBy(getHasName( MPSItems.SOLENOID.get()), has( MPSItems.SOLENOID.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.ION_THRUSTER.get())
            .pattern(" FS")
            .pattern(" IG")
            .pattern("WFS")
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('F', MPSItems.FORCE_FIELD_EMITTER.get())
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('I', Tags.Items.INGOTS_IRON)
            .define('G', Tags.Items.DUSTS_GLOWSTONE)
            .unlockedBy(getHasName( MPSItems.SOLENOID.get()), has( MPSItems.SOLENOID.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.MAGNET.get())
            .pattern("III")
            .pattern("SSS")
            .pattern("III")
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('I', Tags.Items.INGOTS_IRON)
            .unlockedBy(getHasName( MPSItems.SOLENOID.get()), has( MPSItems.SOLENOID.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.MYOFIBER_GEL.get())
            .pattern(" M ")
            .pattern("IMI")
            .pattern("IMI")
            .define('M', Items.MAGMA_CREAM)
            .define('I', Tags.Items.INGOTS_IRON)
            .unlockedBy(getHasName(Items.MAGMA_CREAM), has(Items.MAGMA_CREAM))
            .save(recipeOutput);



        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.PLATING_IRON.get())
            .pattern("II")
            .pattern("WI")
            .pattern("II")
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('I', Tags.Items.INGOTS_IRON)
            .unlockedBy(getHasName(MPSItems.WIRING_STANDARD.get()), has(MPSItems.WIRING_STANDARD.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.PLATING_DIAMOND.get())
            .pattern("DD")
            .pattern("WD")
            .pattern("DD")
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('D', Tags.Items.GEMS_DIAMOND)
            .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
            .save(recipeOutput);

        SmithingUpgradeRecipeBuilder.smithing(
                Ingredient.of(MPSItems.TIER_4_SMITHING_UPGRADE.get()),
                ComponentIngredient.of(MPSItems.PLATING_DIAMOND.get()),
                SizedIngredient.of(Tags.Items.INGOTS_NETHERITE, 3),
                RecipeCategory.MISC,
                MPSItems.PLATING_NETHERITE.get())
            .unlockedBy(getHasName(MPSItems.TIER_4_SMITHING_UPGRADE.get()), has(MPSItems.TIER_4_SMITHING_UPGRADE.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.RUBBER_HOSE.get())
            .pattern("WWW")
            .pattern("G G")
            .pattern("WWW")
            .define('W', Items.WHITE_WOOL)
            .define('G', Tags.Items.GLASS_BLOCKS)
            .unlockedBy(getHasName(Items.WHITE_WOOL), has(Items.WHITE_WOOL))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.SERVO.get())
            .pattern(" S ")
            .pattern("WCW")
            .pattern(" S ")
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('S', ComponentIngredient.of(MPSItems.SOLENOID.get()))
            .define('C', ComponentIngredient.of(MPSItems.CONTROL_CIRCUIT_2.get()))
            .unlockedBy(getHasName(MPSItems.SOLENOID.get()), has(CompatibleComponent.SOLENOID.discoveryTag()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.SOLAR_PANEL.get())
            .pattern("BGB")
            .pattern("GLG")
            .pattern("III")
            .define('B', Tags.Items.DYES_BLUE)
            .define('G', Tags.Items.GLASS_PANES)
            .define('L', Tags.Items.STORAGE_BLOCKS_LAPIS)
            .define('I', Tags.Items.INGOTS_IRON)
            .unlockedBy(getHasName(Items.GLASS_PANE), has(Tags.Items.GLASS_PANES))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.SOLENOID.get())
            .pattern("WIW")
            .pattern("WIW")
            .pattern("WIW")
            .define('W', MPSItems.WIRING_STANDARD.get())
            .define('I', Tags.Items.INGOTS_IRON)
            .unlockedBy(getHasName(MPSItems.WIRING_STANDARD.get()), has(MPSItems.WIRING_STANDARD.get()))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.WIRING_STANDARD.get(), 8)
            .pattern("CRC")
            .define('C', Tags.Items.INGOTS_COPPER)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy(getHasName(Items.COPPER_INGOT), has(Tags.Items.INGOTS_COPPER))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.WIRING_HIGH_CONDUCTIVITY.get(), 8)
            .pattern("ERE")
            .define('E', ELECTRUM_INGOTS)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_electrum", has(ELECTRUM_INGOTS))
            .save(recipeOutput.withConditions(new NotCondition(new TagEmptyCondition(ELECTRUM_INGOTS))));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MPSItems.WIRING_HIGH_CONDUCTIVITY.get(), 8)
            .pattern("GRG")
            .define('G', Tags.Items.INGOTS_GOLD)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy(getHasName(Items.GOLD_INGOT), has(Tags.Items.INGOTS_GOLD))
            .save(recipeOutput.withConditions(new TagEmptyCondition(ELECTRUM_INGOTS)),
                    ResourceLocation.parse("mpsremodularized:compat/fallback/high_conductivity_wire_gold"));
        }
}
