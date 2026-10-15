package mpsremodularized.common.blockentity;

import mpsremodularized.common.registration.MPSBlockEntities;

import mpsremodularized.common.registration.MPSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TinkerTableBlockEntity extends BlockEntity {
    public TinkerTableBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(MPSBlockEntities.TINKER_TABLE_BLOCKENTITY_TYPE.get(), pPos, pBlockState);
    }
}
