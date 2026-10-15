package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeStationIntegration;
import mpsremodularized.common.compat.PortableStationIntegration;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets = "blusunrize.immersiveengineering.common.gui.IEContainerMenu", remap = false)
public abstract class ImmersiveOwnedMenuMixin {
    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void mps$energy(CallbackInfo ci) { NativeStationIntegration.refreshCircuitEnergy(this); }
    @Inject(method = {"broadcastChanges", "removed", "clicked"}, at = @At("RETURN"))
    private void mps$save(CallbackInfo ci) { ((INativeToolMenu)this).mpsremodularized$saveNativeSource(); }
    @Inject(method = "quickMoveStack", at = @At("RETURN"))
    private void mps$saveMove(CallbackInfoReturnable<ItemStack> ci) { ((INativeToolMenu)this).mpsremodularized$saveNativeSource(); }
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void mps$click(int slot, int button, ClickType type, Player player, CallbackInfo ci) {
        if (!PortableStationIntegration.canClick((AbstractContainerMenu)(Object)this, slot, button, type)) ci.cancel();
        else NativeStationIntegration.refreshCircuitOutput(this);
    }
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void mps$move(Player player, int slot, CallbackInfoReturnable<ItemStack> ci) {
        if (!PortableStationIntegration.canClick((AbstractContainerMenu)(Object)this, slot, 0, ClickType.QUICK_MOVE)) ci.setReturnValue(ItemStack.EMPTY);
        else NativeStationIntegration.refreshCircuitOutput(this);
    }
}
