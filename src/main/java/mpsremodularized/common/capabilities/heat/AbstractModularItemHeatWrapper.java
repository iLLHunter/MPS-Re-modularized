package mpsremodularized.common.capabilities.heat;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.TagUtils;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class AbstractModularItemHeatWrapper implements IHeatStorage {
    private final ItemStack itemStack;
    public final int tier;
    double heat = 0;
    double capacity = 0;

    public AbstractModularItemHeatWrapper(@Nonnull ItemStack itemStack, int tier) {
        this.itemStack = itemStack;
        this.tier = tier;
        this.heat = TagUtils.getHeat(itemStack);
        // since this is constantly being reloaded, this should be fine, I think
        this.capacity = getBaseMaxHeat(itemStack, tier) + getModuleMaxHeat();
    }

    @Override
    public double receiveHeat(double maxReceive, boolean simulate) {
        if (!Double.isFinite(maxReceive) || maxReceive <= 0 || !canReceive()) {
            return 0;
        }

        double heatReceived = Math.min(Math.max(0, getMaxHeatStored() - getHeatStored()), Math.min(getMaxHeatTransfer(), maxReceive));
        if (!simulate && heatReceived !=0) {
            heat += heatReceived;
            TagUtils.setHeat(itemStack, heat);
        }
        return heatReceived;
    }

    @Override
    public double extractHeat(double maxExtract, boolean simulate) {
        if (!Double.isFinite(maxExtract) || maxExtract <= 0 || !canExtract()) {
            return 0;
        }

        double heatExtracted = Math.min(heat, maxExtract);
        if (!simulate && heatExtracted != 0) {
            heat -= heatExtracted;
            TagUtils.setHeat(itemStack, heat);
        }
        return heatExtracted;
    }

    @Override
    public double getHeatStored() {
        return heat;
    }

    @Override
    public double getMaxHeatStored() {
        return capacity;
    }

    @Override
    public double getMaxHeatTransfer() {
        return getMaxHeatStored();
    }

    @Override
    public boolean canExtract() {
        return this.heat > 0 && getMaxHeatTransfer() > 0;
    }

    @Override
    public boolean canReceive() {
        return getMaxHeatTransfer() > 0;
    }

    public abstract double getBaseMaxHeat(ItemStack itemStack, int tier);
    public double getModuleMaxHeat() {
        IModularItem iModularItem = MPSCapabilities.getModularItemOrModeChangingCapability(this.itemStack);
        double moduleCapMaxHeat = 0;
        if (iModularItem != null) {
            for (int i=0; i < iModularItem.getSlots(); i++) {
                ItemStack module = iModularItem.getStackInSlot(i);
                IPowerModule pm = module.getCapability(MPSCapabilities.Module.POWER_MODULE);
                if(pm != null) {
                    moduleCapMaxHeat += pm.applyPropertyModifiers(MPSConstants.MAXIMUM_HEAT);
                }
            }
        }
        return moduleCapMaxHeat;
    }
}
