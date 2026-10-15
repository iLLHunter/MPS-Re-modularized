package mpsremodularized.mixin.compat;

import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets = "blusunrize.immersiveengineering.common.gui.IEBaseContainerOld", remap = false)
public abstract class ImmersiveWorkbenchValidityMixin {
    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void mps$valid(Player player, CallbackInfoReturnable<Boolean> ci) {
        var binding = (INativeToolMenu)this;
        if (binding.mpsremodularized$nativeOwner() != null) ci.setReturnValue(binding.mpsremodularized$nativeOwnerValid());
    }
}
