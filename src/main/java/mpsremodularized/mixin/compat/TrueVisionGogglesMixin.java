package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.TrueVisionIntegration;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.bay4lly.secretrooms.server.items.TrueVisionGogglesClientHandler", remap = false)
public abstract class TrueVisionGogglesMixin {
    @Inject(method = "isWearingGoggles", at = @At("HEAD"), cancellable = true)
    private static void mps$trueVision(Player player, CallbackInfoReturnable<Boolean> callback) {
        if (TrueVisionIntegration.hasOnlineGoggles(player)) callback.setReturnValue(true);
    }
}
