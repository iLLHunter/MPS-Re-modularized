package mpsremodularized.common.item.module.environmental;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import mpsremodularized.common.capabilities.module.toggleable.IToggleableModule;
import mpsremodularized.common.item.module.AbstractPowerModule;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSItems;
import mpsremodularized.common.utils.ElectricItemUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ShockModule extends AbstractPowerModule {
    public static final String DAMAGE_SLIDER = "Shock Damage";
    public static final String RANGE = "Shock Range";
    public static final String ENERGY = "Shock Energy Per Mob";

    public static final class Capability extends PowerModule implements IToggleableModule {
        public Capability(ItemStack stack) {
            super(stack, ModuleCategory.ENVIRONMENTAL, ModuleTarget.TORSOONLY, true);
            addBaseProperty(DAMAGE_SLIDER, 50.0 / 9, "damage");
            addTradeoffProperty(DAMAGE_SLIDER, DAMAGE_SLIDER, 50.0 * 8 / 9);
            addBaseProperty(RANGE, 1, "m");
            addTradeoffProperty(DAMAGE_SLIDER, RANGE, 8);
            addBaseProperty(ENERGY, 1_000_000.0 / 9, "FE/mob");
            addTradeoffProperty(DAMAGE_SLIDER, ENERGY, 1_000_000.0 * 8 / 9);
            addPropertyModifier(ENERGY, (tag, value) -> Math.ceil(Math.clamp(value, 1_000_000.0 / 9, 1_000_000)));
        }
        public double scale() {
            double damage = applyPropertyModifiers(DAMAGE_SLIDER);
            return Double.isFinite(damage) ? Math.clamp(damage / 50, 1.0 / 9, 1) : 1.0 / 9;
        }
        @Override public int getEnergyUsage() { return (int) Math.ceil(1_000_000 * scale()); }
    }

    /** Runs once at entry to ServerPlayer.hurt, including hits later rejected by shields, armor or immunity. */
    public static void onMobHit(Player player, DamageSource source, float intendedDamage) {
        if (player.level().isClientSide() || !(intendedDamage > 0) || !(source.getEntity() instanceof Mob)) return;
        var host = MPSCapabilities.getModularItem(player.getItemBySlot(EquipmentSlot.CHEST));
        if (host == null) return;
        int index = host.findInstalledModule(MPSItems.SHOCK_MODULE.getId());
        if (index < 0 || !(host.getModuleCapability(host.getStackInSlot(index)) instanceof Capability module)
                || !host.isModuleOperational(module)) return;
        double scale = module.scale(), range = 9 * scale;
        int cost = module.getEnergyUsage();
        for (Mob target : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(range),
                mob -> mob.isAlive() && mob.distanceToSqr(player) <= range * range)) {
            if (ElectricItemUtils.getPlayerEnergy(player) < cost) continue;
            // One charge for each shocked mob, even if that mob's native damage rules reject health loss.
            ElectricItemUtils.drainPlayerEnergy(player, cost, false);
            var shock = player.damageSources().playerAttack(player);
            if (target instanceof EnderDragon dragon) dragon.hurt(dragon.head, shock, (float) (50 * scale));
            else {
                target.hurt(shock, (float) (50 * scale));
                repel(target, player);
            }
        }
    }

    private static void repel(Mob target, Player player) {
        var distance = player.blockPosition().subtract(target.blockPosition());
        double x = distance.getX(), y = distance.getY(), z = distance.getZ();
        double squared = x * x + y * y + z * z;
        if (squared == 0) return;
        double denominator = squared * squared;
        double dx = -(x * 0.01999999955296516 / denominator) * Math.pow(6, 3);
        double dy = -(y * 0.01999999955296516 / denominator) * Math.pow(6, 3);
        double dz = -(z * 0.01999999955296516 / denominator) * Math.pow(6, 3);
        dx = Math.signum(dx) * 0.22;
        dz = Math.signum(dz) * 0.22;
        if (dy > 0.2 || dy < -0.1) dy = 0.12;
        target.setDeltaMovement(target.getDeltaMovement().add(dx, dy, dz));
        target.hurtMarked = true;
    }
}
