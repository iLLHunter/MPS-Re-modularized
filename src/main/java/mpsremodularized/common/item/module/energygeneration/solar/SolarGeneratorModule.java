package mpsremodularized.common.item.module.energygeneration.solar;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PassivePlayerTickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.HeatUtils;
import mpsremodularized.common.config.module.EnergyGenerationModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.NotNull;

public class SolarGeneratorModule extends AbstractPowerModule {

    public static class SolarGeneratorTickingCapability extends PassivePlayerTickModule {
        private static final int TICKS_PER_ACTION = 20;
        // Keep the heat rate from the original 80-tick cadence, including saved config values.
        private static final double HEAT_PER_ACTION_SCALE = TICKS_PER_ACTION / 80.0;
        int tier;
        public SolarGeneratorTickingCapability(ItemStack module, int tier) {
            super(module, ModuleCategory.ENERGY_GENERATION, ModuleTarget.HEADONLY);
            this.tier = tier;

            switch(tier) {
                case 1-> {
                    addBaseProperty(MPSConstants.ENERGY_GENERATION_DAY, EnergyGenerationModuleConfig.solarGeneratorModule_1_energyGenerationDay, "FE");
                    addBaseProperty(MPSConstants.ENERGY_GENERATION_NIGHT, EnergyGenerationModuleConfig.solarGeneratorModule_1_energyGenerationNight, "FE");
                    addBaseProperty(MPSConstants.HEAT_GENERATION_DAY, EnergyGenerationModuleConfig.solarGeneratorModule_1_heatGenerationDay * HEAT_PER_ACTION_SCALE);
                    addBaseProperty(MPSConstants.HEAT_GENERATION_NIGHT, EnergyGenerationModuleConfig.solarGeneratorModule_1_heatGenerationNight * HEAT_PER_ACTION_SCALE);
                }
                case 2 ->{
                    addBaseProperty(MPSConstants.ENERGY_GENERATION_DAY, EnergyGenerationModuleConfig.solarGeneratorModule_2_energyGenerationDay, "FE");
                    addBaseProperty(MPSConstants.ENERGY_GENERATION_NIGHT, EnergyGenerationModuleConfig.solarGeneratorModule_2_energyGenerationNight, "FE");
                    addBaseProperty(MPSConstants.HEAT_GENERATION_DAY, EnergyGenerationModuleConfig.solarGeneratorModule_2_heatGenerationDay * HEAT_PER_ACTION_SCALE);
                    addBaseProperty(MPSConstants.HEAT_GENERATION_NIGHT, EnergyGenerationModuleConfig.solarGeneratorModule_2_heatGenerationNight * HEAT_PER_ACTION_SCALE);
                }

                case 3 -> {
                    addBaseProperty(MPSConstants.ENERGY_GENERATION_DAY, EnergyGenerationModuleConfig.solarGeneratorModule_3_energyGenerationDay, "FE");
                    addBaseProperty(MPSConstants.ENERGY_GENERATION_NIGHT, EnergyGenerationModuleConfig.solarGeneratorModule_3_energyGenerationNight, "FE");
                    addBaseProperty(MPSConstants.HEAT_GENERATION_DAY, EnergyGenerationModuleConfig.solarGeneratorModule_3_heatGenerationDay * HEAT_PER_ACTION_SCALE);
                    addBaseProperty(MPSConstants.HEAT_GENERATION_NIGHT, EnergyGenerationModuleConfig.solarGeneratorModule_3_heatGenerationNight * HEAT_PER_ACTION_SCALE);
                }

                case 4 -> {
                    addBaseProperty(MPSConstants.ENERGY_GENERATION_DAY, EnergyGenerationModuleConfig.solarGeneratorModule_4_energyGenerationDay, "FE");
                    addBaseProperty(MPSConstants.ENERGY_GENERATION_NIGHT, EnergyGenerationModuleConfig.solarGeneratorModule_4_energyGenerationNight, "FE");
                    addBaseProperty(MPSConstants.HEAT_GENERATION_DAY, EnergyGenerationModuleConfig.solarGeneratorModule_4_heatGenerationDay * HEAT_PER_ACTION_SCALE);
                    addBaseProperty(MPSConstants.HEAT_GENERATION_NIGHT, EnergyGenerationModuleConfig.solarGeneratorModule_4_heatGenerationNight * HEAT_PER_ACTION_SCALE);
                }
            }
        }

        @Override
        public boolean isAllowed() {
            switch (tier) {
                case 1-> {
                    return EnergyGenerationModuleConfig.solarGeneratorModule_1_IsAllowed;
                }
                case 2-> {
                    return EnergyGenerationModuleConfig.solarGeneratorModule_2_IsAllowed;
                }
                case 3-> {
                    return EnergyGenerationModuleConfig.solarGeneratorModule_3_IsAllowed;
                }
                case 4-> {
                    return EnergyGenerationModuleConfig.solarGeneratorModule_4_IsAllowed;
                }
            }
            return false;
        }

        @Override
        public boolean onPlayerTickActive(Player player, Level level, @NotNull ItemStack item, int moduleIndex) {
            if (level.isClientSide || level.getGameTime() % TICKS_PER_ACTION != 0
                || ElectricItemUtils.getPlayerEnergy(player) >= ElectricItemUtils.getMaxPlayerEnergy(player)) {
                return false;
            }
            Level world = player.level();
            boolean isRaining, canRain = true;
            if (world.getGameTime() % 20 == 0) {
                canRain = world.getBiome(player.blockPosition()).value().hasPrecipitation();
            }
            isRaining = canRain && (world.isRaining() || world.isThundering());
            boolean sunVisible = world.isDay() && !isRaining && world.canSeeSkyFromBelowWater(player.blockPosition().above());
            boolean moonVisible = !world.isDay() && !isRaining && world.canSeeSkyFromBelowWater(player.blockPosition().above());

            if (world.dimensionType().hasSkyLight()) {
                float lightLevelScaled = (world.getBrightness(LightLayer.SKY, player.blockPosition().above()) - world.getSkyDarken())/15F;

                if (sunVisible) {
                    generate(player, MPSConstants.ENERGY_GENERATION_DAY, MPSConstants.HEAT_GENERATION_DAY, lightLevelScaled);
                } else if (moonVisible) {
                    generate(player, MPSConstants.ENERGY_GENERATION_NIGHT, MPSConstants.HEAT_GENERATION_NIGHT, lightLevelScaled);
                }
            }
            return false;
        }

        private void generate(Player player, String energyProperty, String heatProperty, double lightLevel) {
            int generatedEnergy = (int) (applyPropertyModifiers(energyProperty) * Math.max(0, lightLevel));
            int acceptedEnergy = ElectricItemUtils.giveEquippedPlayerEnergy(player, generatedEnergy);
            if (acceptedEnergy > 0) {
                HeatUtils.heatPlayer(player, applyPropertyModifiers(heatProperty) * lightLevel / 2
                    * acceptedEnergy / generatedEnergy);
            }
        }
    }
}
