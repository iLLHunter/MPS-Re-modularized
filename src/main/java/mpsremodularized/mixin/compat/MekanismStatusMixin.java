package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.MekanismToolIntegration;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo @Mixin(targets="mekanism.client.render.hud.MekanismStatusOverlay", remap=false)
public abstract class MekanismStatusMixin {
    @Redirect(method="render", at=@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$status(LocalPlayer player) { return MekanismToolIntegration.mainHand(player); }
}
