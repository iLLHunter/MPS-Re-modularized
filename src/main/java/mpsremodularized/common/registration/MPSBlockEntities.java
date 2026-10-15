package mpsremodularized.common.registration;

import java.util.function.Supplier;
import mpsremodularized.common.block.ChargingBase;
import mpsremodularized.common.block.LuxCapacitorBlock;
import mpsremodularized.common.block.TinkerTable;
import mpsremodularized.common.blockentity.ChargingBaseBlockEntity;
import mpsremodularized.common.blockentity.LuxCapacitorBlockEntity;
import mpsremodularized.common.blockentity.TinkerTableBlockEntity;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MPSBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MPSConstants.MOD_ID);
    public static final Supplier<BlockEntityType<ChargingBaseBlockEntity>> CHARGING_BASE_BLOCK_ENTITY = BLOCKENTITY_TYPES.register(MPSConstants.CHARGING_BASE_REGNAME,
        () -> BlockEntityType.Builder.of(ChargingBaseBlockEntity::new, MPSBlocks.CHARGING_BASE_BLOCK.get()).build(null));

    public static final Supplier<BlockEntityType<TinkerTableBlockEntity>> TINKER_TABLE_BLOCKENTITY_TYPE = BLOCKENTITY_TYPES.register(MPSConstants.TINKER_TABLE.getPath(),
        () -> BlockEntityType.Builder.of(TinkerTableBlockEntity::new, MPSBlocks.TINKER_TABLE_BLOCK.get()).build(null));

    public static final Supplier<BlockEntityType<LuxCapacitorBlockEntity>> LUX_CAP_BLOCK_ENTITY_TYPE = BLOCKENTITY_TYPES.register(MPSConstants.LUX_CAPACITOR.getPath(),
        () -> BlockEntityType.Builder.of(LuxCapacitorBlockEntity::new, MPSBlocks.LUX_CAPACITOR_BLOCK.get()).build(null));
}
