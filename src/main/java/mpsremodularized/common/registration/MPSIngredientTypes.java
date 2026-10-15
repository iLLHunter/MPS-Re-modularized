package mpsremodularized.common.registration;

import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.recipe.ingredients.MinEnchantedIngredient;
import mpsremodularized.common.recipe.ingredients.ComponentIngredient;
import mpsremodularized.common.recipe.ingredients.CompatibleCraftingIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class MPSIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, MPSConstants.MOD_ID);

    public static final Supplier<IngredientType<MinEnchantedIngredient>> MIN_ENCHANTED =
            INGREDIENT_TYPES.register("min_enchanted",
                    // The stream codec parameter is optional, a stream codec will be created from the codec
                    // using ByteBufCodecs#fromCodec or #fromCodecWithRegistries if the stream codec isn't specified.
                    () -> new IngredientType<>(MinEnchantedIngredient.CODEC, MinEnchantedIngredient.STREAM_CODEC));

    public static final Supplier<IngredientType<ComponentIngredient>> COMPONENT =
            INGREDIENT_TYPES.register("component", () -> new IngredientType<>(ComponentIngredient.CODEC));
    public static final Supplier<IngredientType<mpsremodularized.common.recipe.ingredients.BatteryStickIngredient>> BATTERY_STICK =
            INGREDIENT_TYPES.register("battery_stick", () -> new IngredientType<>(mpsremodularized.common.recipe.ingredients.BatteryStickIngredient.CODEC));
    public static final Supplier<IngredientType<CompatibleCraftingIngredient>> COMPATIBLE_CRAFTING =
            INGREDIENT_TYPES.register("compatible_crafting", () -> new IngredientType<>(CompatibleCraftingIngredient.CODEC));
}
