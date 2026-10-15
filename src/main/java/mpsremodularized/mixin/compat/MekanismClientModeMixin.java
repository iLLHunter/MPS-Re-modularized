package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.MekanismToolIntegration;
import mpsremodularized.common.compat.NativeToolIntegration;
import mpsremodularized.common.compat.NativeToolRequests;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo @Mixin(targets="mekanism.client.ClientTickHandler", remap=false)
public abstract class MekanismClientModeMixin {
    @Redirect(method="updateSelectorRenderer", at=@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$selected(LocalPlayer player, EquipmentSlot slot) { return MekanismToolIntegration.slot(player, slot); }
    @Redirect(method="onMouseEvent", at=@At(value="INVOKE", target="Lmekanism/common/network/PacketUtils;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)Z"))
    private boolean mps$request(CustomPacketPayload packet) {
        var player = Minecraft.getInstance().player;
        return NativeToolRequests.sendMode(player == null ? null : NativeToolIntegration.find(player, InteractionHand.MAIN_HAND, "mekanism:configurator"), packet);
    }
}
