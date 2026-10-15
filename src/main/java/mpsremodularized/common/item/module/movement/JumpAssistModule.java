package mpsremodularized.common.item.module.movement;

import mpsremodularized.client.control.PlayerMovementInputWrapper;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PlayerTickModule;
import mpsremodularized.common.utils.PlayerUtils;
import mpsremodularized.common.config.module.MovementModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.event.MovementManager;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class JumpAssistModule extends AbstractPowerModule {

    public static class Ticker extends PlayerTickModule {
        public Ticker(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MOVEMENT, ModuleTarget.LEGSONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, MovementModuleConfig.jumpAssistModuleEnergyConsumptionBase, "FE");
            addTradeoffProperty(MPSConstants.POWER, MPSConstants.ENERGY_CONSUMPTION, MovementModuleConfig.jumpAssistModuleEnergyConsumptionPowerMultiplier);
            addBaseProperty(MPSConstants.JUMP_BOOST, MovementModuleConfig.jumpAssistModuleJumpBoostBase, "%");
            addTradeoffProperty(MPSConstants.POWER, MPSConstants.JUMP_BOOST, MovementModuleConfig.jumpAssistModuleJumpBoostPowerMultiplier);
            addTradeoffProperty(MPSConstants.COMPENSATION, MPSConstants.ENERGY_CONSUMPTION, MovementModuleConfig.jumpAssistModuleEnergyConsumptionCompensationMultiplier);
            addBaseProperty(MPSConstants.EXHAUSTION_COMPENSATION, MovementModuleConfig.jumpAssistModuleExhaustionCompensationBase, "%");
            addTradeoffProperty(MPSConstants.COMPENSATION, MPSConstants.EXHAUSTION_COMPENSATION, MovementModuleConfig.jumpAssistModuleExhaustionCompensationCompensationMultiplier);
        }

        @Override
        public boolean isAllowed() {
            return MovementModuleConfig.jumpAssistModuleIsAllowed;
        }

        @Override
        public boolean onPlayerTickActive(Player player, Level level, ItemStack item, int moduleIndex) {
            PlayerMovementInputWrapper.PlayerMovementInput playerInput = PlayerMovementInputWrapper.get(player);
            if (playerInput.jumpKey) {
                double multiplier = MovementManager.INSTANCE.getPlayerJumpMultiplier(player);
                if (multiplier > 0) {
                    player.setDeltaMovement(player.getDeltaMovement().add(0, 0.15 * Math.min(multiplier, 1), 0));
                    MovementManager.INSTANCE.setPlayerJumpTicks(player, multiplier - 1);
                }
                PlayerUtils.resetFloatKickTicks(player);
//                player.getAbilities().setFlyingSpeed(player.getSpeed() * .2f); // FixMe? What was this for?
            } else {
                MovementManager.INSTANCE.setPlayerJumpTicks(player, 0);
            }
            PlayerUtils.resetFloatKickTicks(player);
            return false;
        }
    }
}
