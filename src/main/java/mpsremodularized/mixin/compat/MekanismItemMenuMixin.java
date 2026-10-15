package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeToolIntegration;
import mpsremodularized.common.compat.PortableStationIntegration;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets="mekanism.common.inventory.container.item.MekanismItemContainer", remap=false)
public abstract class MekanismItemMenuMixin implements INativeToolMenu {
    @Unique private NativeToolIntegration.Installed mps$owner;
    @Inject(method="<init>", at=@At("RETURN"))
    private void mps$bind(@Coerce Object type, int id, Inventory inventory, InteractionHand hand, ItemStack source, CallbackInfo ci) {
        if (hand != InteractionHand.MAIN_HAND) return;
        var owner = NativeToolIntegration.find(inventory.player, hand, null);
        if (owner != null && owner.item().provider == mpsremodularized.common.compat.CompatibleMod.MEKANISM && source.is(owner.source().getItem())) {
            mps$owner = new NativeToolIntegration.Installed(owner.player(), owner.hand(), owner.fist(), owner.host(), owner.index(), owner.item(), owner.identity(), source);
            PortableStationIntegration.protectHost((AbstractContainerMenu)(Object)this, mps$owner);
        }
    }
    @Override public NativeToolIntegration.Installed mpsremodularized$nativeOwner() { return mps$owner; }
    @Override public boolean mpsremodularized$nativeOwnerValid() { return mps$owner == null || mps$owner.valid() && mpsremodularized.common.compat.MekanismToolIntegration.canAccess(mps$owner.player(), mps$owner.source()); }
    @Inject(method="stillValid", at=@At("HEAD"), cancellable=true)
    private void mps$valid(Player player, CallbackInfoReturnable<Boolean> ci) { if (mps$owner != null) ci.setReturnValue(player == mps$owner.player() && mpsremodularized$nativeOwnerValid()); }
}
