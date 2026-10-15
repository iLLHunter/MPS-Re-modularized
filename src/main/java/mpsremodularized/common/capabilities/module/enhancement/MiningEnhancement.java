package mpsremodularized.common.capabilities.module.enhancement;

import mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.IRightClickModule;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.capabilities.module.enchantment.IEnchantmentModule;
import mpsremodularized.common.utils.ElectricItemUtils;

public class MiningEnhancement extends RightClickModule implements IRightClickModule, IMiningEnhancementModule {
    private static final ThreadLocal<Set<UUID>> HARVESTING = ThreadLocal.withInitial(HashSet::new);

    public static boolean isHarvesting(Player player) { return HARVESTING.get().contains(player.getUUID()); }

    protected NonNullList<IBlockBreakingModule> operationalTools(ItemStack stack) {
        return mpsremodularized.common.compat.PaxelIntegration.miningTools(stack);
    }

    protected int enchantmentCost(ItemStack stack) {
        var host = MPSCapabilities.getModeChangingModularItem(stack); int cost = 0;
        if (host != null) for (int i = 0; i < host.getSlots(); i++) {
            var module = host.getModuleCapability(host.getStackInSlot(i));
            if (host.isModuleOperational(module) && module instanceof IEnchantmentModule enchantment) cost += Math.max(0, enchantment.getEnergyUsage());
        }
        return cost;
    }

    /** The same full per-block budget drives highlighting and server transactions. */
    protected HashMap<BlockPostionData, Integer> planBlocks(ItemStack stack, Player player, Level level,
            Iterable<BlockPos> positions, double energy, boolean allowHandBlocks) {
        var plan = new LinkedHashMap<BlockPostionData, Integer>();
        var tools = operationalTools(stack); int extras = enchantmentCost(stack) + Math.max(0, getEnergyUsage());
        for (BlockPos position : positions) {
            var pos = position.immutable(); var state = level.getBlockState(pos);
            if (state.isAir() || state.getDestroySpeed(level, pos) < 0) continue;
            IBlockBreakingModule chosen = null;
            for (var tool : tools) if (tool.canHarvestBlock(stack, state, player, pos, energy - extras)) { chosen = tool; break; }
            if (chosen == null && allowHandBlocks && !state.requiresCorrectToolForDrops()) {
                for (var tool : tools) if (energy >= tool.getEnergyUsage() + extras
                    && (chosen == null || tool.getEnergyUsage() < chosen.getEnergyUsage())) chosen = tool;
            }
            boolean canHarvest = player.isCreative() || chosen != null;
            plan.put(new BlockPostionData(pos, canHarvest, chosen), canHarvest ? 2 : 0);
            if (canHarvest && !player.isCreative()) energy -= chosen.getEnergyUsage() + extras;
        }
        return plan;
    }

    /** Use the ordinary server break path so protection, loot, XP and tool costs run once. */
    protected boolean harvestBlocks(ItemStack stack, Player player, Level level, HashMap<BlockPostionData, Integer> plan) {
        if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide || player.isSpectator() || isHarvesting(player)) return false;
        boolean handled = false; var active = HARVESTING.get(); active.add(player.getUUID());
        try {
            for (var entry : plan.entrySet()) {
                var data = entry.getKey(); if (!data.canHarvest() || level.getBlockState(data.pos()).isAir()) continue;
                int extra = Math.max(0, getEnergyUsage());
                int cost = extra + enchantmentCost(stack) + (data.bbm() == null ? 0 : data.bbm().getEnergyUsage());
                if (!player.isCreative() && ElectricItemUtils.getPlayerEnergy(player) < cost) continue;
                if (data.bbm() != null && !data.bbm().isToolEffective(level, data.pos())) {
                    // Hand-breakable targets still fund the chosen tool and enchantments.
                    extra = cost;
                }
                // Reserve the enhancement charge while vanilla checks and funds the tool/enchantments.
                if (!player.isCreative()) ElectricItemUtils.drainPlayerEnergy(player, extra, false);
                if (serverPlayer.gameMode.destroyBlock(data.pos())) handled = true;
                else if (!player.isCreative()) ElectricItemUtils.giveEquippedPlayerEnergy(player, extra);
            }
        } finally { active.remove(player.getUUID()); if (active.isEmpty()) HARVESTING.remove(); }
        return handled;
    }
    public MiningEnhancement(ItemStack module, ModuleCategory category, ModuleTarget target) {
        super(module, category, target);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemStack, BlockHitResult hitResult, Player player, Level level) {
        return false;
    }

    @Override
    public int getEnergyUsage() {
        return 0;
    }


    @Override
    public HashMap<BlockPostionData, Integer> getBlockPositions(@NotNull ItemStack tool, @NotNull BlockHitResult result, @NotNull Player player, @NotNull Level level, NonNullList<IBlockBreakingModule> modules, double playerEnergy) {
        return new HashMap<>();
    }
}
