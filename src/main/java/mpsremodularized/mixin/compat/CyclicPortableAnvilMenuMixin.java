package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.PoweredDiamondAnvilIntegration;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets = "com.lothrazar.cyclic.block.anvil.ContainerAnvil", remap = false)
public abstract class CyclicPortableAnvilMenuMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private BlockEntity mps$ownedTile(Level level, BlockPos pos) { return PoweredDiamondAnvilIntegration.openingTile(level, pos); }
    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void mps$valid(Player player, CallbackInfoReturnable<Boolean> ci) {
        var menu = (INativeToolMenu)this;
        if (menu.mpsremodularized$nativeOwner() != null) ci.setReturnValue(player == menu.mpsremodularized$nativeOwner().player() && menu.mpsremodularized$nativeOwnerValid());
    }
}
