package mpsremodularized.common.item.module.environmental;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PlayerTickModule;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.common.utils.TagUtils;
import mpsremodularized.common.config.module.EnvironmentalModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import mpsremodularized.mixin.common.entity.LivingEntityFoodEffectsInvoker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AutoFeederModule extends AbstractPowerModule {
    public static class Ticker extends PlayerTickModule {
        public Ticker(ItemStack module) {
            super(module, ModuleCategory.ENVIRONMENTAL, ModuleTarget.HEADONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, EnvironmentalModuleConfig.autoFeederModuleEnergyConsumptionBase);
            addBaseProperty(MPSConstants.AUTO_FEEDER_EFFICIENCY, EnvironmentalModuleConfig.autoFeederModuleEatingEfficiencyBase);
            addTradeoffProperty(MPSConstants.EFFICIENCY, MPSConstants.ENERGY_CONSUMPTION, EnvironmentalModuleConfig.autoFeederModuleEfficiencyEnergyConsumptionMultiplier, "FE");
            addTradeoffProperty(MPSConstants.EFFICIENCY, MPSConstants.AUTO_FEEDER_EFFICIENCY, EnvironmentalModuleConfig.autoFeederModuleEfficiencyEatingEfficiencyMultiplier);
        }

        @Override
        public boolean isAllowed() {
            return EnvironmentalModuleConfig.autoFeederModuleIsAllowed;
        }

        @Override
        public boolean onPlayerTickActive(Player player, Level level, ItemStack modularItemStack, int moduleIndex) {
            if (level.isClientSide()) return false;
            float foodLevel = getFoodLevel(getModule());
            float saturationLevel = getSaturationLevel(getModule());
            Inventory inv = player.getInventory();
            double unitCost = applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION) * 0.5;
            double efficiency = applyPropertyModifiers(MPSConstants.AUTO_FEEDER_EFFICIENCY) * 0.01;
            FoodData foodStats = player.getFoodData();
            int foodNeeded = 20 - foodStats.getFoodLevel();
            boolean storeAll = EnvironmentalModuleConfig.autoFeederModuleUseOldAutoFeeder;

            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (!storeAll && foodLevel >= foodNeeded) break;
                ItemStack stack = inv.getItem(i);
                FoodProperties food = stack.getFoodProperties(player);
                if (food == null || food.nutrition() <= 0 || efficiency <= 0) continue;
                // Hold the original food properties: its last item may become a container.
                while (!stack.isEmpty() && (storeAll || foodLevel < foodNeeded)) {
                    ItemStack remainder = food.usingConvertsTo().map(ItemStack::copy)
                            .orElseGet(() -> stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem() : ItemStack.EMPTY);
                    foodLevel += (float) (food.nutrition() * efficiency);
                    // In 1.21.1 saturation() is already the absolute amount, not a modifier.
                    saturationLevel += (float) (food.saturation() * efficiency);
                    // Invoke the same effects as normal eating without feeding a second time.
                    ((LivingEntityFoodEffectsInvoker) player).mpsremodularized$applyFoodEffects(food);
                    stack.shrink(1);
                    if (!remainder.isEmpty()) {
                        if (stack.isEmpty()) {
                            inv.setItem(i, remainder);
                        } else if (!inv.add(remainder)) {
                            player.drop(remainder, false);
                        }
                    }
                }
            }

            int foodUsed = affordableUnits(Math.min(foodNeeded, (int) foodLevel), unitCost, ElectricItemUtils.getPlayerEnergy(player));
            if (foodUsed > 0) {
                foodStats.setFoodLevel(foodStats.getFoodLevel() + foodUsed);
                foodLevel -= foodUsed;
                ElectricItemUtils.drainPlayerEnergy(player, (int) (unitCost * foodUsed), false);
                // Preserve the existing rule that saturation is delivered only with food.
                int saturationNeeded = (int) (foodStats.getFoodLevel() - foodStats.getSaturationLevel());
                int saturationUsed = affordableUnits(Math.min(saturationNeeded, (int) saturationLevel), unitCost, ElectricItemUtils.getPlayerEnergy(player));
                if (saturationUsed > 0) {
                    foodStats.setSaturation(foodStats.getSaturationLevel() + saturationUsed);
                    saturationLevel -= saturationUsed;
                    ElectricItemUtils.drainPlayerEnergy(player, (int) (unitCost * saturationUsed), false);
                }
            }
            setFoodLevel(modularItemStack, foodLevel, level);
            setSaturationLevel(modularItemStack, saturationLevel, level);
            return false;
        }

        private static int affordableUnits(int requested, double unitCost, double available) {
            // Compare the same whole-FE amount that will actually be drained.
            if (requested <= 0) return 0;
            if ((int) (unitCost * requested) <= available) return requested;
            if ((int) unitCost <= available) return 1;
            return 0;
        }

        // these setters should be called server side only -------------------------------------------------------------
        public void setFoodLevel(ItemStack stack, float food, Level level) {
            if(!level.isClientSide()) {
                IModularItem modularItem = MPSCapabilities.getModularItem(stack);
                if (modularItem != null) {
                    modularItem.setModuleFloat(ItemUtils.getRegistryName(getModule()), MPSConstants.TAG_FOOD, food);
                }
            }
        }

        public void setSaturationLevel(ItemStack stack, float saturation, Level level) {
            if (!level.isClientSide()) {
                IModularItem modularItem = MPSCapabilities.getModularItem(stack);
                if (modularItem != null) {
                    modularItem.setModuleFloat(ItemUtils.getRegistryName(getModule()), MPSConstants.TAG_SATURATION, saturation);
                }
            }
        }
    }

    public static float getFoodLevel(ItemStack stack) {
        return TagUtils.getModuleFloat(stack, MPSConstants.TAG_FOOD);
    }

    public static float getSaturationLevel(ItemStack stack) {
        return TagUtils.getModuleFloat(stack, MPSConstants.TAG_SATURATION);
    }
}
