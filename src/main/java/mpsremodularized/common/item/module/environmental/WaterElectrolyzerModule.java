package mpsremodularized.common.item.module.environmental;

import mpsremodularized.client.config.MPSClientConfig;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PlayerTickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.client.sound.MPSSoundDictionary;
import mpsremodularized.common.config.module.EnvironmentalModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WaterElectrolyzerModule extends AbstractPowerModule {

    public static class Ticker extends PlayerTickModule {

        public Ticker(ItemStack module) {
            super(module, ModuleCategory.ENVIRONMENTAL, ModuleTarget.HEADONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, EnvironmentalModuleConfig.waterElectrolyzerEnergyConsumptionBase, "FE");
        }

        @Override
        public boolean isAllowed() {
            return EnvironmentalModuleConfig.waterElectrolyzerIsAllowed;
        }

        @Override
        public boolean onPlayerTickActive(Player player, Level level, ItemStack item, int moduleIndex) {
            double energy = ElectricItemUtils.getPlayerEnergy(player);
            double energyConsumption = Math.round(applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION));
            if (energy >= energyConsumption && player.getAirSupply() < 10) {
                if ((level.isClientSide()) && MPSClientConfig.useSounds) {
                    player.playSound(MPSSoundDictionary.SOUND_EVENT_ELECTROLYZER.get(), 1.0f, 1.0f);
                }
                ElectricItemUtils.drainPlayerEnergy(player, energyConsumption, false);
                player.setAirSupply(300);
            }
            return false;
        }
    }
}
