package mpsremodularized.common.recipe.ingredients;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import mpsremodularized.common.compat.ConstructionStickIntegration;
import mpsremodularized.common.registration.MPSIngredientTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/** Optional provider component is resolved only when that provider is loaded. */
public record BatteryStickIngredient(Ingredient base) implements ICustomIngredient {
    public static final MapCodec<BatteryStickIngredient> CODEC = Ingredient.CODEC_NONEMPTY.fieldOf("base")
            .xmap(BatteryStickIngredient::new, BatteryStickIngredient::base);
    @Override public boolean test(ItemStack stack) { return base.test(stack) && ConstructionStickIntegration.hasBattery(stack); }
    @Override public Stream<ItemStack> getItems() {
        return java.util.Arrays.stream(base.getItems()).map(stack -> {
            var copy = stack.copy(); var battery = ConstructionStickIntegration.batteryComponent();
            if (battery != null) copy.set(battery, true);
            return copy;
        });
    }
    @Override public boolean isSimple() { return false; }
    @Override public IngredientType<?> getType() { return MPSIngredientTypes.BATTERY_STICK.get(); }
}
