package mpsremodularized.common.item.module.miningenhancement;

import com.google.common.util.concurrent.AtomicDouble;
import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule;
import mpsremodularized.common.capabilities.module.enhancement.MiningEnhancement;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.render.highlight.IHighlight;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.config.module.MiningEnhancementModuleConfig;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TunnelBoreModule extends AbstractPowerModule {
    public static class Enhancement extends MiningEnhancement implements IHighlight {
        public Enhancement(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MINING_ENHANCEMENT, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, MiningEnhancementModuleConfig.tunnelBoreModuleEnergyConsumptionBase, "FE/Block");
            addBaseProperty(MPSConstants.HARVEST_SPEED, MiningEnhancementModuleConfig.tunnelBoreModuleHarvestSpeedBase, "x");
            addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.ENERGY_CONSUMPTION, MiningEnhancementModuleConfig.tunnelBoreModuleEnergyConsumptionOverclockMultiplier);
            addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.HARVEST_SPEED, MiningEnhancementModuleConfig.tunnelBoreModuleHarvestSpeedOverclockMultiplier);
            addIntTradeoffProperty(MPSConstants.DIAMETER, MPSConstants.MINING_DIAMETER, MiningEnhancementModuleConfig.tunnelBoreModuleMaxDiameter, "m", 2, 1);
        }

        @Override
        public boolean isAllowed() {
            return MiningEnhancementModuleConfig.tunnelBoreModuleIsAllowed;
        }

        /**
         * @param itemStack
         * @param hitResult
         * @param player
         * @param level
         * @return true to cancel, false to not
         */
        @Override
        public boolean onBlockStartBreak(ItemStack itemStack, BlockHitResult hitResult, Player player, Level level) {
            int radius = ((int) applyPropertyModifiers(MPSConstants.MINING_DIAMETER) - 1) / 2;
            if (!isModuleOnline() || level.isClientSide || isHarvesting(player) || radius <= 0) return false;
            return harvestBlocks(itemStack, player, level, getBlockPositions(itemStack, hitResult, player, level, operationalTools(itemStack), ElectricItemUtils.getPlayerEnergy(player)));
        }

        @Override
        public int getEnergyUsage() {
            return (int) applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
        }

        @Override
        public HashMap<BlockPostionData, Integer> getBlockPositions(@Nonnull ItemStack tool, @Nonnull BlockHitResult result,
                @Nonnull Player player, @Nonnull Level level, NonNullList<IBlockBreakingModule> modules, double playerEnergy) {
            BlockPos pos = result.getBlockPos();
            int radius = Math.max(0, ((int) applyPropertyModifiers(MPSConstants.MINING_DIAMETER) - 1) / 2);
            Stream<BlockPos> positions = switch (result.getDirection()) {
                case UP, DOWN -> BlockPos.betweenClosedStream(pos.north(radius).west(radius), pos.south(radius).east(radius));
                case EAST, WEST -> BlockPos.betweenClosedStream(pos.above(radius).north(radius), pos.below(radius).south(radius));
                case NORTH, SOUTH -> BlockPos.betweenClosedStream(pos.above(radius).west(radius), pos.below(radius).east(radius));
            };
            var ordered = positions.map(BlockPos::immutable).sorted(java.util.Comparator.comparingDouble(pos::distSqr)).toList();
            return planBlocks(tool, player, level, ordered, playerEnergy, false);
        }
    }
}
