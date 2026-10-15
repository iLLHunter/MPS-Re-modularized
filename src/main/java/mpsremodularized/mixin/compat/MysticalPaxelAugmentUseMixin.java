package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.PaxelIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The inspected native path/till augment reads the held tool for its saved area offset. */
@Pseudo
@Mixin(targets = {"com.blakebr0.mysticalagriculture.augment.PathingAOEAugment",
        "com.blakebr0.mysticalagriculture.augment.TillingAOEAugment"}, remap = false)
public abstract class MysticalPaxelAugmentUseMixin {
    @Redirect(method = "onItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$sourceOffset(Player player) {
        return PaxelIntegration.nativeHeld(player, InteractionHand.MAIN_HAND);
    }
}
