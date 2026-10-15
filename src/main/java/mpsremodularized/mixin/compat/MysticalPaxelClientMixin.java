package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.PaxelIntegration;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Native AOE key controls and highlighting inspect the contained tool without replacing the Fist. */
@Pseudo
@Mixin(targets = "com.blakebr0.mysticalagriculture.client.handler.AOEAugmentClientHandler", remap = false)
public abstract class MysticalPaxelClientMixin {
    @Redirect(method = "onKeyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$inputSource(LocalPlayer player) { return PaxelIntegration.mysticalAoeTool(player); }

    @Redirect(method = "onBlockHighlight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$highlightSource(Player player) { return PaxelIntegration.mysticalAoeTool(player); }
}
