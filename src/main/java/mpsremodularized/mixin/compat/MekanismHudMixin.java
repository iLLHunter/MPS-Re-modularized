package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.MekanismToolIntegration;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo @Mixin(targets={"mekanism.client.render.hud.MekanismHUD", "mekanism.client.render.RenderTickHandler"}, remap=false)
public abstract class MekanismHudMixin {
    @Redirect(method={"render", "onBlockHover"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"), require=0)
    private ItemStack mps$hud(Player player, EquipmentSlot slot) { return MekanismToolIntegration.slot(player, slot); }
    @Redirect(method={"render", "onBlockHover"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"), require=0)
    private ItemStack mps$highlight(Player player) { return MekanismToolIntegration.mainHand(player); }
}
