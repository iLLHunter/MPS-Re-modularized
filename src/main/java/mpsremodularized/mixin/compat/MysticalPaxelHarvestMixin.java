package mpsremodularized.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mpsremodularized.common.compat.PaxelIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/** Native area harvest keeps its rules while receiving the same stacked loot and costs as a single break. */
@Pseudo
@Mixin(targets = "com.blakebr0.cucumber.helper.BlockHelper", remap = false)
public abstract class MysticalPaxelHarvestMixin {
    private static final String HARVEST = "harvestBlock(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Z)Z";

    @WrapOperation(method = HARVEST, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/item/ItemStack;)V"))
    private static void mps$stackedDrops(Block block, Level level, Player player, BlockPos pos, BlockState state,
                                        BlockEntity blockEntity, ItemStack source, Operation<Void> original) {
        var fist = PaxelIntegration.mysticalFistForSource(player, source);
        original.call(block, level, player, pos, state, blockEntity,
                fist.isEmpty() ? source : PaxelIntegration.lootTool(fist, level));
    }

    @WrapOperation(method = HARVEST, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;mineBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"))
    private static void mps$nativeMiningCost(ItemStack source, Level level, BlockState state, BlockPos pos,
                                           Player player, Operation<Void> original) {
        var fist = PaxelIntegration.mysticalFistForSource(player, source);
        original.call(fist.isEmpty() ? source : fist, level, state, pos, player);
    }

    @WrapOperation(method = HARVEST, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getExpDrop(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)I"))
    private static int mps$stackedExperience(BlockState state, LevelAccessor level, BlockPos pos,
                                            BlockEntity blockEntity, Entity entity, ItemStack source, Operation<Integer> original) {
        var fist = entity instanceof Player player ? PaxelIntegration.mysticalFistForSource(player, source) : ItemStack.EMPTY;
        return original.call(state, level, pos, blockEntity, entity,
                fist.isEmpty() ? source : PaxelIntegration.lootTool(fist, entity.level()));
    }
}
