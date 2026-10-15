package mpsremodularized.common.item.module.weapon;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.config.module.WeaponModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.entity.SpinningBladeEntity;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class BladeLauncherModule extends AbstractPowerModule {
    public static class RightClickie extends RightClickModule {

        public RightClickie(ItemStack module) {
            super(module, ModuleCategory.WEAPON, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, WeaponModuleConfig.bladeLauncherEnergyConsumption, "FE");
            addBaseProperty(MPSConstants.BLADE_DAMAGE, WeaponModuleConfig.bladeLauncherSpinningBladeDamage, "pt");
        }

        @Override
        public InteractionResultHolder<ItemStack> use(@Nonnull ItemStack itemStackIn, Level level, Player playerIn, InteractionHand hand) {
            if (hand == InteractionHand.MAIN_HAND) {
                if (ElectricItemUtils.getPlayerEnergy(playerIn) >= getEnergyUsage()) {
                    playerIn.startUsingItem(hand);
                    return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
                }
            }
            return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
        }

        @Override
        public void releaseUsing(ItemStack stack, Level levelIn, LivingEntity entityLiving, int timeLeft) {
            if (!levelIn.isClientSide) {
                int energyConsumption = getEnergyUsage();

                if (ElectricItemUtils.getPlayerEnergy(entityLiving) >= energyConsumption) {
                    SpinningBladeEntity blade = new SpinningBladeEntity(levelIn, entityLiving);
                    if (levelIn.addFreshEntity(blade)) ElectricItemUtils.drainPlayerEnergy(entityLiving, energyConsumption, false);
                }
            }
        }

        @Override
        public boolean isAllowed() {
            return WeaponModuleConfig.bladeLauncherIsAllowed;
        }

        @Override
        public int getEnergyUsage() {
            return (int) Math.round(applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION));
        }

        @Override
        public int getTier() {
            return 1;
        }
    }
}
