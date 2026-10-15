package mpsremodularized.common.item.module.movement;

import mpsremodularized.client.config.MPSClientConfig;
import mpsremodularized.client.control.PlayerMovementInputWrapper;
import mpsremodularized.client.sound.Musique;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PlayerTickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.client.sound.MPSSoundDictionary;
import mpsremodularized.common.config.module.MovementModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.event.MovementManager;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;


// FixMe
public class SwimAssistModule extends AbstractPowerModule {
    // NeoForgeMod.SWIM_SPEED


    public static class Ticker extends PlayerTickModule {
        public Ticker(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MOVEMENT, ModuleTarget.LEGSONLY);
            addTradeoffProperty(MPSConstants.THRUST, MPSConstants.ENERGY_CONSUMPTION, MovementModuleConfig.swimAssistModuleEnergyConsumptionThrustMultiplier, "FE");
            addTradeoffProperty(MPSConstants.THRUST, MPSConstants.SWIM_BOOST_AMOUNT, MovementModuleConfig.swimAssistModuleThrustBoostMultiplier, "m/s");

        }

        @Override
        public boolean isAllowed() {
            return MovementModuleConfig.swimAssistModuleIsAllowed;
        }

        @Override
        public boolean onPlayerTickActive(Player player, Level level, @Nonnull ItemStack itemStack, int moduleIndex) {
            PlayerMovementInputWrapper.PlayerMovementInput playerInput = PlayerMovementInputWrapper.get(player);
            // This is a fresh snapshot. Include vanilla sneak without changing the shared flight controls.
            playerInput.downKey = playerInput.downKey || player.isShiftKeyDown();
            if((player.isInWater() && !player.isPassenger()) && (playerInput.strafeLeftKey || playerInput.strafeRightKey || playerInput.forwardKey || playerInput.reverseKey || playerInput.jumpKey || playerInput.downKey)) {
                double moveRatio = 0;
                if (playerInput.forwardKey || playerInput.reverseKey) {
                    moveRatio += 1.0;
                }
                if (playerInput.strafeLeftKey || playerInput.strafeRightKey) {
                    moveRatio += 1.0;
                }
                if (playerInput.jumpKey || playerInput.downKey) {
                    moveRatio += 0.2 * 0.2;
                }
                double swimAssistRate = applyPropertyModifiers(MPSConstants.SWIM_BOOST_AMOUNT) * 0.05 * moveRatio;
                double swimEnergyConsumption = applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);

                double required = Math.ceil(swimEnergyConsumption * 5);
                if (Double.isFinite(required) && required >= 0
                        && required <= ElectricItemUtils.getPlayerEnergy(player)
                        && ElectricItemUtils.drainPlayerEnergy(player, required, true) >= required) {
                    if (level.isClientSide && MPSClientConfig.useSounds) {
                        Musique.playerSound(player, MPSSoundDictionary.SOUND_EVENT_SWIM_ASSIST.get(), SoundSource.PLAYERS, 1.0f, 1.0f, true);
                        // every 20 ticks
                    } else if ((level.getGameTime() % 5) == 0) {
                        ElectricItemUtils.drainPlayerEnergy(player, required, false);
                    }
                    MovementManager.thrust(player, swimAssistRate, true, false, playerInput);
//                    SprintAssistModule.setMovementModifier(itemStack, getModule(), swimAssistRate * 100000, MPSConstants.SWIM_SPEED);
                } else {
                    onPlayerTickInactive(player, level, itemStack, moduleIndex);
                }
            } else {
                onPlayerTickInactive(player, level, itemStack, moduleIndex);
            }
            return false;
        }

        @Override
        public boolean onPlayerTickInactive(Player player, Level level, @Nonnull ItemStack itemStack, int moduleIndex) {
            if (level.isClientSide && MPSClientConfig.useSounds) {
                Musique.stopPlayerSound(player, MPSSoundDictionary.SOUND_EVENT_SWIM_ASSIST.get());
            }
//            SprintAssistModule.setMovementModifier(itemStack, getModule(), 0, MPSConstants.SWIM_SPEED);
            return false;
        }
    }
}
