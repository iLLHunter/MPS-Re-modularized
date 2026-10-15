package mpsremodularized.common.item.module.miningenhancement;

import com.google.common.util.concurrent.AtomicDouble;
import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule;
import mpsremodularized.common.capabilities.module.enhancement.MiningEnhancement;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.IRightClickModule;
import mpsremodularized.common.capabilities.render.chameleon.IChameleon;
import mpsremodularized.common.capabilities.render.highlight.IHighlight;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.TagUtils;
import mpsremodularized.common.utils.block.CheckBlocksFrom;
import mpsremodularized.client.control.KeymappingKeyHandler;
import mpsremodularized.common.config.module.MiningEnhancementModuleConfig;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class SelectiveMiner extends AbstractPowerModule {
    public static class Enhancement extends MiningEnhancement implements IRightClickModule, IHighlight, IChameleon {
        public Enhancement(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MINING_ENHANCEMENT, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, MiningEnhancementModuleConfig.selectiveMinerModuleEnergyConsumptionBase, "FE");
            addBaseProperty(MPSConstants.HARVEST_SPEED, MiningEnhancementModuleConfig.selectiveMinerModuleHarvestSpeedBase, "x");
            addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.ENERGY_CONSUMPTION, MiningEnhancementModuleConfig.selectiveMinerModuleEnergyConsumptionOverclockMultiplier);
            addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.HARVEST_SPEED, MiningEnhancementModuleConfig.selectiveMinerModuleHarvestSpeedOverclockMultiplier);
            addBaseProperty(MPSConstants.SELECTIVE_MINER_LIMIT, 1);
            addIntTradeoffProperty(MPSConstants.SELECTIVE_MINER_LIMIT, MPSConstants.SELECTIVE_MINER_LIMIT, Math.max(0, MiningEnhancementModuleConfig.selectiveMinerModuleBlockLimit - 1), "Blocks", 1, 0);
        }

        @Override
        public BlockState getTargetBlockState() {
            return TagUtils.getModuleBlockState(getModule());
        }

        @Override
        public InteractionResultHolder<ItemStack> use(ItemStack itemStackIn, Level level, Player playerIn, InteractionHand hand) {
            if (level.isClientSide()) {
                if (KeymappingKeyHandler.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || KeymappingKeyHandler.isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                    BlockHitResult rayTraceResult = getPlayerPOVHitResult(level, playerIn, ClipContext.Fluid.NONE);
                    if ((rayTraceResult.getType() ==  HitResult.Type.BLOCK)) {
                        setTargetBlockStateByPos(rayTraceResult.getBlockPos());
                    } else if (rayTraceResult.getType() == HitResult.Type.MISS) {
                        setClearTargetBlockState();
                    }
                }
            }

            return super.use(itemStackIn, level, playerIn, hand);
        }

        @Override
        public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
            if(context.getPlayer() != null) {
                if (context.getHand().equals(InteractionHand.MAIN_HAND) && context.getLevel().isClientSide()) {
                    if (KeymappingKeyHandler.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || KeymappingKeyHandler.isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                        BlockHitResult rayTraceResult = getPlayerPOVHitResult(context.getLevel(), context.getPlayer(), ClipContext.Fluid.NONE);
                        if ((rayTraceResult.getType() ==  HitResult.Type.BLOCK)) {
                            setTargetBlockStateByPos(rayTraceResult.getBlockPos());
                        } else if (rayTraceResult.getType() == HitResult.Type.MISS) {
                            setClearTargetBlockState();
                        }
                    }
                }
            }
            return super.onItemUseFirst(stack, context);
        }

        @Override
        public boolean isAllowed() {
            return MiningEnhancementModuleConfig.selectiveMinerModuleIsAllowed;
        }

        // this only harvesting 1 type of block
        @Override
        public boolean onBlockStartBreak(ItemStack itemStack, BlockHitResult hitResult, Player player, Level level) {
            if (!isModuleOnline() || level.isClientSide || isHarvesting(player)) return false;
            return harvestBlocks(itemStack, player, level, getBlockPositions(itemStack, hitResult, player, level, operationalTools(itemStack), ElectricItemUtils.getPlayerEnergy(player)));
        }

        @Override
        public int getEnergyUsage() {
            return (int) applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
        }

        @Override
        public HashMap<BlockPostionData, Integer> getBlockPositions(@Nonnull ItemStack tool, @Nonnull BlockHitResult result,
                @Nonnull Player player, @Nonnull Level level, NonNullList<IBlockBreakingModule> modules, double playerEnergy) {
            var state = level.getBlockState(result.getBlockPos());
            if (state.isAir() || state != getTargetBlockState()) return new HashMap<>();
            int limit = Math.max(1, (int) applyPropertyModifiers(MPSConstants.SELECTIVE_MINER_LIMIT));
            return planBlocks(tool, player, level, new CheckBlocksFrom(result.getBlockPos(), limit).startCheck(level), playerEnergy, true);
        }
}
}
