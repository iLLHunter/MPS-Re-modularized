package mpsremodularized.common.item.module.environmental;

import java.util.ArrayList;
import java.util.List;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import mpsremodularized.common.capabilities.module.toggleable.IToggleableModule;
import mpsremodularized.common.item.module.AbstractPowerModule;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSItems;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.ItemUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public final class BlockReachModule extends AbstractPowerModule {
    public static final String REACH = "Block Reach";
    public static final double MAX_REACH = 59;
    public static final int FE_PER_BLOCK = 1_000;

    public static final class Capability extends PowerModule implements IToggleableModule {
        public Capability(ItemStack stack) {
            super(stack, ModuleCategory.ENVIRONMENTAL, ModuleTarget.TORSOONLY, true);
            addBaseProperty(REACH, Player.DEFAULT_BLOCK_INTERACTION_RANGE + 1, "blocks");
            addTradeoffProperty(REACH, REACH, MAX_REACH - Player.DEFAULT_BLOCK_INTERACTION_RANGE - 1);
        }

        public double reach(Player player) {
            double value = applyPropertyModifiers(REACH);
            double setting = Double.isFinite(value) ? Math.clamp((value - 5.5) / 53.5, 0, 1) : 0;
            double minimum = vanillaReach(player) + 1;
            return minimum + setting * (MAX_REACH - minimum);
        }
    }

    public static double vanillaReach(Player player) {
        return Player.DEFAULT_BLOCK_INTERACTION_RANGE + (player.getAbilities().instabuild ? 0.5 : 0);
    }

    public static Capability online(Player player) {
        var chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof mpsremodularized.common.item.electric.armor.AbstractElectricArmor armor)
                || armor.getEquipmentSlot() != EquipmentSlot.CHEST) return null;
        var host = MPSCapabilities.getModularItem(chest);
        if (host == null) return null;
        int index = host.findInstalledModule(MPSItems.BLOCK_REACH_MODULE.getId());
        return index >= 0 && host.getModuleCapability(host.getStackInSlot(index)) instanceof Capability module
                && host.isModuleOperational(module) ? module : null;
    }

    public static int actionCost(Player player, double distance) {
        return (int) (Math.ceil(Math.max(0, distance - vanillaReach(player))) * FE_PER_BLOCK);
    }

    /** Mining packets contain a position; use vanilla's eye-to-block-box distance. */
    public static double blockDistance(Player player, BlockPos pos) {
        return Math.sqrt(new AABB(pos).distanceToSqr(player.getEyePosition()));
    }

    public static final class Payment {
        private record Debit(ItemStack stack, int energy) {}
        private final Player player;
        private final List<Debit> debits = new ArrayList<>();
        public boolean blockRemoved;

        private Payment(Player player) { this.player = player; }

        public void finish(boolean successful) {
            if (!successful) {
                for (var debit : debits) {
                    int remaining = debit.energy();
                    while (remaining > 0) {
                        int restored = ElectricItemUtils.chargeItem(debit.stack(), remaining, false);
                        if (restored <= 0) {
                            restored = ElectricItemUtils.giveEquippedPlayerEnergy(player, remaining);
                            if (restored <= 0) throw new IllegalStateException("Cannot restore reserved Block Reach energy");
                        }
                        remaining -= restored;
                    }
                }
            }
            if (!debits.isEmpty()) player.getInventory().setChanged();
        }
    }

    /** Reserve the action's FE so native powered tools cannot spend it. Failed actions refund it. */
    public static Payment prepare(Player player, double distance) {
        var module = online(player);
        var payment = new Payment(player);
        if (module == null) return payment;
        if (!Double.isFinite(distance) || distance > module.reach(player)) return null;
        int cost = actionCost(player, distance);
        // Retain MPS's established Creative energy exemption.
        if (cost == 0 || player.getAbilities().instabuild) return payment;
        if (ElectricItemUtils.drainPlayerEnergy(player, cost, true) < cost) return null;
        int remaining = cost;
        for (var slot : EquipmentSlot.values()) {
            var stack = ItemUtils.getItemFromEntitySlot(player, slot);
            int withdrawn = ElectricItemUtils.drainItem(stack, remaining, false);
            if (withdrawn > 0) payment.debits.add(new Payment.Debit(stack, withdrawn));
            remaining -= withdrawn;
            if (remaining == 0) break;
        }
        if (remaining > 0) {
            payment.finish(false);
            return null;
        }
        return payment;
    }
}
