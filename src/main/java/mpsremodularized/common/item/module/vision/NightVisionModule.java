package mpsremodularized.common.item.module.vision;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PlayerTickModule;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.config.module.VisionModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;

import javax.annotation.Nonnull;

public class NightVisionModule extends AbstractPowerModule {
    private static final String LEASE = "mpsremodularized:night_vision_lease";
    private static final int DURATION = 300;

    /** Ownership lives on the wearer, so removing the host cannot strand its effect. */
    public static void beforePlayerTick(Player player) {
        if (player.level().isClientSide() || !player.getPersistentData().contains(LEASE)) return;
        if (!ownsEffect(player)) {
            player.getPersistentData().remove(LEASE);
            return;
        }
        IModularItem helmet = MPSCapabilities.getModularItem(player.getItemBySlot(EquipmentSlot.HEAD));
        if (helmet == null || !helmet.isModuleOnline(MPSConstants.NIGHT_VISION_MODULE)) release(player);
    }

    private static boolean ownsEffect(Player player) {
        if (!player.getPersistentData().contains(LEASE)) return false;
        var lease = player.getPersistentData().getCompound(LEASE);
        MobEffectInstance effect = player.getEffect(MobEffects.NIGHT_VISION);
        long remaining = lease.getInt("duration") - (player.level().getGameTime() - lease.getLong("tick"));
        return effect != null && remaining > 0 && effect.getDuration() == remaining
                && effect.getAmplifier() == 0 && !effect.isAmbient() && !effect.isVisible() && !effect.showIcon();
    }

    private static void release(Player player) {
        // A potion may extend/replace our effect. Only remove the effect whose lease still matches.
        if (ownsEffect(player)) player.removeEffect(MobEffects.NIGHT_VISION);
        player.getPersistentData().remove(LEASE);
    }

    public static class Ticker extends PlayerTickModule {
        public Ticker(@Nonnull ItemStack module) {
            super(module, ModuleCategory.VISION, ModuleTarget.HEADONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, VisionModuleConfig.nightVisionEnergyConsumption, "FE/t");
        }

        @Override
        public boolean isAllowed() {
            return VisionModuleConfig.nightVisionModuleIsAllowed;
        }

        @Override
        public boolean onPlayerTickActive(Player player, Level level, @Nonnull ItemStack item, int moduleIndex) {
            if (level.isClientSide) {
                return false;
            }
            // FE storage is integral; a positive fractional configured cost must not become free.
            int powerDrain = (int) Math.ceil(applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION));

            boolean owned = ownsEffect(player);
            if (player.hasEffect(MobEffects.NIGHT_VISION) && !owned) {
                player.getPersistentData().remove(LEASE);
                return false;
            }
            if (ElectricItemUtils.getPlayerEnergy(player) < powerDrain) {
                release(player);
                return false;
            }
            // Keep >=200 ticks to avoid vanilla's short-duration brightness pulse.
            var effect = new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION, 0, false, false);
            if (CommonHooks.canMobEffectBeApplied(player, effect, (Entity) null) && player.addEffect(effect)) {
                var lease = new net.minecraft.nbt.CompoundTag();
                lease.putInt("duration", DURATION);
                lease.putLong("tick", level.getGameTime());
                player.getPersistentData().put(LEASE, lease);
                ElectricItemUtils.drainPlayerEnergy(player, powerDrain, false);
            }
            return false;
        }

        @Override
        public boolean onPlayerTickInactive(Player player, Level level, @Nonnull ItemStack item, int moduleIndex) {
            if (!level.isClientSide()) release(player);
            return false;
        }
    }
}
