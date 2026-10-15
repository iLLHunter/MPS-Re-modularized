package mpsremodularized.common.item.module.movement;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.PlayerUtils;
import mpsremodularized.common.config.module.MovementModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

public class BlinkDriveModule extends AbstractPowerModule {
    public static class RightClickie extends RightClickModule {
        public RightClickie(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MOVEMENT, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, MovementModuleConfig.blinkDriveModuleEnergyConsumptionBase, "FE");
            addBaseProperty(MPSConstants.RANGE, MovementModuleConfig.blinkDriveModuleRangeBase, "m");
            addTradeoffProperty(MPSConstants.RANGE, MPSConstants.ENERGY_CONSUMPTION, MovementModuleConfig.blinkDriveModuleEnergyConsumptionRangeMultiplier);
            addTradeoffProperty(MPSConstants.RANGE, MPSConstants.RANGE, MovementModuleConfig.blinkDriveModuleRangeMultiplier);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(@Nonnull ItemStack itemStackIn, Level worldIn, Player playerIn, InteractionHand hand) {
            if (worldIn.isClientSide) {
                return InteractionResultHolder.success(itemStackIn);
            }
            if (!(playerIn instanceof ServerPlayer player) || player.isPassenger() || player.isVehicle()) {
                return InteractionResultHolder.fail(itemStackIn);
            }
            double range = applyPropertyModifiers(MPSConstants.RANGE);
            if (!Double.isFinite(range) || range <= 0) {
                return fail(itemStackIn, player, "range");
            }
            BlockHitResult hit = rayTrace(worldIn, player, ClipContext.Fluid.NONE, range);
            if (hit.getType() != HitResult.Type.BLOCK) {
                return fail(itemStackIn, player, "target");
            }
            Optional<Vec3> destination = findSafeDestination(worldIn, player, hit);
            if (destination.isEmpty()) {
                return fail(itemStackIn, player, "unsafe");
            }
            double distance = hit.getLocation().distanceTo(player.getEyePosition());
            int energyConsumption = (int) Math.ceil(getEnergyUsage() * Math.clamp(distance / range, 0, 1));
            if (ElectricItemUtils.drainPlayerEnergy(player, energyConsumption, true) != energyConsumption) {
                return fail(itemStackIn, player, "energy");
            }
            Vec3 origin = player.position();
            Vec3 target = destination.get();
            if (!player.teleportTo(player.serverLevel(), target.x, target.y, target.z, Set.of(), player.getYRot(), player.getXRot())) {
                return InteractionResultHolder.fail(itemStackIn);
            }
            PlayerUtils.resetFloatKickTicks(player);
            player.resetFallDistance();
            ElectricItemUtils.drainPlayerEnergy(player, energyConsumption, false);
            worldIn.playSound(null, origin.x, origin.y, origin.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.0F);
            return InteractionResultHolder.success(itemStackIn);
        }

        private InteractionResultHolder<ItemStack> fail(ItemStack fist, ServerPlayer player, String reason) {
            player.displayClientMessage(Component.translatable("message.mpsremodularized.blink." + reason), true);
            return InteractionResultHolder.fail(fist);
        }

        @Override
        public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
            return context.getPlayer() == null ? InteractionResult.PASS
                    : use(stack, context.getLevel(), context.getPlayer(), context.getHand()).getResult();
        }

        @Override
        public boolean isAllowed() {
            return MovementModuleConfig.blinkDriveModuleIsAllowed;
        }

        @Override
        public int getEnergyUsage() {
            return (int) applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
        }
    }

    /** Keep the player's body outside the targeted face, with eyes aligned for a side hit. */
    public static Optional<Vec3> findSafeDestination(Level level, Player player, BlockHitResult hit) {
        Direction face = hit.getDirection();
        double clearance = player.getBbWidth() / 2.0 + 0.001;
        Vec3 point = hit.getLocation();
        Vec3 target = switch (face) {
            case UP -> point.add(0, 0.001, 0);
            case DOWN -> point.add(0, -player.getBbHeight() - 0.001, 0);
            default -> point.add(face.getStepX() * clearance, -player.getEyeHeight(), face.getStepZ() * clearance);
        };
        AABB bounds = player.getBoundingBox().move(target.subtract(player.position()));
        if (!DimensionalRiftModule.isWithinBuildBounds(bounds, level.getMinBuildHeight(), level.getMaxBuildHeight())
                || !level.getWorldBorder().isWithinBounds(bounds)
                || !level.noCollision(player, bounds)
                || DimensionalRiftModule.containsHazards(level, bounds)) {
            return Optional.empty();
        }
        return Optional.of(target);
    }
}
