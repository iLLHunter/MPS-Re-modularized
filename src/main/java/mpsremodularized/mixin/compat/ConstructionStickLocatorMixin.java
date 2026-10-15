package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.ConstructionStickIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets = "mrbysco.constructionstick.basics.StickUtil", remap = false)
public abstract class ConstructionStickLocatorMixin {
    @Inject(method = "holdingStick", at = @At("HEAD"), cancellable = true)
    private static void mps$selected(Player player, CallbackInfoReturnable<ItemStack> ci) {
        var owner = ConstructionStickIntegration.selected(player);
        if (owner != null) ci.setReturnValue(owner.source());
    }
}
