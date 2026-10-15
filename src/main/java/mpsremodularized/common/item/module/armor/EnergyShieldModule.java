package mpsremodularized.common.item.module.armor;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.toggleable.ToggleableModule;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.config.module.ArmorModuleConfig;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.item.ItemStack;

public class EnergyShieldModule extends AbstractPowerModule {
    public static class EnergyShieldCapabilityWrapper extends ToggleableModule {
        public EnergyShieldCapabilityWrapper(ItemStack module) {
            super(module, ModuleCategory.ARMOR, ModuleTarget.ARMORONLY);
            addTradeoffProperty(MPSConstants.MODULE_FIELD_STRENGTH, MPSConstants.ARMOR_VALUE_ENERGY, ArmorModuleConfig.energyShieldFieldStrengthMultiplier, MPSConstants.MODULE_TRADEOFF_PREFIX + MPSConstants.ARMOR_POINTS);
            addTradeoffProperty(MPSConstants.MODULE_FIELD_STRENGTH, MPSConstants.ARMOR_ENERGY_CONSUMPTION, ArmorModuleConfig.energyShieldEnergyConsumptionMultiplier, "FE");
            addTradeoffProperty(MPSConstants.MODULE_FIELD_STRENGTH, MPSConstants.MAXIMUM_HEAT, ArmorModuleConfig.energyShieldMaxHeatMultiplier);
            addTradeoffProperty(MPSConstants.MODULE_FIELD_STRENGTH, MPSConstants.ARMOR_TOUGHNESS, ArmorModuleConfig.energyShieldToughnessMultiplier);
            addTradeoffProperty(MPSConstants.MODULE_FIELD_STRENGTH, MPSConstants.KNOCKBACK_RESISTANCE, ArmorModuleConfig.energyShieldKnockBackResistanceMultiplier);
        }

        @Override
        public String getModuleGroup() {
            return "Armor";
        }

        @Override
        public int getTier() {
            return 4;
        }

        @Override
        public boolean isAllowed() {
            return ArmorModuleConfig.energyShieldIsAllowed;
        }
    }
}
