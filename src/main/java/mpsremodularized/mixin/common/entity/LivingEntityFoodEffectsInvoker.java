package mpsremodularized.mixin.common.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Reuse Minecraft's food-effect handling independently of the feeder's nutrition buffers. */
@Mixin(LivingEntity.class)
public interface LivingEntityFoodEffectsInvoker {
    @Invoker("addEatEffect")
    void mpsremodularized$applyFoodEffects(FoodProperties food);
}
