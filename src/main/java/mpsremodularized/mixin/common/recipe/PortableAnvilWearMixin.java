package mpsremodularized.mixin.common.recipe;

import mpsremodularized.common.compat.PortableStationIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilMenu.class)
public abstract class PortableAnvilWearMixin {
    @Redirect(method = "lambda$onTake$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState mps$state(Level level, BlockPos pos) { return PortableStationIntegration.anvilState(level, pos); }
    @Redirect(method = "lambda$onTake$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static boolean mps$damage(Level level, BlockPos pos, BlockState state, int flags) { return PortableStationIntegration.damageAnvil(level, pos, state, flags); }
    @Redirect(method = "lambda$onTake$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private static boolean mps$break(Level level, BlockPos pos, boolean moving) { return PortableStationIntegration.breakAnvil(level, pos, moving); }
}
