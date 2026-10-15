package mpsremodularized.common.event;

import mpsremodularized.client.config.MPSClientConfig;
import mpsremodularized.client.control.PlayerMovementInputWrapper;
import mpsremodularized.client.sound.Musique;
import mpsremodularized.client.sound.MPSSoundDictionary;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.common.utils.MathUtils;
import mpsremodularized.common.utils.PlayerUtils;
import mpsremodularized.common.utils.TagUtils;
import mpsremodularized.common.config.MPSCommonConfig;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum MovementManager {
    INSTANCE;
    static final double root2 = 1 / Math.sqrt(2);
    public static final Map<UUID, Double> playerJumpMultipliers = new HashMap<>();
    /**
     * Gravity, in meters per tick per tick.
     */
    public static final double DEFAULT_GRAVITY = -0.0784000015258789;
    public double getPlayerJumpMultiplier(Player player) {
        if (playerJumpMultipliers.containsKey(player.getUUID())) {
            return playerJumpMultipliers.get(player.getUUID());
        } else {
            return 0;
        }
    }

    public void setPlayerJumpTicks(Player player, double number) {
        playerJumpMultipliers.put(player.getUUID(), number);
    }

    public double computeFallHeightFromVelocity(double velocity) {
        double ticks = velocity / DEFAULT_GRAVITY;
        return -0.5 * DEFAULT_GRAVITY * ticks * ticks;
    }

    public double getMovementResistance(Player player) {
        double movementResistance = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equippedItem = ItemUtils.getItemFromEntitySlot(player, slot);
            IModularItem modularItem = MPSCapabilities.getModularItem(equippedItem);
            if(modularItem != null) {
                movementResistance += modularItem.getMovementResistance();
            }
        }
        return movementResistance;
    }

    // moved here so it is still accessible if sprint assist module isn't installed.
    public void setMovementModifier(ItemStack itemStack, double multiplier, Player player, Holder<Attribute> attribute) {
        // reduce player speed according to Kinetic Energy Generator setting
        double movementResistance = getMovementResistance(player);
        multiplier -= movementResistance;
        // player walking speed: 0.1
        // player sprint speed: 0.13
        // negative addative will make player slower
        double additive;

//        MPSLogger.logDebug("multiplier: " + multiplier);

        if(player.isSprinting()) {
            additive = multiplier * 0.13;
        }else {
            additive = multiplier * 0.1;
        }
        additive = additive * 0.5;

        if(additive > 0) {
            if(player.isSprinting()) {
                //            additive = multiplier * 0.13;
                additive = 10;
            }else {
                //            additive = multiplier * 0.1;
                additive = 5;
            }
        }

//        removeModifiers(itemStack);

        ItemAttributeModifiers modifiers = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
//        MPSLogger.logDebug("compute before: " + modifiers.compute(player.getAttributeBaseValue(Attributes.MOVEMENT_SPEED), EquipmentSlot.LEGS));

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

//        MPSLogger.logDebug("setting speed as: " + additive);

//        builder.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(getArmorAttributeResourceLocationBySlot(EquipmentSlot.LEGS), additive, AttributeModifier.Operation.ADD_VALUE),
//            EquipmentSlotGroup.bySlot(EquipmentSlot.LEGS));
//        TagUtils.setAttributes(itemStack, builder.build());

        //        ItemStack testStack = ItemUtils.getItemFromEntitySlot(player, EquipmentSlot.LEGS);
        //
        //        ItemAttributeModifiers test = testStack.get(DataComponents.ATTRIBUTE_MODIFIERS);

        //
        //
        //
        //        //        +        // Neo: Convert Movement Speed to percent-based for more appropriate display using IAttributeExtension. Use a scale factor of 1000 since movement speed has 0.001 units.
        //        //            +        "generic.movement_speed", new PercentageAttribute("attribute.name.generic.movement_speed", 0.7, 0.0, 1024.0, 1000).setSyncable(true));
        //        //
        //        modifiers = testStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        //
        ////                AttributeInstance attributeinstance = modifiers..modifiers().getInstance(attribute);
        ////                    this.getAttribute(Attributes.MOVEMENT_SPEED);
        ////                if (attributeinstance != null && attributeinstance.getModifier(SPEED_MODIFIER_POWDER_SNOW_ID) != null) {
        ////                    attributeinstance.removeModifier(SPEED_MODIFIER_POWDER_SNOW_ID);
        ////                }
        //
        //        MPSLogger.logDebug("test stack compute after: " + modifiers.compute(player.getAttributeBaseValue(Attributes.MOVEMENT_SPEED), EquipmentSlot.LEGS));
        //        if (test != null) {
        //
        //            MPSLogger.logDebug("test here: " + MobEffects.MOVEMENT_SPEED.value());
        //
        //
        //        }
        //
        //        modifiers = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        //
        //        //                AttributeInstance attributeinstance = modifiers..modifiers().getInstance(attribute);
        //        //                    this.getAttribute(Attributes.MOVEMENT_SPEED);
        //        //                if (attributeinstance != null && attributeinstance.getModifier(SPEED_MODIFIER_POWDER_SNOW_ID) != null) {
        //        //                    attributeinstance.removeModifier(SPEED_MODIFIER_POWDER_SNOW_ID);
        //        //                }
        //        test = testStack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        //        MPSLogger.logDebug("itemStack compute after: " + modifiers.compute(player.getAttributeBaseValue(Attributes.MOVEMENT_SPEED), EquipmentSlot.LEGS));
        //        if (test != null) {
        //
        //            MPSLogger.logDebug("itemStack here: " + MobEffects.MOVEMENT_SPEED.value());
        //
        //
        //        }
        //
        //
        //
        //        MPSLogger.logDebug("equal?: " + ItemStack.matches(itemStack, testStack));


        //        if(additive > 0) {
        //            MobEffectInstance effect = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 10);
        ////            MobEffectInstance effectTest = player.getEffect(MobEffects.MOVEMENT_SPEED);
        ////
        ////            if(effectTest == null || effectTest.getDuration() < 90) {
        //                player.addEffect(effect);
        //
        //                //            NeoForgeMod.SWIM_SPEED
        ////            } else {
        ////                MPSLogger.logDebug("player already has effect");
        ////            }
        //        } else {
        //            MPSLogger.logDebug("removing effect");
        //
        //            player.removeEffect(MobEffects.MOVEMENT_SPEED);
        //        }
    }

    public static ResourceLocation getArmorAttributeResourceLocationBySlot(EquipmentSlot slot) {
        return ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "armor." + slot.getName());
    }

    public static void removeModifiers(@Nonnull ItemStack itemStack) {
        TagUtils.setAttributes(itemStack, ItemAttributeModifiers.EMPTY);
    }

    static double boolToVal(boolean boolIn) {
        return boolIn ? 1.0D : 0.0D;
    }

    public static double thrust(Player player, double thrust, boolean flightControl) {
        return thrust(player, thrust, flightControl, false);
    }

    /** Preview computes the same motion and cost without moving the player or resetting flight checks. */
    public static double thrust(Player player, double thrust, boolean flightControl, boolean simulate) {
        return thrust(player, thrust, flightControl, simulate, PlayerMovementInputWrapper.get(player));
    }

    /** Allows a module to supply its own input snapshot without rewriting shared key states. */
    public static double thrust(Player player, double thrust, boolean flightControl, boolean simulate,
                                PlayerMovementInputWrapper.PlayerMovementInput playerInput) {
        Vec3 motion = player.getDeltaMovement();
        double thrustUsed = 0;
        if (flightControl) {
            Vec3 desiredDirection = player.getLookAngle().normalize();
            double strafeX = desiredDirection.z;
            double strafeZ = -desiredDirection.x;

            double flightVerticality = 0D;
            IModularItem iModularItem = ItemUtils.getItemFromEntitySlot(player, EquipmentSlot.HEAD).getCapability(MPSCapabilities.Inventory.MODULAR_ITEM);
            if (iModularItem != null) {
                ItemStack flightControlModule = iModularItem.getOnlineModuleOrEmpty(MPSConstants.FLIGHT_CONTROL_MODULE);
                IPowerModule pm = flightControlModule.getCapability(MPSCapabilities.Module.POWER_MODULE);
                if (pm != null) {
                    flightVerticality = pm.applyPropertyModifiers(MPSConstants.FLIGHT_VERTICALITY);
                }
            }

            int strafeState = ((playerInput.strafeRightKey ? -1 : 0) + (playerInput.strafeLeftKey ? 1 : 0));
            double forwardReverse = ((playerInput.reverseKey ? -1D : 0) + (playerInput.forwardKey ? 1D : 0));

            desiredDirection = new Vec3(
                (desiredDirection.x * Math.signum(forwardReverse) + strafeX * Math.signum(strafeState)),
                (flightVerticality * desiredDirection.y * Math.signum(forwardReverse) + (playerInput.jumpKey ? 1 : 0) - (playerInput.downKey ? 1 : 0)),
                (desiredDirection.z * Math.signum(forwardReverse) + strafeZ * Math.signum(strafeState)));

            desiredDirection = desiredDirection.normalize();

            // Brakes
            if (motion.y < 0 && desiredDirection.y >= 0) {
                if (-motion.y > thrust) {
                    motion = motion.add(0, thrust, 0);
                    thrustUsed += thrust;
                    thrust = 0;
                } else {
                    thrust -= motion.y;
                    thrustUsed += motion.y;
                    motion = new Vec3(motion.x, 0, motion.z);
                }
            }

            if (motion.y < -1) {
                thrust += 1 + motion.y;
                thrustUsed -= 1 + motion.y;
                motion = new Vec3(motion.x, -1, motion.z);
            }

            if (Math.abs(motion.x) > 0 && desiredDirection.length() == 0) {
                //                MPSLogger.logDebug("motion.x");

                if (Math.abs(motion.x) > thrust) {
                    motion = motion.add(
                        -(Math.signum(motion.x) * thrust), 0, 0);
                    thrustUsed += thrust;
                    thrust = 0;
                } else {
                    thrust -= Math.abs(motion.x);
                    thrustUsed += Math.abs(motion.x);
                    motion = new Vec3(0, motion.y, motion.z);
                }
            }

            if (Math.abs(motion.z) > 0 && desiredDirection.length() == 0) {
                if (Math.abs(motion.z) > thrust) {
                    motion = motion.add(0, 0, -(Math.signum(motion.z) * thrust));
                    thrustUsed += thrust;
                    thrust = 0;
                } else {
                    thrustUsed += Math.abs(motion.z);
                    thrust -= Math.abs(motion.z);
                    motion = new Vec3(motion.x, motion.y, 0);
                }
            }

            // Thrusting, finally :V
            motion = motion.add(
                thrust * desiredDirection.x,
                thrust * desiredDirection.y,
                thrust * desiredDirection.z
            );
            thrustUsed += thrust;

        } else {
            Vec3 desiredDirection = player.getLookAngle().normalize();
            desiredDirection = new Vec3(desiredDirection.x, 0, desiredDirection.z);
            desiredDirection.normalize();
            if (!playerInput.forwardKey) {
                motion = motion.add(0, thrust, 0);
            } else {

                //                MPSLogger.logDebug("thrust: " + thrust +", thrust * root2: " + (thrust * root2) );

                motion = motion.add(
                    desiredDirection.x * thrust * root2 * boolToVal(playerInput.forwardKey),
                    thrust * root2,
                    desiredDirection.z * thrust * root2// * Math.signum(playerInput.forwardKey)
                );
            }
            thrustUsed += thrust;
        }
        // Slow the player if they are going too fast
        double horzm2 = motion.x * motion.x + motion.z * motion.z;

        // currently comes out to 0.0625
        double horizontalLimit = MPSCommonConfig.maxFlyingSpeed * MPSCommonConfig.maxFlyingSpeed / 400;

        if (player.isCrouching() && horizontalLimit > 0.05) {
            horizontalLimit = 0.05;
        }

        if (horzm2 > horizontalLimit) {
            double ratio = Math.sqrt(horizontalLimit / horzm2);
            motion = new Vec3(
                motion.x * ratio,
                motion.y,
                motion.z * ratio);
        }

        if (!simulate) {
            player.setDeltaMovement(motion);
            PlayerUtils.resetFloatKickTicks(player);
        }
        return thrustUsed;
    }

    public static double computePlayerVelocity(Player player) {
        return MathUtils.pythag(player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z);
    }

    @SubscribeEvent
    public void handleLivingJumpEvent(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            Level level = player.level();
            IModularItem iModularItem = ItemUtils.getItemFromEntitySlot(player, EquipmentSlot.LEGS).getCapability(MPSCapabilities.Inventory.MODULAR_ITEM);
            if (iModularItem != null) {
                IPowerModule jumper = iModularItem.getOnlineModuleOrEmpty(MPSConstants.JUMP_ASSIST_MODULE).getCapability(MPSCapabilities.Module.POWER_MODULE);
                if (jumper != null) {
                    double jumpAssist = jumper.applyPropertyModifiers(MPSConstants.JUMP_BOOST) * 2;
                    int drain = (int) Math.ceil(jumper.applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION));
                    double avail = ElectricItemUtils.getPlayerEnergy(player);
                    if ((level.isClientSide()) && MPSClientConfig.useSounds) {
                        Musique.playerSound(player, MPSSoundDictionary.SOUND_EVENT_JUMP_ASSIST.get(), SoundSource.PLAYERS, (float) (jumpAssist / 8.0), (float) 1, false);
                    }

                    if (drain <= avail) {
                        ElectricItemUtils.drainPlayerEnergy(player, drain, false);
                        setPlayerJumpTicks(player, jumpAssist);
                        double jumpCompensationRatio = jumper.applyPropertyModifiers(MPSConstants.EXHAUSTION_COMPENSATION);
                        if (player.isSprinting()) {
                            player.getFoodData().addExhaustion((float) (-0.2F * jumpCompensationRatio));
                        } else {
                            player.getFoodData().addExhaustion((float) (-0.05F * jumpCompensationRatio));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void handleFallEvent(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && event.getDistance() > 3.0) {
            Level level = player.level();
            IModularItem iModularItem = ItemUtils.getItemFromEntitySlot(player, EquipmentSlot.FEET).getCapability(MPSCapabilities.Inventory.MODULAR_ITEM);
            if (iModularItem != null) {
                ItemStack shockAbsorbers = iModularItem.getOnlineModuleOrEmpty(MPSConstants.SHOCK_ABSORBER_MODULE);
                IPowerModule sa = shockAbsorbers.getCapability(MPSCapabilities.Module.POWER_MODULE);
                if (sa != null) {
                    double distanceAbsorb = Math.abs(event.getDistance() * sa.applyPropertyModifiers(MPSConstants.DISTANCE_REDUCTION));
                    if (level.isClientSide && MPSClientConfig.useSounds) {
                        Musique.playerSound(player, MPSSoundDictionary.SOUND_EVENT_GUI_INSTALL.get(), SoundSource.PLAYERS, (float) (distanceAbsorb), (float) 1, false);
                    }
                    int drain = (int) Math.ceil(distanceAbsorb * sa.applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION));
                    double avail = ElectricItemUtils.getPlayerEnergy(player);
                    if (drain <= avail) {
                        ElectricItemUtils.drainPlayerEnergy(player, drain, false);
                        double fallDistance = Math.max(0, (event.getDistance() - distanceAbsorb));
                        event.setDistance((float) fallDistance);
                        //                        event.getEntityLiving().sendMessage(Component.literalString("modified fall settings: [ damage : " + event.getDamageMultiplier() + " ], [ distance : " + event.getDistance() + " ]"));
                    }
                }
            }
        }
    }
}
