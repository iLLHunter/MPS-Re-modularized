package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.PaxelIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.blakebr0.mysticalagradditions.item.EssencePaxelItem", remap = false)
public abstract class EssencePaxelMixin {
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$source(Player player, InteractionHand hand) { return PaxelIntegration.nativeHeld(player, hand); }
}
