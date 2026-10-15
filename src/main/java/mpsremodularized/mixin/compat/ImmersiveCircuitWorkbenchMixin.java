package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeStationIntegration;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native recipe/material/edit rules remain in charge; only their energy source is the approved Fist battery. */
@Pseudo @Mixin(targets = "blusunrize.immersiveengineering.common.gui.CircuitTableMenu", remap = false)
public abstract class ImmersiveCircuitWorkbenchMixin {
    @Redirect(method = "canAssemble", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/energy/EnergyStorage;getEnergyStored()I"))
    private int mps$available(EnergyStorage storage) {
        var owner = NativeStationIntegration.circuitOwner(this);
        return owner == null ? storage.getEnergyStored() : owner.valid() ? ElectricItemUtils.getItemEnergy(owner.fist()) : 0;
    }
    @Redirect(method = "consumeInputs(Lblusunrize/immersiveengineering/api/tool/LogicCircuitHandler$LogicCircuitInstruction;Z)V", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/energy/EnergyStorage;extractEnergy(IZ)I"))
    private int mps$consume(EnergyStorage storage, int amount, boolean simulate) {
        var owner = NativeStationIntegration.circuitOwner(this);
        return owner == null ? storage.extractEnergy(amount, simulate)
                : owner.valid() ? ElectricItemUtils.drainItem(owner.fist(), amount, simulate) : 0;
    }
    @Inject(method = "receiveMessageFromScreen", at = @At("HEAD"), cancellable = true)
    private void mps$guard(CompoundTag message, CallbackInfo ci) {
        if (!((INativeToolMenu)this).mpsremodularized$nativeOwnerValid()) ci.cancel();
    }
    @Inject(method = {"receiveMessageFromScreen", "consumeInputs", "slotsChanged"}, at = @At("RETURN"))
    private void mps$save(CallbackInfo ci) {
        NativeStationIntegration.refreshCircuitEnergy(this);
        ((INativeToolMenu)this).mpsremodularized$saveNativeSource();
    }
}
