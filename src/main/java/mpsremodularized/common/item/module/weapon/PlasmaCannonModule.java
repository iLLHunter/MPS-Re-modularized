package mpsremodularized.common.item.module.weapon;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.HeatUtils;
import mpsremodularized.common.utils.MathUtils;
import mpsremodularized.common.config.module.WeaponModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.entity.PlasmaBallEntity;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class PlasmaCannonModule extends AbstractPowerModule {

    public static class RightClickie extends RightClickModule {
        public RightClickie(ItemStack module) {
            super(module, ModuleCategory.WEAPON, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.ENERGY_PER_TICK, WeaponModuleConfig.plasmaCannonEnergyPerTickBase, "FE");
            addBaseProperty(MPSConstants.PLASMA_CANNON_DAMAGE_AT_FULL_CHARGE, WeaponModuleConfig.plasmaCannonDamageAtFullChargeBase, "pt");
            addTradeoffProperty(MPSConstants.AMPERAGE, MPSConstants.ENERGY_PER_TICK, WeaponModuleConfig.plasmaCannonEnergyPerTickAmperageMultiplier, "FE");
            addTradeoffProperty(MPSConstants.AMPERAGE, MPSConstants.PLASMA_CANNON_DAMAGE_AT_FULL_CHARGE, WeaponModuleConfig.plasmaCannonDamageAtFullChargeAmperageMultiplier, "pt");
            addTradeoffProperty(MPSConstants.VOLTAGE, MPSConstants.ENERGY_PER_TICK, WeaponModuleConfig.plasmaCannonEnergyPerTickVoltageMultiplier, "FE");
            addTradeoffProperty(MPSConstants.VOLTAGE, MPSConstants.PLASMA_CANNON_EXPLOSIVENESS, WeaponModuleConfig.plasmaCannonExplosivenessVoltageMultiplier, MPSConstants.CREEPER);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(@Nonnull ItemStack itemStackIn, Level level, Player playerIn, InteractionHand hand) {
//            MPSLogger.logDebug("use here() ");

            if (hand == InteractionHand.MAIN_HAND && ElectricItemUtils.getPlayerEnergy(playerIn) >= minimumShotCost()) {
                playerIn.startUsingItem(hand);
                return InteractionResultHolder.consume(itemStackIn);
            }
            return InteractionResultHolder.fail(itemStackIn);
        }

        @Override
        public void releaseUsing(@Nonnull ItemStack stack, Level levelIn, LivingEntity entityLiving, int timeLeft) {
            int chargeTicks = (int) MathUtils.clampDouble(stack.getUseDuration(entityLiving) - timeLeft, 10, 50);

//            MPSLogger.logDebug("release using with charge ticks " + chargeTicks);

            if (!levelIn.isClientSide && entityLiving instanceof Player) {
                double chargePercent = chargeTicks * 0.02; // chargeticks/50
                int energyConsumption = (int) (getEnergyUsage() * chargePercent);
                Player player = (Player) entityLiving;
                if (ElectricItemUtils.getPlayerEnergy(player) >= energyConsumption) {
                    float explosiveness = (float) (applyPropertyModifiers(MPSConstants.PLASMA_CANNON_EXPLOSIVENESS) * chargePercent);
                    float damagingness = (float) (applyPropertyModifiers(MPSConstants.PLASMA_CANNON_DAMAGE_AT_FULL_CHARGE) * chargePercent);
                    PlasmaBallEntity plasmaBolt = new PlasmaBallEntity(levelIn, player, explosiveness, damagingness, (float) chargePercent);
                    if (levelIn.addFreshEntity(plasmaBolt)) {
                        HeatUtils.heatPlayer(player, energyConsumption / 5000F * chargePercent);
                        ElectricItemUtils.drainPlayerEnergy(player, energyConsumption, false);
                    }
                }
            }
        }

        @Override
        public boolean isAllowed() {
            return WeaponModuleConfig.plasmaCannonIsAllowed;
        }

        @Override
        public int getEnergyUsage() {
            return (int) Math.round(applyPropertyModifiers(MPSConstants.ENERGY_PER_TICK));
        }

        private int minimumShotCost() { return (int) (getEnergyUsage() * .2); }
    }
}
