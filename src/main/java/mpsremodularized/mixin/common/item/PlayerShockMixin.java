package mpsremodularized.mixin.common.item;

import mpsremodularized.common.item.module.environmental.ShockModule;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.server.level.ServerPlayer.class)
public abstract class PlayerShockMixin {
    @Inject(method = "hurt", at = @At("HEAD"))
    private void mps$incomingMobHit(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
        ShockModule.onMobHit((Player) (Object) this, source, amount);
    }
}
