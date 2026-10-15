package mpsremodularized.mixin.compat;

import java.util.function.Supplier;
import mpsremodularized.common.compat.MekanismToolIntegration;
import mpsremodularized.common.compat.NativeToolIntegration;
import mpsremodularized.common.compat.NativeToolRequests;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets="mekanism.client.gui.GuiRadialSelector", remap=false)
public abstract class MekanismRadialMixin {
    @Unique private NativeToolIntegration.Installed mps$owner;
    @Inject(method="<init>", at=@At("RETURN"))
    private void mps$bind(EquipmentSlot slot, @Coerce Object data, Supplier<Player> player, CallbackInfo ci) {
        if (slot == EquipmentSlot.MAINHAND && player.get() != null) mps$owner = NativeToolIntegration.find(player.get(), InteractionHand.MAIN_HAND, "mekanism:configurator");
    }
    @Inject(method="hasMatchingData", at=@At("HEAD"), cancellable=true)
    private void mps$stale(EquipmentSlot slot, @Coerce Object data, CallbackInfoReturnable<Boolean> ci) {
        if (mps$owner != null && !mps$owner.valid()) ci.setReturnValue(false);
    }
    @Redirect(method="getCurrent", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$current(Player player, EquipmentSlot slot) {
        return mps$owner != null && !mps$owner.valid() ? ItemStack.EMPTY : MekanismToolIntegration.slot(player, slot);
    }
    @Redirect(method="updateSelection", at=@At(value="INVOKE", target="Lmekanism/common/network/PacketUtils;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)Z"))
    private boolean mps$request(CustomPacketPayload packet) { return NativeToolRequests.sendMode(mps$owner, packet); }
}
