package mpsremodularized.common.registration;

import java.util.function.Supplier;
import mpsremodularized.common.block.ChargingBase;
import mpsremodularized.common.block.LuxCapacitorBlock;
import mpsremodularized.common.block.TinkerTable;
import mpsremodularized.common.blockentity.LuxCapacitorBlockEntity;
import mpsremodularized.common.blockentity.TinkerTableBlockEntity;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MPSBlocks {
    /**
     * Blocks -------------------------------------------------------------------------------------
     */
    public static final DeferredRegister<Block> MPS_BLOCKS = DeferredRegister.createBlocks(MPSConstants.MOD_ID);

    public static final DeferredHolder<Block, TinkerTable> TINKER_TABLE_BLOCK = MPS_BLOCKS.register(MPSConstants.TINKER_TABLE.getPath(), TinkerTable::new);

    public static final DeferredHolder<Block, LuxCapacitorBlock> LUX_CAPACITOR_BLOCK = MPS_BLOCKS.register(MPSConstants.LUX_CAPACITOR.getPath(), LuxCapacitorBlock::new);

    public static final DeferredHolder<Block, ChargingBase> CHARGING_BASE_BLOCK = MPS_BLOCKS.register(MPSConstants.CHARGING_BASE_REGNAME, ChargingBase::new);
}
