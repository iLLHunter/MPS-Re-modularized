package mpsremodularized.common.item.module.environmental;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PlayerTickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.config.module.EnvironmentalModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ActiveCamouflageModule extends AbstractPowerModule {
    private static final Holder<MobEffect> invisibility = MobEffects.INVISIBILITY;
    private static final int EFFECT_TICKS = 2;

    public static class Ticker extends PlayerTickModule {
        public Ticker(ItemStack module) {
            super(module, ModuleCategory.ENVIRONMENTAL, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, EnvironmentalModuleConfig.activeCamouflageModuleEnergyConsumptionBase, "FE/t");
        }

        @Override
        public boolean isAllowed() {
            return EnvironmentalModuleConfig.activeCamouflageModuleIsAllowed;
        }

        @Override
        public boolean onPlayerTickActive(Player player, Level level, ItemStack item, int moduleIndex) {
            if (level.isClientSide()) return false;
            MobEffectInstance invis = player.getEffect(invisibility);
            // Leave longer potion/mod effects intact instead of replacing them or claiming ownership.
            if (invis != null && (invis.getDuration() > EFFECT_TICKS || invis.isInfiniteDuration())) return false;
            int energyUsage = getEnergyUsage();
            if (ElectricItemUtils.getPlayerEnergy(player) >= energyUsage) {
                // Refresh a short lease while powered. Switching off or dropping the host expires it naturally.
                if (player.addEffect(new MobEffectInstance(invisibility, EFFECT_TICKS, 0, false, false))) {
                    ElectricItemUtils.drainPlayerEnergy(player, energyUsage, false);
                }
            }
            return false;
        }

        @Override
        public boolean onPlayerTickInactive(Player player, Level level, ItemStack item,  int moduleIndex) {
            // Never remove potion/other-mod invisibility; the module's own two-tick lease expires.
            return false;
        }

        @Override
        public int getEnergyUsage() {
            return (int)applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
        }
    }
}
