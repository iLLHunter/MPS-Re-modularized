package mpsremodularized.common.capabilities.energy;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * Energy wrapper to wrap tile energy handler and battery energy handler
 */
public class BlockEnergyWrapper implements IEnergyStorage {
    IEnergyStorage tileEnergy;
    IItemHandler itemHandler;
    boolean dirty = false;
    public BlockEnergyWrapper(IEnergyStorage tileEnergyIn, IItemHandler itemHandlerIn) {
        tileEnergy = tileEnergyIn;
        itemHandler = itemHandlerIn;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean value) {
        this.dirty = value;
    }

    /**
     *
     * @param toReceive energy available to receive
     * @param simulate whether to commit the change
     * @return amount received
     */
    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (this.canReceive() && toReceive > 0) {
            int remaining = toReceive;

            // charge inventory item first
            IEnergyStorage itemEnergyStorage = getItemEnergyHandler();
            if (itemEnergyStorage != null) {
                int itemReceived = itemEnergyStorage.receiveEnergy(toReceive, simulate);
                remaining = remaining - itemReceived;
            }

            int tileReceived = tileEnergy.receiveEnergy(remaining, simulate);
            remaining = remaining - tileReceived;
            if (!simulate && remaining < toReceive) {
                dirty = true;
            }
            return toReceive - remaining;
        }
        return 0;
    }

    /**
     *
     * @param toExtract total amount to try to extract
     * @param simulate whether to commit the change
     * @return amount extracted
     */
    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (this.canExtract() && toExtract > 0) {
            int remaining = toExtract;

            // drain blockEntity first
            int tileExtracted = tileEnergy.extractEnergy(toExtract, simulate);
            remaining = remaining - tileExtracted;

            // then the item in inventory
            IEnergyStorage itemEnergyStorage = getItemEnergyHandler();

            if(itemEnergyStorage != null) {
                int itemExtracted = itemEnergyStorage.extractEnergy(remaining, simulate);
                remaining = remaining - itemExtracted;
            }
            if (!simulate && remaining < toExtract) {
                dirty = true;
            }
            return toExtract - remaining;
        }
        return 0;
    }

    @Override
    public int getEnergyStored() {
        int stored = 0;
        IEnergyStorage itemEnergyStorage = getItemEnergyHandler();
        if(itemEnergyStorage != null) {
            stored = itemEnergyStorage.getEnergyStored();
        }
        return combinedEnergy(stored, tileEnergy.getEnergyStored());
    }

    @Override
    public int getMaxEnergyStored() {
        int maxStored = 0;
        IEnergyStorage itemEnergyStorage = getItemEnergyHandler();
        if(itemEnergyStorage != null) {
            maxStored = itemEnergyStorage.getMaxEnergyStored();
        }
        return combinedEnergy(maxStored, tileEnergy.getMaxEnergyStored());
    }

    private static int combinedEnergy(int itemEnergy, int blockEnergy) {
        return (int) Math.min(Integer.MAX_VALUE, (long) itemEnergy + blockEnergy);
    }

    @Override
    public boolean canExtract() {
        IEnergyStorage itemEnergyStorage = getItemEnergyHandler();
        if(itemEnergyStorage != null && itemEnergyStorage.canExtract()) {
            return true;
        }
        return tileEnergy.canExtract();
    }

    @Override
    public boolean canReceive() {
        IEnergyStorage itemEnergyStorage = getItemEnergyHandler();
        if(itemEnergyStorage != null && itemEnergyStorage.canReceive()) {
            return true;
        }
        return tileEnergy.canReceive();
    }

    @Nullable
    IEnergyStorage getItemEnergyHandler() {
        return itemHandler.getStackInSlot(0).getCapability(Capabilities.EnergyStorage.ITEM);
    }
}
