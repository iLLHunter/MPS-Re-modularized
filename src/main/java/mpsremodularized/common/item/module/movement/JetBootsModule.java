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

public class JetBootsModule extends AbstractPowerModule {
    public static class Ticker extends PlayerTickModule {
        public Ticker(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MOVEMENT, ModuleTarget.FEETONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, MovementModuleConfig.jetBootsModuleEnergyConsumptionBase, "RF/t");
            addBaseProperty(MPSConstants.JETBOOTS_THRUST, MovementModuleConfig.jetBootsThrustBase, "N");
            addTradeoffProperty(MPSConstants.THRUST, MPSConstants.ENERGY_CONSUMPTION, MovementModuleConfig.jetBootsEnergyConsumptionThrustMultiplier, "FE");
            addTradeoffProperty(MPSConstants.THRUST, MPSConstants.JETBOOTS_THRUST, MovementModuleConfig.jetBootsThrustMultiplier);
        }

        @Override
        public boolean isAllowed() {
            return MovementModuleConfig.jetBootsModuleIsAllowed;
        }

        @Override
        public boolean onPlayerTickActive(Player player, Level level, ItemStack item, int moduleIndex) {
            if (player.isInWater()) {
                onPlayerTickInactive(player, level, item, moduleIndex);
                return false;
            }

            boolean hasFlightControl = false;
            IModularItem modularItem = ItemUtils.getItemFromEntitySlot(player, EquipmentSlot.HEAD).getCapability(MPSCapabilities.Inventory.MODULAR_ITEM);
            if (modularItem != null) {
                hasFlightControl = modularItem.isModuleOnline(MPSConstants.FLIGHT_CONTROL_MODULE);
            }

            double jetEnergy = applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
            double thrust = applyPropertyModifiers(MPSConstants.JETBOOTS_THRUST);

            PlayerMovementInputWrapper.PlayerMovementInput playerInput = PlayerMovementInputWrapper.get(player);
            if ((hasFlightControl && thrust > 0) || (playerInput.jumpKey && player.getDeltaMovement().y < 0.5)) {
                double used = MovementManager.thrust(player, thrust, hasFlightControl, true);
                double required = Math.ceil(used * jetEnergy);
                if (Double.isFinite(required) && required >= 0
                        && required <= ElectricItemUtils.getPlayerEnergy(player)
                        && ElectricItemUtils.drainPlayerEnergy(player, required, true) >= required) {
                    if (!level.isClientSide) ElectricItemUtils.drainPlayerEnergy(player, required, false);
                    MovementManager.thrust(player, thrust, hasFlightControl);
                    if (level.isClientSide && MPSClientConfig.useSounds)
                        Musique.playerSound(player, MPSSoundDictionary.SOUND_EVENT_JETBOOTS.get(), SoundSource.PLAYERS, (float) (used * 12.5), 1.0f, true);
                    return false;
                }
            }
            onPlayerTickInactive(player, level, item, moduleIndex);
            return false;
        }

        @Override
        public int getEnergyUsage() {
            return (int) applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
        }

        @Override
        public boolean onPlayerTickInactive(Player player, Level level, ItemStack item, int moduleIndex) {
            if (level.isClientSide && MPSClientConfig.useSounds) {
                Musique.stopPlayerSound(player, MPSSoundDictionary.SOUND_EVENT_JETBOOTS.get());
            }
            return false;
        }
    }
}
