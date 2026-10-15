package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeToolIntegration;
import mpsremodularized.common.compat.PortableStationIntegration;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo @Mixin(targets="com.lothrazar.cyclic.item.compass.ContainerGpsCompass", remap=false)
public abstract class CyclicCompassMenuMixin implements INativeToolMenu {
    @Shadow(remap=false) public ItemStack bag;
    @Unique private NativeToolIntegration.Installed mps$owner;
    @Redirect(method="<init>", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$source(Player player) {
        var owner = NativeToolIntegration.current();
        if (owner == null || owner.player() != player || !owner.item().donor.toString().equals("cyclic:compass_gps"))
            owner = NativeToolIntegration.find(player, InteractionHand.MAIN_HAND, "cyclic:compass_gps");
        return owner == null ? player.getMainHandItem() : owner.source();
    }
    @Inject(method="<init>", at=@At("RETURN"))
    private void mps$bind(int id, Inventory inventory, Player player, CallbackInfo ci) {
        var owner = NativeToolIntegration.find(player, InteractionHand.MAIN_HAND, "cyclic:compass_gps");
        if (owner != null && bag.is(owner.source().getItem())) {
            mps$owner = new NativeToolIntegration.Installed(player, owner.hand(), owner.fist(), owner.host(), owner.index(), owner.item(), owner.identity(), bag);
            PortableStationIntegration.protectHost((AbstractContainerMenu)(Object)this, mps$owner);
        }
    }
    @Override public NativeToolIntegration.Installed mpsremodularized$nativeOwner() { return mps$owner; }
    @Override public boolean mpsremodularized$nativeOwnerValid() { return mps$owner == null || mps$owner.valid(); }
    @Inject(method="stillValid", at=@At("HEAD"), cancellable=true)
    private void mps$valid(Player player, CallbackInfoReturnable<Boolean> ci) { if (mps$owner != null) ci.setReturnValue(player == mps$owner.player() && mps$owner.valid()); }
    @Inject(method="clicked", at=@At("HEAD"), cancellable=true)
    private void mps$guard(int slot, int button, net.minecraft.world.inventory.ClickType type, Player player, CallbackInfo ci) {
        if (mps$owner != null && (player != mps$owner.player() || !mps$owner.valid())) ci.cancel();
    }
    @Inject(method="clicked", at=@At("RETURN"))
    private void mps$save(int slot, int button, net.minecraft.world.inventory.ClickType type, Player player, CallbackInfo ci) { if (mps$owner != null) mps$owner.save(); }
}
