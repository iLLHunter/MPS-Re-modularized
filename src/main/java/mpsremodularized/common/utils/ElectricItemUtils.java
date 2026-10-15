package mpsremodularized.common.utils;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class ElectricItemUtils {
    /** Source-funded charging, with equipment first and each actual inventory stack visited once. */
    public static int chargePlayerFromSource(LivingEntity entity, IEnergyStorage source, int maximum) {
        if (maximum <= 0 || entity.level().isClientSide()) {
            return 0;
        }
        int remaining = maximum;
        Set<ItemStack> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = ItemUtils.getItemFromEntitySlot(entity, slot);
            if (visited.add(stack)) {
                remaining -= chargeFromSource(source, stack, remaining);
            }
        }
        if (entity instanceof Player player) {
            for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (visited.add(stack)) {
                    remaining -= chargeFromSource(source, stack, remaining);
                }
            }
            if (remaining < maximum) {
                player.getInventory().setChanged();
            }
        }
        return maximum - remaining;
    }

    private static int chargeFromSource(IEnergyStorage source, ItemStack stack, int maximum) {
        IEnergyStorage target = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return target == null ? 0 : transferEnergy(source, target, maximum);
    }

    /** Simulation reserves no energy; the committed receiver is funded by the actual withdrawal. */
    public static int transferEnergy(IEnergyStorage source, IEnergyStorage target, int maximum) {
        if (maximum <= 0) {
            return 0;
        }
        int available = source.extractEnergy(maximum, true);
        if (available <= 0) return 0;
        int accepted = target.receiveEnergy(available, true);
        if (accepted <= 0) return 0;
        int withdrawn = source.extractEnergy(accepted, false);
        if (withdrawn <= 0) return 0;
        return target.receiveEnergy(withdrawn, false);
    }

    /**
     * returns the sum of the energy of the entity's equipped items
     *
     * Note here we filter out foreign items so the entity available/ usableenergy isn't wrong
     */
    public static double getPlayerEnergy(LivingEntity entity) {
        double avail = 0;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            avail += getItemEnergy(ItemUtils.getItemFromEntitySlot(entity, slot));
        }
        return avail;
    }

    /**
     * returns the total possible amount of energy the entity's equipped items can hold
     *
     * Note here we filter out foreign items so the entity available/ usableenergy isn't wrong
     */
    public static double getMaxPlayerEnergy(LivingEntity entity) {
        double avail = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            avail += getMaxItemEnergy(ItemUtils.getItemFromEntitySlot(entity, slot));
        }
        return avail;
    }

    /**
     * returns the total amount of energy drained from the entity's equipped items
     *
     * Note that charging held items while in use causes issues so they are skipped
     */
    public static double drainPlayerEnergy(LivingEntity entity, double drainAmount, boolean simulate){
        double drainleft = drainAmount;
        if (entity.level().isClientSide || (entity instanceof Player && ((Player) entity).getAbilities().instabuild)) {
            return drainAmount;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (drainleft <= 0) {
                break;
            }

            ItemStack stack = ItemUtils.getItemFromEntitySlot(entity, slot);
            drainleft = drainleft - drainItem(stack, (int)drainleft, simulate);
        }

        if (!simulate && drainAmount - drainleft > 0 && entity instanceof Player player) {
            player.getInventory().setChanged();
        }
        return drainAmount - drainleft;
    }

    public static int giveEquippedPlayerEnergy(LivingEntity entity, int energyToGive) {
        if (energyToGive <= 0 || entity.level().isClientSide) {
            return 0;
        }

        int remaining = energyToGive;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (remaining <= 0) {
                break;
            }
            remaining -= chargeItem(ItemUtils.getItemFromEntitySlot(entity, slot), remaining, false);
        }

        int accepted = energyToGive - remaining;
        if (accepted > 0 && entity instanceof Player player) {
            player.getInventory().setChanged();
        }
        return accepted;
    }

    /**
     * returns the total amount of energy given to a entity's equipped items
     *
     * Note that charging held items while in use causes issues so they are skipped
     */
    public static double givePlayerEnergy(LivingEntity entity, double rfToGive, boolean simulate) {
        if(rfToGive <= 0) {
            return 0;
        }

        double rfLeft = rfToGive;
        // Charges equipped items first
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (rfLeft > 0) {
                ItemStack stack = ItemUtils.getItemFromEntitySlot(entity, slot);
                rfLeft = rfLeft - chargeItem(stack, (int)rfLeft, simulate);
            } else {
                break;
            }
        }
        // charge other compatible items in inventory
        if(entity instanceof Player player) {
            if (rfLeft > 0) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (rfLeft > 0) {
                        rfLeft = rfLeft - chargeItem(player.getInventory().getItem(i), (int) rfLeft, simulate);
                    } else {
                        break;
                    }
                }

                if (rfToGive - rfLeft > 0) {
                    player.getInventory().setChanged();
                }
            }
        }
        return rfToGive - rfLeft;
    }

    /**
     * returns the energy an itemStack has
     */
    public static int drainItem(@Nonnull ItemStack itemStack, double drainAmount, boolean simulate) {
//        if (BlackList.blacklistModIds.mpsremodularized$contains(ItemUtils.getRegistryName(itemStack).getNamespace())) {
//            return 0;
//        }
        IEnergyStorage iEnergyStorage = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (iEnergyStorage != null) {
            return iEnergyStorage.extractEnergy((int)drainAmount, simulate);
        }
        return 0;
    }

    /**
     * returns the energy an itemStack has
     */
    public static int getItemEnergy(@Nonnull ItemStack itemStack) {
        IEnergyStorage iEnergyStorage = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (iEnergyStorage != null) {
            return iEnergyStorage.getEnergyStored();
        }
        return 0;
    }

    /**
     * returns total possible energy an itemStack can hold
     * Note: safe to use an integer as return value since values are stored as integers anyway
     */
    public static int getMaxItemEnergy(@Nonnull ItemStack itemStack) {
        IEnergyStorage iEnergyStorage = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (iEnergyStorage != null) {
            return iEnergyStorage.getMaxEnergyStored();
        }
        return 0;
    }

    public static int chargeItem(@Nonnull ItemStack itemStack, double chargeAmount, boolean simulate) {
        IEnergyStorage iEnergyStorage = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (iEnergyStorage != null) {
            return iEnergyStorage.receiveEnergy((int)chargeAmount, simulate);
        }
        return 0;
    }

    /**
     * Hacky way of determining if item is a power source.
     * @return
     */
    static double getMaxEnergyForComparison() {
        return 0;
//        return (0.8 * (MPSCapabilities.getCapability(new ItemStack(NuminaObjects.BASIC_BATTERY.get()), Capabilities.EnergyStorage.ITEM)).map(IEnergyStorage::getMaxEnergyStored).orElse(0));
    }
}
