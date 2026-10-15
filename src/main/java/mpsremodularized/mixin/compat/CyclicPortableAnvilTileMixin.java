package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.PoweredDiamondAnvilIntegration;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets = "com.lothrazar.cyclic.block.TileBlockEntityCyclic", remap = false)
public abstract class CyclicPortableAnvilTileMixin {
    @Inject(method = "setLitProperty", at = @At("HEAD"), cancellable = true)
    private void mps$noWorldMutation(boolean lit, CallbackInfo ci) {
        if (PoweredDiamondAnvilIntegration.portable((BlockEntity)(Object)this)) ci.cancel();
    }
    @Inject(method = "syncEnergy", at = @At("HEAD"), cancellable = true)
    private void mps$noWorldPacket(CallbackInfo ci) {
        if (PoweredDiamondAnvilIntegration.portable((BlockEntity)(Object)this)) ci.cancel();
    }
    @Inject(method = "getEnergy", at = @At("HEAD"), cancellable = true)
    private void mps$buffer(CallbackInfoReturnable<Integer> ci) {
        var tile = (BlockEntity)(Object)this;
        if (PoweredDiamondAnvilIntegration.portable(tile)) ci.setReturnValue(PoweredDiamondAnvilIntegration.energy(tile).getEnergyStored());
    }
    @Inject(method = "setEnergy", at = @At("HEAD"), cancellable = true)
    private void mps$syncBuffer(int amount, CallbackInfo ci) {
        var tile = (BlockEntity)(Object)this;
        if (PoweredDiamondAnvilIntegration.portable(tile)) { PoweredDiamondAnvilIntegration.setEnergy(tile, amount); ci.cancel(); }
    }
}
