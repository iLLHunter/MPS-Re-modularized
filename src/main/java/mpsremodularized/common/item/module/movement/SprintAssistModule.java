package mpsremodularized.common.item.module.movement;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PlayerTickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.config.module.MovementModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import mpsremodularized.common.network.MPSPackets;
import mpsremodularized.common.network.packets.serverbound.SetSprintAssistDoubleAndDrainServerBound;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceKey;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.registration.MPSCapabilities;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.stats.Stats;
import net.minecraft.world.food.FoodData;

import javax.annotation.Nonnull;

/**
 * Ported by leon on 10/18/16.
 */
public class SprintAssistModule extends AbstractPowerModule {
    // Only accessed on the server game thread; disconnected players are not retained.
    private static final Map<ServerPlayer, MotionSample> MOTION = new WeakHashMap<>();
    private record MotionSample(Vec3 position, ResourceKey<Level> dimension, long tick,
                                int sprintCentimeters, double fractionalEnergy) {}

    private static MotionSample sample(ServerPlayer player, double remainder) {
        return new MotionSample(player.position(), player.level().dimension(), player.level().getGameTime(),
                player.getStats().getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM)), remainder);
    }

    /** Bill movement and refund sprint exhaustion before vanilla converts exhaustion into hunger. */
    public static void onServerPlayerPreTick(ServerPlayer player) {
        ItemStack legs = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        IModularItem host = MPSCapabilities.getModularItem(legs);
        var module = host == null ? null : host.getModuleCapability(host.getOnlineModuleOrEmpty(MPSConstants.SPRINT_ASSIST_MODULE));
        if (player.isAlive() && !player.isSpectator() && host != null
                && host.isModuleOperational(module) && module instanceof Ticker ticker) {
            ticker.updateServerMovement(player, legs);
        } else {
            MOTION.remove(player);
            if (host != null) host.setModuleDouble(MPSConstants.SPRINT_ASSIST_MODULE, MPSConstants.MOVEMENT_SPEED, 0);
        }
    }

    /** Compensation is a fraction of vanilla's exhaustion for server-recorded sprint centimeters. */
    public static float refundSprintExhaustion(FoodData food, int sprintCentimeters, double compensation) {
        float exhaustion = food.getExhaustionLevel();
        if (sprintCentimeters <= 0 || !Double.isFinite(compensation) || compensation <= 0
                || !Float.isFinite(exhaustion) || exhaustion <= 0) return 0;
        float sprintExhaustion = 0.1F * sprintCentimeters * 0.01F;
        float refund = (float) Math.min(exhaustion, sprintExhaustion * Math.min(1, compensation));
        food.setExhaustion(Math.max(0, exhaustion - refund));
        return refund;
    }

    public static double movementEnergyCost(double cost, double distance, boolean sprinting) {
        if (!Double.isFinite(cost) || !Double.isFinite(distance) || cost < 0 || distance < 0) {
            return Double.NaN;
        }
        // Vanilla walkDist advances by 0.6 * distance; walking was billed once per 20 ticks.
        return cost * distance * 0.6 / (sprinting ? 1 : 20);
    }

    public static class Ticker extends PlayerTickModule {
        public Ticker(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MOVEMENT, ModuleTarget.LEGSONLY);
            // Sprinting ------------------------------------------------------------------------------------------------------------------
            addBaseProperty(MPSConstants.SPRINT_ENERGY_CONSUMPTION,MovementModuleConfig.sprintAssistModuleSprintEnergyConsumptionBase, "RF");
            addTradeoffProperty(MPSConstants.SPRINT_ASSIST, MPSConstants.SPRINT_ENERGY_CONSUMPTION, MovementModuleConfig.sprintAssistModuleSprintEnergyConsumptionAssistMultiplier);
            addBaseProperty(MPSConstants.SPRINT_SPEED_MULTIPLIER, MovementModuleConfig.sprintAssistModuleSprintSpeedBase, "%");
            addTradeoffProperty(MPSConstants.SPRINT_ASSIST, MPSConstants.SPRINT_SPEED_MULTIPLIER, MovementModuleConfig.sprintAssistModuleSprintSpeedAssistMultiplier);

            // Sprint Food Compensation -------------------------------------------------------------------------------
            addBaseProperty(MPSConstants.EXHAUSTION_COMPENSATION, MovementModuleConfig.sprintAssistModuleExhaustionCompensationBase, "%");
            addTradeoffProperty(MPSConstants.COMPENSATION, MPSConstants.SPRINT_ENERGY_CONSUMPTION, MovementModuleConfig.sprintAssistExhaustionCompensationEnergyConsumptionMultiplier);
            addTradeoffProperty(MPSConstants.COMPENSATION, MPSConstants.EXHAUSTION_COMPENSATION, MovementModuleConfig.sprintAssistExhaustionCompensationCompensationMultiplier);


            // Walking --------------------------------------------------------------------------------------------------------------------
            addBaseProperty(MPSConstants.WALKING_ENERGY_CONSUMPTION, MovementModuleConfig.sprintAssistModuleWalkingEnergyBase, "RF");
            addTradeoffProperty(MPSConstants.WALKING_ASSISTANCE, MPSConstants.WALKING_ENERGY_CONSUMPTION, MovementModuleConfig.sprintAssistModuleWalkingEnergyWalkingAssistanceMultiplier);
            addBaseProperty(MPSConstants.WALKING_SPEED_MULTIPLIER, MovementModuleConfig.sprintAssistModuleWalkingSpeedMultiplierBase, "%");
            addTradeoffProperty(MPSConstants.WALKING_ASSISTANCE, MPSConstants.WALKING_SPEED_MULTIPLIER, MovementModuleConfig.sprintAssistModuleWalkingSpeedMultiplierWalkingAssistMultiplier);
        }

        @Override
        public boolean isAllowed() {
            return MovementModuleConfig.sprintAssistModuleIsAllowed;
        }


        @Override
        public boolean onPlayerTickActive(Player player, Level level, @Nonnull ItemStack armorLegs, int moduleIndex) {
            if (!level.isClientSide()) {
                // The server's pre-tick handler already billed movement and updated hunger.
                return false;
            }
            // Normal:
            // ----------------------------------------
            // player speed .1 walking, .13 sprinting
            // ----------------------------------------
            if (player.getAbilities().flying || player.isPassenger() || player.isFallFlying()  || player.isInWaterOrBubble()) {
                setPlayerSpeed(0);
                return false;
            }

            if (level.isClientSide()) {
                double horzMovement = player.walkDist - player.walkDistO;
                double totalEnergy = ElectricItemUtils.getPlayerEnergy(player);

                if (horzMovement > 0.001) { // stop doing drain calculations or when player hasn't moved
                    if (player.isSprinting()) {
                        double sprintCost = applyPropertyModifiers(MPSConstants.SPRINT_ENERGY_CONSUMPTION);
                        if (sprintCost < totalEnergy) {
                            double sprintMultiplier = applyPropertyModifiers(MPSConstants.SPRINT_SPEED_MULTIPLIER) * 0.13;
                            setPlayerSpeed(sprintMultiplier);
                            player.getAbilities().setFlyingSpeed(player.getSpeed() * 0.2f);
                        }
                    } else {
                        double walkCost = applyPropertyModifiers(MPSConstants.WALKING_ENERGY_CONSUMPTION);
                        if (walkCost < totalEnergy) {
                            double walkMultiplier = applyPropertyModifiers(MPSConstants.WALKING_SPEED_MULTIPLIER) * 0.1; // value * normalWalkingSpeed
                            setPlayerSpeed(walkMultiplier);
                            player.getAbilities().setFlyingSpeed(player.getSpeed() * 0.2f);
                        }
                    }
                } else {
                    setPlayerSpeed(0);
                }
            }
            return false;
        }

        @Override
        public boolean onPlayerTickInactive(Player player, Level level, @Nonnull ItemStack itemStack, int moduleIndex) {
            if (level.isClientSide()) {
                setPlayerSpeed(0);
            } else {
                MOTION.remove(player);
                IModularItem host = MPSCapabilities.getModularItem(itemStack);
                if (host != null) {
                    host.setModuleDouble(MPSConstants.SPRINT_ASSIST_MODULE, MPSConstants.MOVEMENT_SPEED, 0);
                }
            }
            return false;
        }

        private void updateServerMovement(ServerPlayer player, ItemStack legs) {
            IModularItem host = MPSCapabilities.getModularItem(legs);
            MotionSample previous = MOTION.get(player);
            double remainder = previous == null ? 0 : previous.fractionalEnergy();
            MotionSample current = sample(player, remainder);
            MOTION.put(player, current);
            if (host == null) {
                return;
            }
            double boost = host.getModuleDouble(MPSConstants.SPRINT_ASSIST_MODULE, MPSConstants.MOVEMENT_SPEED);
            double configuredBoost = applyPropertyModifiers(player.isSprinting()
                    ? MPSConstants.SPRINT_SPEED_MULTIPLIER : MPSConstants.WALKING_SPEED_MULTIPLIER)
                    * (player.isSprinting() ? 0.13 : 0.1);
            double cost = applyPropertyModifiers(player.isSprinting()
                    ? MPSConstants.SPRINT_ENERGY_CONSUMPTION : MPSConstants.WALKING_ENERGY_CONSUMPTION);
            double playerEnergy = ElectricItemUtils.getPlayerEnergy(player);
            if (!Double.isFinite(boost) || !Double.isFinite(configuredBoost) || configuredBoost <= 0
                    || !Double.isFinite(cost) || cost < 0 || playerEnergy <= cost
                    || player.connection.clientIsFloating || player.getAbilities().flying || player.isPassenger()
                    || player.isFallFlying() || player.isInWaterOrBubble()) {
                host.setModuleDouble(MPSConstants.SPRINT_ASSIST_MODULE, MPSConstants.MOVEMENT_SPEED, 0);
                return;
            }
            double distance = 0;
            int sprintCentimeters = 0;
            if (previous != null && previous.dimension().equals(player.level().dimension())
                    && previous.tick() + 1 == player.level().getGameTime()) {
                Vec3 delta = player.position().subtract(previous.position());
                distance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
                sprintCentimeters = Math.max(0, current.sprintCentimeters() - previous.sprintCentimeters());
                // Restart the sample on teleport-sized changes rather than billing a teleport as walking.
                if (distance > Math.max(1.0, Math.abs(configuredBoost) + player.getSpeed()) * 10) {
                    distance = 0;
                    sprintCentimeters = 0;
                }
            }
            // Charge eligible movement even if a client sends stop just before the server tick.
            double accumulated = remainder + movementEnergyCost(cost, distance, player.isSprinting());
            double drain = Math.floor(accumulated);
            boolean funded = Double.isFinite(drain) && drain <= playerEnergy
                    && ElectricItemUtils.drainPlayerEnergy(player, drain, true) >= drain;
            if (!funded) {
                configuredBoost = 0;
                accumulated = 0;
                drain = 0;
            } else if (drain > 0) {
                funded = ElectricItemUtils.drainPlayerEnergy(player, drain, false) >= drain;
                if (!funded) {
                    configuredBoost = 0;
                    accumulated = 0;
                    drain = 0;
                }
            }
            if (funded && player.isSprinting() && !player.getAbilities().invulnerable) {
                refundSprintExhaustion(player.getFoodData(), sprintCentimeters,
                        applyPropertyModifiers(MPSConstants.EXHAUSTION_COMPENSATION));
            }
            MOTION.put(player, sample(player, accumulated - drain));
            host.setModuleDouble(MPSConstants.SPRINT_ASSIST_MODULE, MPSConstants.MOVEMENT_SPEED,
                    boost > 0 ? Math.max(0, configuredBoost) : 0);
        }

        void setPlayerSpeed(double value) {
            try {
                MPSPackets.sendToServer(new SetSprintAssistDoubleAndDrainServerBound(value > 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
