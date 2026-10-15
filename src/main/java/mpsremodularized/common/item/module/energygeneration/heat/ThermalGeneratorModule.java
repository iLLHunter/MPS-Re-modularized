package mpsremodularized.common.item.module.energygeneration.heat;

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
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

/**
 * Created by User: Andrew2448
 * 6:43 PM 4/23/13
 */
public class ThermalGeneratorModule extends AbstractPowerModule {
    public static class ThermalGeneratorTickingCapability extends PassivePlayerTickModule {
        final int tier;
        public ThermalGeneratorTickingCapability(ItemStack module, int tier) {
            super(module, ModuleCategory.ENERGY_GENERATION, ModuleTarget.TORSOONLY);
            this.tier = tier;
            switch(tier) {
                case 1 -> {
                    addBaseProperty(MPSConstants.THERMOELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_1_thermoelectricEnergyGeneration);

                    addBaseProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_1_steamElectricEnergyGenerationBase, "FE");
                    addBaseProperty(MPSConstants.STEAM_ELECTRIC_WATER_CONSUMPTION, EnergyGenerationModuleConfig.thermalEnergyGenerator_1_steamElectricWaterConsumptionBase, "mb");
                    addTradeoffProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_1_steamElectricEnergyGenerationMultiplier);
                    addTradeoffProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, MPSConstants.STEAM_ELECTRIC_WATER_CONSUMPTION, EnergyGenerationModuleConfig.thermalEnergyGenerator_1_steamElectricWaterConsumptionMultiplier);
                }
                case 2 -> {
                    addBaseProperty(MPSConstants.THERMOELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_2_thermoelectricEnergyGeneration);

                    addBaseProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_2_steamElectricEnergyGenerationBase, "FE");
                    addBaseProperty(MPSConstants.STEAM_ELECTRIC_WATER_CONSUMPTION, EnergyGenerationModuleConfig.thermalEnergyGenerator_2_steamElectricWaterConsumptionBase, "mb");
                    addTradeoffProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_2_steamElectricEnergyGenerationMultiplier);
                    addTradeoffProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, MPSConstants.STEAM_ELECTRIC_WATER_CONSUMPTION, EnergyGenerationModuleConfig.thermalEnergyGenerator_2_steamElectricWaterConsumptionMultiplier);
                }
                case 3 -> {
                    addBaseProperty(MPSConstants.THERMOELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_3_thermoelectricEnergyGeneration);

                    addBaseProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_3_steamElectricEnergyGenerationBase, "FE");
                    addBaseProperty(MPSConstants.STEAM_ELECTRIC_WATER_CONSUMPTION, EnergyGenerationModuleConfig.thermalEnergyGenerator_3_steamElectricWaterConsumptionBase, "mb");
                    addTradeoffProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_3_steamElectricEnergyGenerationMultiplier);
                    addTradeoffProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, MPSConstants.STEAM_ELECTRIC_WATER_CONSUMPTION, EnergyGenerationModuleConfig.thermalEnergyGenerator_3_steamElectricWaterConsumptionMultiplier);
                }
                case 4 -> {
                    addBaseProperty(MPSConstants.THERMOELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_4_thermoelectricEnergyGeneration);

                    addBaseProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_4_steamElectricEnergyGenerationBase, "FE");
                    addBaseProperty(MPSConstants.STEAM_ELECTRIC_WATER_CONSUMPTION, EnergyGenerationModuleConfig.thermalEnergyGenerator_4_steamElectricWaterConsumptionBase, "mb");
                    addTradeoffProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, EnergyGenerationModuleConfig.thermalEnergyGenerator_4_steamElectricEnergyGenerationMultiplier);
                    addTradeoffProperty(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, MPSConstants.STEAM_ELECTRIC_WATER_CONSUMPTION, EnergyGenerationModuleConfig.thermalEnergyGenerator_4_steamElectricWaterConsumptionMultiplier);
                }
            }
        }

        private static final int TICKS_PER_ACTION = 20;

        @Override
        public boolean onPlayerTickActive(@NotNull Player player, Level level, @NotNull ItemStack item, int moduleIndex) {
            if (level.isClientSide || level.getGameTime() % TICKS_PER_ACTION != 0
                || ElectricItemUtils.getPlayerEnergy(player) >= ElectricItemUtils.getMaxPlayerEnergy(player)) {
                return false;
            }

            HeatUtils.PlayerHeat heat = HeatUtils.getPlayerHeat(player);
            if (heat.currentHeat() <= 0 || heat.maxHeat() <= 0) {
                return false;
            }

            IFluidHandlerItem fluidHandler = item.getCapability(Capabilities.FluidHandler.ITEM);
            int waterAmount = 0;
            if (fluidHandler != null && fluidHandler.getTanks() > 0) {
                FluidStack fluid = fluidHandler.getFluidInTank(0);
                if (fluid.is(Fluids.WATER)) {
                    waterAmount = fluid.getAmount();
                }
            }

            double requiredWater = TICKS_PER_ACTION * applyPropertyModifiers(MPSConstants.STEAM_ELECTRIC_WATER_CONSUMPTION);
            double waterToUse = 0;
            double baseEnergy = 0;
            if (waterAmount > 0 && requiredWater > 0) {
                waterToUse = Math.min(waterAmount, requiredWater);
                baseEnergy = TICKS_PER_ACTION * applyPropertyModifiers(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION)
                    * waterToUse / requiredWater;
            } else {
                double multiplier = player.isOnFire() ? 4 : heat.currentHeat() >= 200 ? 2
                    : heat.currentHeat() / heat.maxHeat() >= 0.5 ? 1 : 0;
                baseEnergy = TICKS_PER_ACTION * applyPropertyModifiers(MPSConstants.THERMOELECTRIC_ENERGY_GENERATION) * multiplier;
            }

            ThermalGenerationPlan plan = ThermalGenerationPlan.create(heat.currentHeat(), heat.maxHeat(), baseEnergy, waterToUse);
            int acceptedEnergy = ElectricItemUtils.giveEquippedPlayerEnergy(player, plan.energy());
            if (acceptedEnergy <= 0) {
                return false;
            }

            // Only spend resources for the portion that actually charged the equipped batteries.
            if (waterToUse > 0) {
                fluidHandler = item.getCapability(Capabilities.FluidHandler.ITEM);
                if (fluidHandler != null) {
                    fluidHandler.drain(plan.waterUsed(acceptedEnergy), IFluidHandler.FluidAction.EXECUTE);
                }
            }
            HeatUtils.coolPlayer(player, plan.heatUsed(acceptedEnergy));
            return false;
        }

        @Override
        public boolean isAllowed() {
            return switch (tier) {
                case 1-> EnergyGenerationModuleConfig.thermalEnergyGenerator_1_isAllowed;
                case 2-> EnergyGenerationModuleConfig.thermalEnergyGenerator_2_isAllowed;
                case 3-> EnergyGenerationModuleConfig.thermalEnergyGenerator_3_isAllowed;
                case 4-> EnergyGenerationModuleConfig.thermalEnergyGenerator_4_isAllowed;
                default -> false;
            };
        }

        @Override
        public boolean onPlayerTickInactive(@NotNull Player player, @NotNull Level level, @NotNull ItemStack item, int moduleIndex) {
            return super.onPlayerTickInactive(player, level, item, moduleIndex);
        }
    }
}
