package mpsremodularized.mixin.compat;

import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets = "com.lothrazar.library.gui.ContainerFlib", remap = false)
public abstract class CyclicPortableMenuMutationMixin {
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void mps$guard(Player player, int index, CallbackInfoReturnable<ItemStack> ci) {
        if (!((INativeToolMenu)this).mpsremodularized$nativeOwnerValid()) ci.setReturnValue(ItemStack.EMPTY);
    }
    @Inject(method = "quickMoveStack", at = @At("RETURN"))
    private void mps$save(CallbackInfoReturnable<ItemStack> ci) { ((INativeToolMenu)this).mpsremodularized$saveNativeSource(); }
}
