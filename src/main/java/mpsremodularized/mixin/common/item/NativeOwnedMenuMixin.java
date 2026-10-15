package mpsremodularized.mixin.common.item;

import mpsremodularized.common.compat.NativeToolIntegration;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Inert for ordinary menus; persistence runs after the native slot operation. */
@Mixin(AbstractContainerMenu.class)
public abstract class NativeOwnedMenuMixin implements INativeToolMenu {
    @Unique private NativeToolIntegration.Installed mps$boundOwner;
    @Unique private Runnable mps$saveBoundSource;
    @Override public void mpsremodularized$bindNativeOwner(NativeToolIntegration.Installed owner, Runnable save) {
        if (mps$boundOwner != null && mps$boundOwner != owner) throw new IllegalStateException("Native menu already owned");
        mps$boundOwner = owner; mps$saveBoundSource = save;
    }
    @Override public NativeToolIntegration.Installed mpsremodularized$nativeOwner() { return mps$boundOwner; }
    @Override public boolean mpsremodularized$nativeOwnerValid() { return mps$boundOwner == null || mps$boundOwner.valid(); }
    @Override public void mpsremodularized$saveNativeSource() {
        if (mps$saveBoundSource != null && mpsremodularized$nativeOwnerValid()) mps$saveBoundSource.run();
        else {
            var owner = mpsremodularized$nativeOwner();
            if (owner != null && mpsremodularized$nativeOwnerValid()) owner.save();
        }
    }
    @Inject(method="clicked",at=@At("HEAD"),cancellable=true)
    private void mps$guardClick(int slot, int button, ClickType type, Player player, CallbackInfo ci) {
        if (!mpsremodularized.common.compat.PortableStationIntegration.canClick((AbstractContainerMenu)(Object)this, slot, button, type)) ci.cancel();
    }
    @Inject(method={"clicked","broadcastChanges","removed"},at=@At("RETURN"))
    private void mps$saveAfterNative(CallbackInfo ci) { mpsremodularized$saveNativeSource(); }
}
