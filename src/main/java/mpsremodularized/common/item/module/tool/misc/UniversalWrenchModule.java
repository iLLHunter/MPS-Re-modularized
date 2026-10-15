package mpsremodularized.common.item.module.tool.misc;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import mpsremodularized.common.compat.WrenchCompatibility;
import mpsremodularized.common.config.module.ToolModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.CommonHooks;

/** The item is only an installable module; all wrench behavior belongs to the selected fist mode. */
public class UniversalWrenchModule extends AbstractPowerModule {
    public static class WrenchCapability extends RightClickModule {
        public WrenchCapability(ItemStack module) {
            super(module, ModuleCategory.TOOL, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, ToolModuleConfig.universalWrenchEnergyConsumption, "FE");
        }

        @Override
        public boolean isAllowed() {
            return ToolModuleConfig.universalWrenchModuleIsAllowed;
        }

        @Override
        public boolean isModuleOnline() {
            return TagUtils.getModuleIsOnline(getModule());
        }

        @Override
        public int getEnergyUsage() {
            return (int) applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
        }

        @Override
        public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
            return useOn(context);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            var player = context.getPlayer();
            var host = MPSCapabilities.getModeChangingModularItem(context.getItemInHand());
            if (player == null || context.getHand() != InteractionHand.MAIN_HAND || host == null
                    || !host.hasActiveModule(MPSConstants.UNIVERSAL_WRENCH_MODULE)
                    || !host.isModuleOperational(this)) return InteractionResult.PASS;
            BlockPos pos = context.getClickedPos();
            var level = context.getLevel();
            if (!player.mayBuild() || !level.mayInteract(player, pos)
                    || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) {
                return InteractionResult.FAIL;
            }
            if (player.getCooldowns().isOnCooldown(context.getItemInHand().getItem())) return InteractionResult.FAIL;
            int cost = getEnergyUsage();
            if (ElectricItemUtils.getPlayerEnergy(player) < cost) return InteractionResult.FAIL;

            BlockState state = level.getBlockState(pos);
            ItemStack nativeWrench = WrenchCompatibility.findWrench(state, player.isShiftKeyDown());
            if (!nativeWrench.isEmpty()) {
                // Execute the native transaction, including its interaction events, security and drops.
                // The temporary native item never enters storage and the exact fist is restored on every exit.
                if (level.isClientSide()) return InteractionResult.SUCCESS;
                if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
                ItemStack fist = player.getItemInHand(context.getHand());
                var previousMenu = player.containerMenu;
                InteractionResult result;
                player.setItemInHand(context.getHand(), nativeWrench);
                try {
                    var hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside());
                    if (BuiltInRegistries.ITEM.getKey(nativeWrench.getItem()).equals(ResourceLocation.parse("oritech:wrench"))) {
                        // Oritech implements its wrench in Item.use with a fresh native ray trace.
                        // Keep both block protection and the ray-traced target inside this transaction.
                        var target = rayTrace(level, player, ClipContext.Fluid.NONE, player.blockInteractionRange());
                        if (target.getType() != HitResult.Type.BLOCK || !target.getBlockPos().equals(pos)) return InteractionResult.FAIL;
                        var event = CommonHooks.onRightClickBlock(player, context.getHand(), pos, hit);
                        result = event.isCanceled() ? event.getCancellationResult()
                                : event.getUseItem().isFalse() ? InteractionResult.FAIL
                                : nativeWrench.use(level, player, context.getHand()).getResult();
                    } else {
                        result = serverPlayer.gameMode.useItemOn(serverPlayer, level, nativeWrench, context.getHand(), hit);
                    }
                } finally {
                    player.setItemInHand(context.getHand(), fist);
                }
                if (result.consumesAction() && player.containerMenu == previousMenu) {
                    ElectricItemUtils.drainPlayerEnergy(player, cost, false);
                }
                // Do not repeat a native PASS through useOn after onItemUseFirst already tried it.
                return result == InteractionResult.PASS ? InteractionResult.FAIL : result;
            }

            BlockState rotated = rotateVanilla(state, player.isShiftKeyDown());
            if (rotated == state) return InteractionResult.PASS;
            if (!level.isClientSide()) {
                if (!level.setBlock(pos, rotated, 3)) return InteractionResult.FAIL;
                ElectricItemUtils.drainPlayerEnergy(player, cost, false);
                level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.35F, 1.4F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
    }

    /** Narrow standalone fallback: never dismantle or change a foreign machine's state directly. */
    static BlockState rotateVanilla(BlockState state, boolean reverse) {
        if (!BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals("minecraft")) return state;
        if (state.getBlock() instanceof RotatedPillarBlock) {
            var axes = Direction.Axis.values();
            int index = state.getValue(BlockStateProperties.AXIS).ordinal();
            return state.setValue(BlockStateProperties.AXIS, axes[Math.floorMod(index + (reverse ? -1 : 1), axes.length)]);
        }
        if (state.hasProperty(BlockStateProperties.EXTENDED) && state.getValue(BlockStateProperties.EXTENDED)) return state;
        if (state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER)
                || state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN)) {
            Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, reverse ? direction.getCounterClockWise() : direction.getClockWise());
        }
        if (state.is(Blocks.DISPENSER) || state.is(Blocks.DROPPER) || state.is(Blocks.OBSERVER)
                || state.is(Blocks.PISTON) || state.is(Blocks.STICKY_PISTON)) {
            var directions = Direction.values();
            int index = state.getValue(BlockStateProperties.FACING).ordinal();
            return state.setValue(BlockStateProperties.FACING, directions[Math.floorMod(index + (reverse ? -1 : 1), directions.length)]);
        }
        return state;
    }
}
