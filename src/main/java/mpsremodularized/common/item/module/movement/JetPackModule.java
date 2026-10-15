package mpsremodularized.common.item.module.movement;

import mpsremodularized.client.config.MPSClientConfig;
import mpsremodularized.client.control.PlayerMovementInputWrapper;
import mpsremodularized.client.sound.Musique;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PlayerTickModule;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.client.sound.MPSSoundDictionary;
import mpsremodularized.common.config.module.MovementModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.event.MovementManager;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class JetPackModule extends AbstractPowerModule {
    public static class Ticker extends PlayerTickModule {
        public Ticker(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MOVEMENT, ModuleTarget.TORSOONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, MovementModuleConfig.jetPackModuleEnergyConsumptionBase, "RF/t");
            addBaseProperty(MPSConstants.JETPACK_THRUST, MovementModuleConfig.jetPackThrustBase, "N");
            addTradeoffProperty(MPSConstants.THRUST, MPSConstants.ENERGY_CONSUMPTION, MovementModuleConfig.jetPackEnergyConsumptionThrustMultiplier, "FE");
            addTradeoffProperty(MPSConstants.THRUST, MPSConstants.JETPACK_THRUST, MovementModuleConfig.jetPackThrustMultiplier);
        }

        @Override
        public boolean isAllowed() {
            return MovementModuleConfig.jetPackModuleIsAllowed;
        }

        @Override
        public boolean onPlayerTickActive(Player player, Level level, ItemStack torso, int moduleIndex) {
            if (player.isInWater()) {
                onPlayerTickInactive(player, level, torso, moduleIndex);
                return false;
            }

            PlayerMovementInputWrapper.PlayerMovementInput playerInput = PlayerMovementInputWrapper.get(player);
            boolean hasFlightControl = false;
            ItemStack helmet = ItemUtils.getItemFromEntitySlot(player, EquipmentSlot.HEAD);
            IModularItem modularItem = helmet.getCapability(MPSCapabilities.Inventory.MODULAR_ITEM);
            if(modularItem != null) {
                hasFlightControl = modularItem.isModuleOnline(MPSConstants.FLIGHT_CONTROL_MODULE);
            }

            double jetEnergy = 0;
            double thrust = 0;
            jetEnergy += applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
            thrust += applyPropertyModifiers(MPSConstants.JETPACK_THRUST);

            if ((hasFlightControl && thrust > 0) || playerInput.jumpKey) {
                double used = MovementManager.thrust(player, thrust, hasFlightControl, true);
                double required = Math.ceil(used * jetEnergy * 5);
                if (Double.isFinite(required) && required >= 0
                        && required <= ElectricItemUtils.getPlayerEnergy(player)
                        && ElectricItemUtils.drainPlayerEnergy(player, required, true) >= required) {
                    if (!level.isClientSide && level.getGameTime() % 5 == 0)
                        ElectricItemUtils.drainPlayerEnergy(player, required, false);
                    MovementManager.thrust(player, thrust, hasFlightControl);
                    if (level.isClientSide && MPSClientConfig.useSounds)
                        Musique.playerSound(player, MPSSoundDictionary.SOUND_EVENT_JETPACK.get(), SoundSource.PLAYERS, (float) (used * 6.25), 1.0f, true);
                    return false;
                }
            }
            onPlayerTickInactive(player, level, torso, moduleIndex);
            return false;
        }

        @Override
        public boolean onPlayerTickInactive(Player player, Level level, ItemStack item,  int moduleIndex) {
            if (level.isClientSide && MPSClientConfig.useSounds) {
                Musique.stopPlayerSound(player, MPSSoundDictionary.SOUND_EVENT_JETPACK.get());
            }
            return false;
        }
    }
}
