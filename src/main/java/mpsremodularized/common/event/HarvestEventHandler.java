package mpsremodularized.common.event;

import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule;
import mpsremodularized.common.capabilities.module.enhancement.IMiningEnhancementModule;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.capabilities.render.highlight.IHighlight;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HarvestEventHandler {
    @SubscribeEvent
    public static void handleHarvestCheck(PlayerEvent.HarvestCheck event) {
        Player player = event.getEntity();
        BlockState state = event.getTargetBlock();
        var nativeTool = mpsremodularized.common.compat.PaxelIntegration.find(player.getMainHandItem());
        if (nativeTool != null) {
            double energy = ElectricItemUtils.getPlayerEnergy(player);
            for (int i = 0; i < nativeTool.host().getSlots(); i++) {
                var module = nativeTool.host().getModuleCapability(nativeTool.host().getStackInSlot(i));
                if (nativeTool.host().isModuleOperational(module) && module instanceof mpsremodularized.common.capabilities.module.enchantment.IEnchantmentModule enchantment)
                    energy -= Math.max(0, enchantment.getEnergyUsage());
            }
            event.setCanHarvest(!state.requiresCorrectToolForDrops() || (energy >= 0 && nativeTool.source().isCorrectToolForDrops(state)));
            return;
        }

        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(player.getMainHandItem());
        if (mci != null) {
            if (!state.requiresCorrectToolForDrops()) {
                event.setCanHarvest(true);
                return;
            }

            event.setCanHarvest(false);
            double playerEnergy = ElectricItemUtils.getPlayerEnergy(player);
            for (int i = 0; i < mci.getSlots(); i++) {
                IPowerModule pm = mci.getModuleCapability(mci.getStackInSlot(i));
                if (mci.isModuleOperational(pm) && pm instanceof mpsremodularized.common.capabilities.module.enchantment.IEnchantmentModule enchantment) {
                    playerEnergy -= Math.max(0, enchantment.getEnergyUsage());
                }
            }
            for (int i = 0; i < mci.getSlots(); i++) {
                IPowerModule pm = mci.getModuleCapability(mci.getStackInSlot(i));
                if (mci.isModuleOperational(pm) && pm instanceof IBlockBreakingModule bbm
                    && playerEnergy >= bbm.getEnergyUsage() && bbm.getEmulatedTool().isCorrectToolForDrops(state)) {
                    event.setCanHarvest(true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (event.isCanceled() || mpsremodularized.common.capabilities.module.enhancement.MiningEnhancement.isHarvesting(player)) return;
        ItemStack tool = player.getMainHandItem();
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(tool);
        if (mci != null) {
            HitResult rayTraceResult = pick(player, 1F);
            if (rayTraceResult instanceof BlockHitResult blockHitResult) {
                event.setCanceled(mci.onBlockStartBreak(tool, blockHitResult, player, player.level()));
            }
        }
    }

    // copied from vanilla item
    protected static HitResult rayTrace(LevelAccessor level, Player player, ClipContext.Fluid fluidMode) {
        float pitch = player.xRotO;
        float yaw = player.getYRot();
        Vec3 vec3d = player.getEyePosition(1.0F);
        float f2 = Mth.cos(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = Mth.sin(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -Mth.cos(-pitch * ((float) Math.PI / 180F));
        float f5 = Mth.sin(-pitch * ((float) Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = player.blockInteractionRange();//.getAttribute(NeoForgeMod.ENTITY_REACH.get()).getValue();
        Vec3 vec3d1 = vec3d.add((double) f6 * d0, (double) f5 * d0, (double) f7 * d0);
        return level.clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.OUTLINE, fluidMode, player));
    }

    // Adapted from Minecraft's level renderer so that the highlighted block display matches what's actually being harvested
    private static HitResult pick(Player player, float partialTick) {
        double blockInteractionRange = player.blockInteractionRange();
        double entityInteractionRange = player.entityInteractionRange();
        double max = Math.max(blockInteractionRange, entityInteractionRange);
        double maxSquared = Mth.square(max);

        Vec3 eyePosition = player.getEyePosition(partialTick);
        HitResult hitresult = player.pick(max, partialTick, false);
        double distanceToEyeSqr = hitresult.getLocation().distanceToSqr(eyePosition);
        if (hitresult.getType() != HitResult.Type.MISS) {
            maxSquared = distanceToEyeSqr;
            max = Math.sqrt(distanceToEyeSqr);
        }

        Vec3 vec31 = player.getViewVector(partialTick);
        Vec3 vec32 = eyePosition.add(vec31.x * max, vec31.y * max, vec31.z * max);
        float f = 1.0F;
        AABB aabb = player.getBoundingBox().expandTowards(vec31.scale(max)).inflate(1.0, 1.0, 1.0);
        EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(
            player, eyePosition, vec32, aabb, p_234237_ -> !p_234237_.isSpectator() && p_234237_.isPickable(), maxSquared
        );
        return entityhitresult != null && entityhitresult.getLocation().distanceToSqr(eyePosition) < distanceToEyeSqr
            ? filterHitResult(entityhitresult, eyePosition, entityInteractionRange)
            : filterHitResult(hitresult, eyePosition, blockInteractionRange);
    }

    private static HitResult filterHitResult(HitResult pHitResult, Vec3 pPos, double blockInteractionRange) {
        Vec3 vec3 = pHitResult.getLocation();
        if (!vec3.closerThan(pPos, blockInteractionRange)) {
            Vec3 vec31 = pHitResult.getLocation();
            Direction direction = Direction.getNearest(vec31.x - pPos.x, vec31.y - pPos.y, vec31.z - pPos.z);
            return BlockHitResult.miss(vec31, direction, BlockPos.containing(vec31));
        } else {
            return pHitResult;
        }
    }

    /**
     * checks if a mining enhancement module is being used and does a speed adjustment based on a rough average for each
     * if no mining enhancement is used, then the speed adjustment falls back on the module that will break the block
     * <p>
     * TODO: overclock for mining enhancements?
     *
     * @param event
     */
    @SubscribeEvent
    public static void handleBreakSpeed(PlayerEvent.BreakSpeed event) {
        // Note: here we can actually get the position if needed. we can't easily om the harvest check.
        Player player = event.getEntity();
        Level level = player.level();
        ItemStack tool = player.getInventory().getSelected();
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(tool);
        double playerEnergy = ElectricItemUtils.getPlayerEnergy(player);
        NonNullList<IBlockBreakingModule> modules = NonNullList.create();
        float newSpeed = event.getNewSpeed();

        if (mci != null) {
            if (player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)) {
                double required = 0; boolean aqua = false;
                int toolCost = -1;
                for (int i = 0; i < mci.getSlots(); i++) {
                    var pm = mci.getModuleCapability(mci.getStackInSlot(i));
                    if (mci.isModuleOperational(pm) && pm instanceof mpsremodularized.common.capabilities.module.enchantment.IEnchantmentModule enchantment) {
                        required += Math.max(0, enchantment.getEnergyUsage());
                        aqua |= enchantment instanceof mpsremodularized.common.item.module.miningenchantment.AquaAffinityModule.TickingEnchantment;
                    }
                    if (toolCost < 0 && mpsremodularized.common.compat.PaxelIntegration.find(tool) == null && mci.isModuleOperational(pm) && pm instanceof IBlockBreakingModule toolModule
                        && (toolModule.getEmulatedTool().isCorrectToolForDrops(event.getState())
                            || (!event.getState().requiresCorrectToolForDrops() && toolModule.getEmulatedTool().getDestroySpeed(event.getState()) > 1))) toolCost = Math.max(0, toolModule.getEnergyUsage());
                }
                if (mpsremodularized.common.compat.PaxelIntegration.find(tool) != null) toolCost = 0;
                double submerged = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SUBMERGED_MINING_SPEED);
                if (aqua && toolCost >= 0 && playerEnergy >= required + toolCost && submerged > 0 && submerged < 1) event.setNewSpeed((float) (event.getNewSpeed() / submerged));
            }
            HitResult rayTraceResult = rayTrace(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (rayTraceResult instanceof BlockHitResult blockHitResult) {
                BlockState state = event.getState();
                BlockPos pos = event.getPosition().orElse(blockHitResult.getBlockPos()).immutable();

                // Reuse this list instead of making a new one
                modules.addAll(mpsremodularized.common.compat.PaxelIntegration.miningTools(tool));

                // If using a MingingEnhancement, account for multiple block breaking at once
                ItemStack module = mci.getActiveModule();

                IPowerModule moduleCap = mci.getModuleCapability(module);
                if (moduleCap instanceof IMiningEnhancementModule me && me.isModuleOnline()) {
                    HashMap<IHighlight.BlockPostionData, Integer> blockPositionsMap = me.getBlockPositions(tool,
                        blockHitResult,
                        player,
                        player.level(),
                        modules,
                        playerEnergy
                    );

                    if(!blockPositionsMap.isEmpty()) {
                        double overclock = me.applyPropertyModifiers(MPSConstants.HARVEST_SPEED);
                        Map<IBlockBreakingModule, List<BlockPos>> speedMap = new HashMap<>();
                        List<Double> correctedSpeeds = new ArrayList<>();

                        for (Map.Entry<IHighlight.BlockPostionData, Integer> entry : blockPositionsMap.entrySet()) {
                            IHighlight.BlockPostionData blockPostionData = entry.getKey();
                            int harvestLevel = entry.getValue();
                            if (blockPostionData.bbm() != null) {
                                if (blockPostionData.canHarvest()) {
                                    double harvestSpeed = blockPostionData.bbm().getEmulatedTool().getDestroySpeed(level.getBlockState(blockPostionData.pos()))
                                        * blockPostionData.bbm().applyPropertyModifiers(MPSConstants.HARVEST_SPEED);
                                    correctedSpeeds.add(harvestSpeed);
                                    List<BlockPos> tmpList = speedMap.getOrDefault(blockPostionData.bbm(), new ArrayList<>());
                                    tmpList.add(blockPostionData.pos());
                                    speedMap.put(blockPostionData.bbm(), tmpList);
                                }
                            }
                        }

                        double finalSpeed = (correctedSpeeds.stream().mapToDouble(Double::doubleValue).average().orElse(1.0) * 1.2); // slight boost

                        // Speed not changed yet, just debugging to see what new values look like
//                        MPSLogger.logDebug(
//                            "event old speed: " + event.getOriginalSpeed() + ", newSpeed: " + event.getNewSpeed() + ", speed to set: " + finalSpeed
//                                + ", finalSpeeds: " + correctedSpeeds);
//                        MPSLogger.logDebug("mining enhancement speed adjustment: " + moduleCap.applyPropertyModifiers(MPSConstants.HARVEST_SPEED));
                        //                        event.setNewSpeed((float) finalSpeed);
                        // Fixme testing
                        float baseSpeed = mci.getDestroySpeed(tool, state);
                        double penalty = baseSpeed > 0 ? event.getNewSpeed() / baseSpeed : 1;
                        event.setNewSpeed((float) (finalSpeed * overclock * penalty));
                        return;
                    }
                }

                boolean handled = false;

                // FIME: Pickaxe setting speed way too high
                for (IBlockBreakingModule bbm : modules) {
                    // should require this check?
                    if (bbm.canHarvestBlock(tool, state, player, pos, playerEnergy)) {
                        if (event.getNewSpeed() == 0) {
                            event.setNewSpeed(1);
                        }
                        handled = true;
                        bbm.handleBreakSpeed(event);
                        break;
                    }
                }
                if (!handled) {
                    if (state.requiresCorrectToolForDrops()) {
                        event.setNewSpeed(0);
                    } else {
                        event.setNewSpeed(1.0F);
                    }
                }
            }
        }
    }
}
