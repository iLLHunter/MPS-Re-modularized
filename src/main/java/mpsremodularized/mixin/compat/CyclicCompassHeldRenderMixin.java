package mpsremodularized.mixin.compat;

import com.llamalad7.mixinextras.sugar.Local;
import mpsremodularized.common.compat.NativeToolIntegration;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Show the selected compass while retaining the Fist as the real held item. */
@Mixin(ItemInHandRenderer.class)
public abstract class CyclicCompassHeldRenderMixin {
    @ModifyArg(method = "renderArmWithItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"), index = 1)
    private ItemStack mps$compass(ItemStack held, @Local(argsOnly = true) AbstractClientPlayer player,
                                  @Local(argsOnly = true) InteractionHand hand) {
        return NativeToolIntegration.renderedSource(player, hand, held, "cyclic:compass_gps");
    }
}
