package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeCompassIntegration;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Correct the module's needle for vanilla compass textures; standalone Cyclic stays native. */
@Pseudo
@Mixin(targets = "com.lothrazar.cyclic.item.compass.GpsCompassItem", remap = false)
public abstract class CyclicCompassAngleMixin {
    @ModifyConstant(method = "getAngle", constant = @Constant(doubleValue = 0.25D))
    private static double mps$moduleBearing(double original, ItemStack stack, LivingEntity entity) {
        var owner = NativeCompassIntegration.owner(stack);
        return owner != null && owner.valid(true) ? original + 0.5D : original;
    }
}
