package mpsremodularized.common.item.module.movement;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.HeatUtils;
import mpsremodularized.common.config.module.MovementModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;

/**
 * Created by Eximius88 on 2/3/14.
 */
public class DimensionalRiftModule extends AbstractPowerModule {
    private static final int SEARCH_RADIUS = 16;
    public static class RightClickie extends RightClickModule {
        public RightClickie(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MOVEMENT, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.HEAT_GENERATION, MovementModuleConfig.dimensionalRiftModuleHeatGenerationBase);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, MovementModuleConfig.dimensionalRiftModuleEnergyConsumptionBase);
        }

        @Override
        public boolean isAllowed() {
            return MovementModuleConfig.dimensionalRiftModuleIsAllowed;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(@Nonnull ItemStack itemStackIn, Level worldIn, Player playerIn, InteractionHand hand) {
            if (worldIn.isClientSide) {
                return InteractionResultHolder.success(itemStackIn);
            }
            if (playerIn.getCooldowns().isOnCooldown(itemStackIn.getItem())) {
                return InteractionResultHolder.fail(itemStackIn);
            }
            if (playerIn instanceof ServerPlayer && !playerIn.isPassenger() && !playerIn.isVehicle() && !playerIn.level().isClientSide()) {
                Level level ;
                if (playerIn.level().dimension().location().equals(Level.NETHER.location())) {
                    level = playerIn.getServer().getLevel(Level.OVERWORLD);
                } else if (playerIn.level().dimension().location().equals(Level.OVERWORLD.location())) {
                    level = playerIn.getServer().getLevel(Level.NETHER);
                } else {
                    level = null;
                }

                if (level != null && playerIn.getServer().isLevelEnabled((ServerLevel) level) && playerIn.canChangeDimensions(worldIn, level)) {
                    BlockPos coords = playerIn.blockPosition();
                    double energyConsumption = (int) applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
                    double playerEnergy = ElectricItemUtils.getPlayerEnergy(playerIn);
                    if (playerEnergy >= energyConsumption) {
                        Optional<BlockPos> targetPos = findSafeLocation(coords, (ServerLevel) level, playerIn);
                        if (targetPos.isPresent()) {

                            DimensionTransition transition = new DimensionTransition((ServerLevel) level,
                                targetPos.get().getBottomCenter(),
                                Vec3.ZERO,
                                playerIn.getYRot(),
                                playerIn.getXRot(),
                                false,
                                DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));

                            if (playerIn.changeDimension(transition) == null) {
                                return InteractionResultHolder.fail(itemStackIn);
                            }
                            ElectricItemUtils.drainPlayerEnergy(playerIn, getEnergyUsage(), false);
                            HeatUtils.heatPlayer(playerIn, applyPropertyModifiers(MPSConstants.HEAT_GENERATION));
                            playerIn.resetFallDistance();
                            playerIn.getCooldowns().addCooldown(itemStackIn.getItem(), 20);
                            return InteractionResultHolder.success(itemStackIn);
                        }
                        return fail(itemStackIn, playerIn, "unsafe");
                    }
                    return fail(itemStackIn, playerIn, "energy");
                }
                return fail(itemStackIn, playerIn, "dimension");
            }
            return fail(itemStackIn, playerIn, "mounted");
        }

        private InteractionResultHolder<ItemStack> fail(ItemStack fist, Player player, String reason) {
            player.displayClientMessage(Component.translatable("message.mpsremodularized.rift." + reason), true);
            return InteractionResultHolder.fail(fist);
        }

        @Override
        public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
            return context.getPlayer() == null ? InteractionResult.PASS
                    : use(stack, context.getLevel(), context.getPlayer(), context.getHand()).getResult();
        }

        @Override
        public int getEnergyUsage() {
            return (int) applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
        }
    }

    public static Optional<BlockPos> findSafeLocation(BlockPos targetPos, ServerLevel level, Player player) {
        int minimumY = level.getMinBuildHeight();
        int maximumY = Math.min(level.getMaxBuildHeight(), minimumY + level.getLogicalHeight());
        WorldBorder border = level.getWorldBorder();
        return searchDestination(targetPos, minimumY, maximumY,
                (x, z) -> border.isWithinBounds(new BlockPos(x, minimumY, z))
                        // Level.getHeight returns minimumY for an unloaded chunk. Load each
                        // destination chunk before reading its heightmap (at most nine chunks).
                        ? level.getChunk(x >> 4, z >> 4).getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15) + 1 : minimumY,
                pos -> canTeleportTo(level, pos, player, minimumY, maximumY));
    }

    /** Bounded search; an exhausted search never invents an unchecked fallback. */
    public static Optional<BlockPos> searchDestination(BlockPos targetPos, int minimumY, int maximumY,
            ToIntBiFunction<Integer, Integer> columnHeight, Predicate<BlockPos> isSafe) {
        if (minimumY >= maximumY) {
            return Optional.empty();
        }
        for (BlockPos column : BlockPos.spiralAround(targetPos, SEARCH_RADIUS, Direction.EAST, Direction.SOUTH)) {
            int startY = Math.min(maximumY - 1, columnHeight.applyAsInt(column.getX(), column.getZ()));
            for (int y = startY; y > minimumY; y--) {
                BlockPos candidate = new BlockPos(column.getX(), y, column.getZ());
                if (isSafe.test(candidate)) {
                    return Optional.of(candidate);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean canTeleportTo(ServerLevel level, BlockPos pos, Player player, int minimumY, int maximumY) {
        AABB bounds = destinationBounds(player.getBoundingBox(), player.position(), pos);
        return isSafeDestination(level, player, pos, bounds, minimumY, maximumY);
    }

    public static boolean isSafeDestination(CollisionGetter level, Entity player, BlockPos pos,
            AABB bounds, int minimumY, int maximumY) {
        if (!isWithinBuildBounds(bounds, minimumY, maximumY)
                || !level.getWorldBorder().isWithinBounds(bounds)) {
            return false;
        }
        BlockPos floor = pos.below();
        return isLandingSurface(level.getBlockState(floor), level, floor)
                && level.noCollision(player, bounds)
                && !containsHazards(level, bounds);
    }

    public static AABB destinationBounds(AABB bounds, Vec3 origin, BlockPos target) {
        return bounds.move(target.getBottomCenter().subtract(origin));
    }

    public static boolean isWithinBuildBounds(AABB bounds, int minimumY, int maximumY) {
        return bounds.minY > minimumY && bounds.maxY <= maximumY;
    }

    public static boolean containsHazards(BlockGetter level, AABB bounds) {
        for (BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                BlockPos.containing(bounds.maxX - 1.0E-7, bounds.maxY - 1.0E-7, bounds.maxZ - 1.0E-7))) {
            BlockState state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty() || isHazardous(state)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHazardous(BlockState state) {
        return state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CACTUS)
                || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.WITHER_ROSE)
                || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.NETHER_PORTAL)
                || state.is(Blocks.END_PORTAL) || state.is(Blocks.END_GATEWAY);
    }

    public static boolean isLandingSurface(BlockState state, BlockGetter level, BlockPos pos) {
        return !isHazardous(state) && state.isFaceSturdy(level, pos, Direction.UP)
                && state.isSolidRender(level, pos);
    }
}
