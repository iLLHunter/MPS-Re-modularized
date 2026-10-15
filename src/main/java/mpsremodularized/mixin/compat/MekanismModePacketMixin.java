package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeToolIntegration;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo @Mixin(targets={"mekanism.common.network.to_server.PacketModeChange", "mekanism.common.network.to_server.PacketRadialModeChange"}, remap=false)
public abstract class MekanismModePacketMixin {
    @Redirect(method="handle", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$source(Player player, EquipmentSlot slot) { return NativeToolIntegration.slot(player, slot); }
}
