package mpsremodularized.common.item.module.fluidstorage;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PassivePlayerTickModule;
import mpsremodularized.common.registration.MPSDataComponents;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.HeatUtils;
import mpsremodularized.common.config.module.FluidStorageConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

public class CoolantTankModule extends AbstractPowerModule {
    public static class FluidHandlerItemStackMPS extends FluidHandlerItemStack {
        /**
         * @param container The container itemStack, data is stored on it directly under a component.
         * @param tier the module tier
         */
        public FluidHandlerItemStackMPS(ItemStack container, int tier) {
            super(() -> MPSDataComponents.ITEM_FLUID_CODEC, container, getCapacity(tier));
        }

        // TODO: different fluids for different tier modules
        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return fluid.getFluid() == Fluids.WATER;
        }
    }

    /*
        returns The maximum capacity of this fluid tank. Set with config value based on tier
    */
    static int getCapacity(int tier) {
        return switch (tier) {
            case 1 -> FluidStorageConfig.coolantTankModuleCapacity1;
            case 2 -> FluidStorageConfig.coolantTankModuleCapacity2;
            case 3 -> FluidStorageConfig.coolantTankModuleCapacity3;
            case 4 -> FluidStorageConfig.coolantTankModuleCapacity4;
            default -> 0;
        };
    }

    public static class Ticker extends PassivePlayerTickModule {
        int tier;
        public Ticker(ItemStack module, int tier) {
            super(module, ModuleCategory.FLUID_STORAGE, ModuleTarget.TORSOONLY);
            this.tier = tier;
            switch (tier) {
                case 1 -> {
                    addBaseProperty(MPSConstants.HEAT_ACTIVATION_PERCENT, FluidStorageConfig.coolantTankModuleHeatActivationPercentBase1);
                    addTradeoffProperty(MPSConstants.ACTIVATION_PERCENT, MPSConstants.HEAT_ACTIVATION_PERCENT, FluidStorageConfig.coolantTankModuleHeatActivationPercentMultiplier1, "%");
                    addTradeoffProperty(MPSConstants.ENERGY_CONSUMPTION, MPSConstants.COOLING_BONUS, FluidStorageConfig.coolantTankModuleEnergyConsumptionCoolingBonusMultiplier1, "%");
                    addTradeoffProperty(MPSConstants.ENERGY_CONSUMPTION, MPSConstants.ENERGY_CONSUMPTION, FluidStorageConfig.coolantTankModuleEnergyConsumptionMultiplier1, "RF/t");
                }
                case 2 -> {
                    addBaseProperty(MPSConstants.HEAT_ACTIVATION_PERCENT, FluidStorageConfig.coolantTankModuleHeatActivationPercentBase2);
                    addTradeoffProperty(MPSConstants.ACTIVATION_PERCENT, MPSConstants.HEAT_ACTIVATION_PERCENT, FluidStorageConfig.coolantTankModuleHeatActivationPercentMultiplier2, "%");
                    addTradeoffProperty(MPSConstants.ENERGY_CONSUMPTION, MPSConstants.COOLING_BONUS, FluidStorageConfig.coolantTankModuleEnergyConsumptionCoolingBonusMultiplier2, "%");
                    addTradeoffProperty(MPSConstants.ENERGY_CONSUMPTION, MPSConstants.ENERGY_CONSUMPTION, FluidStorageConfig.coolantTankModuleEnergyConsumptionMultiplier2, "RF/t");
                }
                case 3 -> {
                    addBaseProperty(MPSConstants.HEAT_ACTIVATION_PERCENT, FluidStorageConfig.coolantTankModuleHeatActivationPercentBase3);
                    addTradeoffProperty(MPSConstants.ACTIVATION_PERCENT, MPSConstants.HEAT_ACTIVATION_PERCENT, FluidStorageConfig.coolantTankModuleHeatActivationPercentMultiplier3, "%");
                    addTradeoffProperty(MPSConstants.ENERGY_CONSUMPTION, MPSConstants.COOLING_BONUS, FluidStorageConfig.coolantTankModuleEnergyConsumptionCoolingBonusMultiplier3, "%");
                    addTradeoffProperty(MPSConstants.ENERGY_CONSUMPTION, MPSConstants.ENERGY_CONSUMPTION, FluidStorageConfig.coolantTankModuleEnergyConsumptionMultiplier3, "RF/t");
                }
                case 4 -> {
                    addBaseProperty(MPSConstants.HEAT_ACTIVATION_PERCENT, FluidStorageConfig.coolantTankModuleHeatActivationPercentBase4);
                    addTradeoffProperty(MPSConstants.ACTIVATION_PERCENT, MPSConstants.HEAT_ACTIVATION_PERCENT, FluidStorageConfig.coolantTankModuleHeatActivationPercentMultiplier4, "%");
                    addTradeoffProperty(MPSConstants.ENERGY_CONSUMPTION, MPSConstants.COOLING_BONUS, FluidStorageConfig.coolantTankModuleEnergyConsumptionCoolingBonusMultiplier4, "%");
                    addTradeoffProperty(MPSConstants.ENERGY_CONSUMPTION, MPSConstants.ENERGY_CONSUMPTION, FluidStorageConfig.coolantTankModuleEnergyConsumptionMultiplier4, "RF/t");
                }
            }
        }

        @Override
        public boolean isAllowed() {
            return switch (this.tier) {
                case 1 -> FluidStorageConfig.coolantTankModuleIsAllowed1;
                case 2 -> FluidStorageConfig.coolantTankModuleIsAllowed2;
                case 3 -> FluidStorageConfig.coolantTankModuleIsAllowed3;
                case 4 -> FluidStorageConfig.coolantTankModuleIsAllowed4;
                default -> false;
            };
        }

        private static final double BASE_COOLING_PER_MB = 1.0 / 5.0;

        @Override
        public boolean onPlayerTickActive(Player player, Level level, ItemStack item, int moduleIndex) {
            if (level.isClientSide) {
                return false;
            }
            boolean ret = false;
            IFluidHandlerItem fluidHandler = getModule().getCapability(Capabilities.FluidHandler.ITEM);
            if (fluidHandler == null) {
                return false;
            }

            // Fill if player in some sort of water
            if (player.getCommandSenderWorld().getGameTime() % 10 == 0) {
                // we only have one tank, so index 0;
                int maxFluid = fluidHandler.getTankCapacity(0);
                int currentFluid = fluidHandler.getFluidInTank(0).getAmount();

                // fill the tank
                if (currentFluid < maxFluid) {
                    BlockPos pos = player.blockPosition();
                    BlockState blockstate = level.getBlockState(pos);

                    // fill by being in water
                    if (player.isInWater() && level.getBlockState(pos).getBlock() != Blocks.BUBBLE_COLUMN) {
                        if (blockstate.getBlock() instanceof BucketPickup && blockstate.getFluidState().getType() == Fluids.WATER) {
                            FluidActionResult pickup = FluidUtil.tryPickUpFluid(getModule(), player, level, pos, Direction.UP);
                            if (pickup.isSuccess()) {
                                getModule().applyComponents(pickup.getResult().getComponents());
                                ret = true;
                            }
                        }
                        // fill by being in the rain or bubble column
                    } else if (player.isInWaterRainOrBubble()) {
                        FluidStack water = new FluidStack(Fluids.WATER, 100);
                        if (fluidHandler.fill(water, IFluidHandler.FluidAction.EXECUTE) > 0) {
                            ret = true;
                            player.playSound(SoundEvents.BUCKET_FILL, 1.0F, 1.0F);
                        }
                    }
                }
            }

            // Apply cooling
            HeatUtils.PlayerHeat heat = HeatUtils.getPlayerHeat(player);
            if (heat.currentHeat() <= 0 || heat.maxHeat() <= 0
                || heat.currentHeat() / heat.maxHeat() < applyPropertyModifiers(MPSConstants.HEAT_ACTIVATION_PERCENT)) {
                return ret;
            }

            double cooled = 0;
            double electricCooling = Math.min(applyPropertyModifiers(MPSConstants.COOLING_BONUS), heat.currentHeat());
            double energyPerHeat = applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
            int energyUsage = (int) Math.ceil(electricCooling * energyPerHeat);
            if (electricCooling > 0 && ElectricItemUtils.getPlayerEnergy(player) >= energyUsage) {
                cooled = HeatUtils.coolPlayer(player, electricCooling);
                ElectricItemUtils.drainPlayerEnergy(player, (int) Math.ceil(cooled * energyPerHeat), false);
            }

            // Water cooling is independent of the electrical cooling budget.
            double coolingPerMb = BASE_COOLING_PER_MB * (1 << (tier - 1));
            double remainingHeat = HeatUtils.getPlayerHeat(player).currentHeat();
            int waterToUse = (int) Math.min(1000, remainingHeat / coolingPerMb);
            FluidStack water = fluidHandler.getFluidInTank(0);
            if (water.is(Fluids.WATER) && waterToUse > 0) {
                int usedWater = fluidHandler.drain(waterToUse, IFluidHandler.FluidAction.EXECUTE).getAmount();
                cooled += HeatUtils.coolPlayer(player, usedWater * coolingPerMb);
            }

            if (cooled > 0) {
                ret = true;
                level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.MASTER, 1.0F, 1.0F);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.5, player.getZ(), 4, 0, 0, 0, 0);
                }
            }
            return ret;
        }
    }

    //    @Override
    //    public void appendHoverText(ItemStack itemStack, TooltipContext context, List<Component> toolTips, TooltipFlag flags) {
    //        super.appendHoverText(itemStack, context, toolTips, flags);
    //        AdditionalInfo.appendHoverText(itemStack, context, toolTips, flags, Screen.hasShiftDown());
    //    }
}
