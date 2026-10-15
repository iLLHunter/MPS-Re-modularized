package mpsremodularized.mixin.common.item;

import mpsremodularized.common.compat.NativeStationIntegration;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Owned station inventories save to the module and never dirty a world block at their temporary position. */
@Mixin(BlockEntity.class)
public abstract class NativePortableBlockChangesMixin {
    @Inject(method = "setChanged()V", at = @At("HEAD"), cancellable = true)
    private void mps$owned(CallbackInfo ci) {
        if (NativeStationIntegration.portableBlock((BlockEntity)(Object)this)
                || mpsremodularized.common.compat.PoweredDiamondAnvilIntegration.portable((BlockEntity)(Object)this)) ci.cancel();
    }
}
