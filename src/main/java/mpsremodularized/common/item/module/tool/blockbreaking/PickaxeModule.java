package mpsremodularized.common.item.module.tool.blockbreaking;

import mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.config.module.PickaxeModuleConfig;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PickaxeModule extends AbstractPowerModule {
    public static class BlockBreaker extends PowerModule implements IBlockBreakingModule {
        int tier;

        public BlockBreaker(ItemStack module, int tier) {
            super(module, ModuleCategory.PICKAXE, ModuleTarget.TOOLONLY);
            this.tier = tier;
            //            MPSLogger.logDebug("module: " + module + ", tier: " + tier + ", isAllowed: " + isAllowed());

            switch(tier) {
                case 1 -> {
                    addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, PickaxeModuleConfig.stonePickAxeModuleEnergyConsumptionBase, "FE");
                    addBaseProperty(MPSConstants.HARVEST_SPEED, PickaxeModuleConfig.stonePickAxeModuleHarvestSpeedBase, "x");
                    addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.ENERGY_CONSUMPTION, PickaxeModuleConfig.stonePickAxeModuleEnergyConsumptionOverclockMultiplier);
                    addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.HARVEST_SPEED, PickaxeModuleConfig.stonePickAxeModuleHarvestSpeedOverclockMultiplier);
                }
                case 2 -> {
                    addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, PickaxeModuleConfig.ironPickAxeModuleEnergyConsumptionBase, "FE");
                    addBaseProperty(MPSConstants.HARVEST_SPEED, PickaxeModuleConfig.ironPickAxeModuleHarvestSpeedBase, "x");
                    addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.ENERGY_CONSUMPTION, PickaxeModuleConfig.ironPickAxeModuleEnergyConsumptionOverclockMultiplier);
                    addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.HARVEST_SPEED, PickaxeModuleConfig.ironPickAxeModuleHarvestSpeedOverclockMultiplier);
                }
                case 3 -> {
                    addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, PickaxeModuleConfig.diamondPickAxeModuleEnergyConsumptionBase, "FE");
                    addBaseProperty(MPSConstants.HARVEST_SPEED, PickaxeModuleConfig.diamondPickAxeModuleHarvestSpeedBase, "x");
                    addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.ENERGY_CONSUMPTION, PickaxeModuleConfig.diamondPickAxeModuleEnergyConsumptionOverclockMultiplier);
                    addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.HARVEST_SPEED, PickaxeModuleConfig.diamondPickAxeModuleHarvestSpeedOverclockMultiplier);
                }
                case 4 -> {
                    addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, PickaxeModuleConfig.netheritePickAxeModuleEnergyConsumptionBase, "FE");
                    addBaseProperty(MPSConstants.HARVEST_SPEED, PickaxeModuleConfig.netheritePickAxeModuleHarvestSpeedBase, "x");
                    addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.ENERGY_CONSUMPTION, PickaxeModuleConfig.netheritePickAxeModuleEnergyConsumptionOverclockMultiplier);
                    addTradeoffProperty(MPSConstants.OVERCLOCK, MPSConstants.HARVEST_SPEED, PickaxeModuleConfig.netheritePickAxeModuleHarvestSpeedOverclockMultiplier);
                }
            }
        }

        @Override
        public boolean mineBlock(ItemStack powerFist, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving, double playerEnergy) {
//            MPSLogger.logDebug("energy usage: " + getEnergyUsage() +", player energy: " + playerEnergy);

            if (this.canHarvestBlock(powerFist, state, (Player) entityLiving, pos, playerEnergy)) {
                ElectricItemUtils.drainPlayerEnergy(entityLiving, getEnergyUsage(), false);
//                MPSLogger.logDebug("returning true");
                return true;
            }
//            MPSLogger.logDebug("returning false");
            return false;
        }

        @Override
        public void handleBreakSpeed(PlayerEvent.BreakSpeed event) {
            event.setNewSpeed((float) (event.getNewSpeed() * applyPropertyModifiers(MPSConstants.HARVEST_SPEED)));
        }

        @Override
        public int getEnergyUsage() {
            return (int) applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
        }

        @Override
        public ItemStack getEmulatedTool() {
            return switch (tier) {
                case 1 -> new ItemStack(Items.STONE_PICKAXE);
                case 2 -> new ItemStack(Items.IRON_PICKAXE);
                case 3 -> new ItemStack(Items.DIAMOND_PICKAXE);
                case 4 -> new ItemStack(Items.NETHERITE_PICKAXE);
                default -> new ItemStack(Items.STICK);
            };
        }

        @Override
        public boolean isAllowed() {
            return switch (tier) {
                case 1 -> PickaxeModuleConfig.stonePickAxeModuleIsAllowed;
                case 2 -> PickaxeModuleConfig.ironPickAxeModuleIsAllowed;
                case 3 -> PickaxeModuleConfig.diamondPickAxeModuleIsAllowed;
                case 4 -> PickaxeModuleConfig.netheritePickAxeModuleIsAllowed;
                default -> false;
            };
        }

        @Override
        public String getModuleGroup() {
            return "Pickaxe";
        }

        @Override
        public int getTier() {
            return this.tier;
        }
    }
}
