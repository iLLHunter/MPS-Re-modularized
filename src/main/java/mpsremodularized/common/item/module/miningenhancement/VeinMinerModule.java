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
import mpsremodularized.common.utils.block.CheckOreBlocksFrom;
import mpsremodularized.common.config.module.MiningEnhancementModuleConfig;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class VeinMinerModule extends AbstractPowerModule {
    static final TagKey<Block> ORE_TAG = Tags.Blocks.ORES;

    public static class Enhancement extends MiningEnhancement implements IHighlight {

        public Enhancement(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MINING_ENHANCEMENT, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, MiningEnhancementModuleConfig.veinMinerModuleEnergyConsumptionBase, "FE");
            addBaseProperty(MPSConstants.HARVEST_SPEED, MiningEnhancementModuleConfig.veinMinerModuleHarvestSpeedBase, "x");
            addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.ENERGY_CONSUMPTION, MiningEnhancementModuleConfig.veinMinerModuleEnergyConsumptionOverclockMultiplier);
            addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.HARVEST_SPEED, MiningEnhancementModuleConfig.veinMinerModuleHarvestSpeedOverclockMultiplier);
            addBaseProperty(MPSConstants.SELECTIVE_MINER_LIMIT, 1);
            addIntTradeoffProperty(MPSConstants.SELECTIVE_MINER_LIMIT, MPSConstants.SELECTIVE_MINER_LIMIT, Math.max(0, MiningEnhancementModuleConfig.veinMinerModuleBlockLimit - 1), "Blocks", 1, 0);
        }

        @Override
        public boolean isAllowed() {
            return MiningEnhancementModuleConfig.veinMinerModuleIsAllowed;
        }

        @Override
        public int getEnergyUsage() {
            return (int) applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
        }

        @Override
        public boolean onBlockStartBreak(ItemStack itemStack, BlockHitResult hitResult, Player player, Level level) {
            if (!isModuleOnline() || level.isClientSide || isHarvesting(player)) return false;
            return harvestBlocks(itemStack, player, level, getBlockPositions(itemStack, hitResult, player, level, operationalTools(itemStack), ElectricItemUtils.getPlayerEnergy(player)));
        }

        @Override
        public HashMap<BlockPostionData, Integer> getBlockPositions(@NotNull ItemStack tool, @NotNull BlockHitResult result,
                @NotNull Player player, @NotNull Level level, NonNullList<IBlockBreakingModule> modules, double playerEnergy) {
            var state = level.getBlockState(result.getBlockPos());
            if (state.isAir() || !state.is(ORE_TAG)) return new HashMap<>();
            int limit = Math.max(1, (int) applyPropertyModifiers(MPSConstants.SELECTIVE_MINER_LIMIT));
            return planBlocks(tool, player, level, new CheckOreBlocksFrom(result.getBlockPos(), limit).startCheck(level), playerEnergy, true);
        }
}
}
