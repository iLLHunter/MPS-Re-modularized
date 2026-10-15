package mpsremodularized.mixin.common.item;

import mpsremodularized.common.item.module.environmental.BlockReachModule;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerBlockReachMixin {
    @Inject(method = "blockInteractionRange", at = @At("HEAD"), cancellable = true)
    private void mps$blockReach(CallbackInfoReturnable<Double> callback) {
        Player player = (Player) (Object) this;
        var module = BlockReachModule.online(player);
        if (module != null) callback.setReturnValue(module.reach(player));
    }
}
