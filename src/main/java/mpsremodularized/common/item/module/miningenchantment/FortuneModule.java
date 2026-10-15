package mpsremodularized.common.item.module.miningenchantment;


import mpsremodularized.common.capabilities.module.enchantment.EnchantmentModule;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.utils.TagUtils;
import mpsremodularized.common.config.module.MiningEnchantmentModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class FortuneModule extends AbstractPowerModule {
    public static class TickingEnchantment extends EnchantmentModule {
        boolean added;
        boolean removed;
        public TickingEnchantment(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MINING_ENCHANTMENT, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, MiningEnchantmentModuleConfig.fortuneModuleEnergyConsumptionBase, "FE");
            addTradeoffProperty(MPSConstants.ENCHANTMENT_LEVEL, MPSConstants.ENERGY_CONSUMPTION, MiningEnchantmentModuleConfig.fortuneModuleEnergyConsumptionEnchantmentMultiplier);
            addIntTradeoffProperty(MPSConstants.ENCHANTMENT_LEVEL, MPSConstants.FORTUNE_ENCHANTMENT_LEVEL, 3, "", 1, 1);

            // setting to and loading these just allow values to be persistant when capability reloads
            added = TagUtils.getModuleBoolean(module, "added");
            removed = TagUtils.getModuleBoolean(module, "removed");
        }

        @Override
        public int getEnergyUsage() {
            return (int) applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
        }

        @Override
        public Holder<Enchantment> getEnchantment(Level level) {
            return level.holderOrThrow(Enchantments.FORTUNE);
        }

        @Override
        public int getLevel() {
            return Math.clamp((int) applyPropertyModifiers(MPSConstants.FORTUNE_ENCHANTMENT_LEVEL), 1, 3);
        }

        @Override
        public boolean isAllowed() {
            return MiningEnchantmentModuleConfig.fortuneModuleIsAllowed;
        }
    }
}
