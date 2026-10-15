package mpsremodularized.common.capabilities.inventory.modularitem;

import com.mojang.datafixers.util.Pair;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.tickable.IPlayerTickModule;
import mpsremodularized.common.capabilities.module.toggleable.IToggleableModule;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.common.utils.TagUtils;
import mpsremodularized.imixin.common.item.IMixinRangedWrapper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ModularItem extends ComponentItemHandler implements IModularItem {
    @Override
    protected void onContentsChanged(int slot, ItemStack oldStack, ItemStack newStack) {
        super.onContentsChanged(slot, oldStack, newStack);
        if (oldStack.isEmpty() || oldStack.is(newStack.getItem()) || !TagUtils.getModuleBoolean(oldStack, "added")) return;
        var key = oldStack.is(mpsremodularized.common.registration.MPSItems.SILK_TOUCH_MODULE.get()) ? net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH
            : oldStack.is(mpsremodularized.common.registration.MPSItems.FORTUNE_MODULE.get()) ? net.minecraft.world.item.enchantment.Enchantments.FORTUNE
            : oldStack.is(mpsremodularized.common.registration.MPSItems.AQUA_AFFINITY_MODULE.get()) ? net.minecraft.world.item.enchantment.Enchantments.AQUA_AFFINITY : null;
        if (key == null) return;
        var existing = getModularItemStack().getOrDefault(DataComponents.ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        var mutable = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(existing);
        for (var holder : existing.keySet()) if (holder.is(key)) mutable.set(holder, 0);
        getModularItemStack().set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }
    final boolean isTool;
    final int tier;
    Map<ModuleCategory, RangedWrapper> rangedWrappers;

    public ModularItem(ItemStack modularItem, int tier, int size) {
        this(modularItem, tier, size, false);
    }

    public ModularItem(ItemStack modularItem, int tier, int size, boolean isTool) {
        super(modularItem, DataComponents.CONTAINER, size);
        this.rangedWrappers = new HashMap<>();
        this.isTool = isTool;
        this.tier = tier;
    }

    @Override
    public ItemStack getModularItemStack() {
        return (ItemStack) parent;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public Map<ModuleCategory, RangedWrapper> getRangedWrappers() {
        return rangedWrappers;
    }

    @Override
    public void setRangedWrapperMap(Map<ModuleCategory, RangedWrapper> rangedWrappersIn) {
        this.rangedWrappers = rangedWrappersIn;
    }

    @Override
    public boolean isModuleValid (IPowerModule pm) {
        if (pm != null && pm.getModule().getItem() instanceof mpsremodularized.common.compat.NativeModuleItem nativeItem
                && (nativeItem.source(pm.getModule()).isEmpty() || !nativeItem.acceptsSource(nativeItem.source(pm.getModule())))) return false;
        return isModuleCompatible(pm);
    }

    @Override
    public boolean isModuleCompatible(@Nullable IPowerModule pm) {
        if(pm != null && pm.isAllowed() && pm.getTier() <= getTier()) {
            // check module target against item stack type
            switch (pm.getTarget()) {
                case ALLITEMS-> {
                    return true;
                }
                case HANDHELD -> {
                    return isHandHeld();
                }
                case TOOLONLY-> {
                    return isTool();
                }
                case ARMORONLY-> {
                    return getModularItemStack().getItem() instanceof ArmorItem;
                }
                case HEADONLY-> {
                    return getModularItemStack().getItem() instanceof ArmorItem
                        && ItemUtils.getEquipmentSlotForItem(getModularItemStack()) == EquipmentSlot.HEAD;
                }
                case TORSOONLY-> {
                    return getModularItemStack().getItem() instanceof ArmorItem
                        && ItemUtils.getEquipmentSlotForItem(getModularItemStack()) == EquipmentSlot.CHEST;
                }
                case LEGSONLY-> {
                    return getModularItemStack().getItem() instanceof ArmorItem
                        && ItemUtils.getEquipmentSlotForItem(getModularItemStack()) == EquipmentSlot.LEGS;
                }
                case FEETONLY-> {
                    return getModularItemStack().getItem() instanceof ArmorItem
                        && ItemUtils.getEquipmentSlotForItem(getModularItemStack()) == EquipmentSlot.FEET;
                }
                default-> {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isModuleValid(ItemStack module) {
        // empty item is valid
        if (module.isEmpty()) {
            return false;
        }

        IPowerModule cap = getModuleCapability(module);
        if (cap == null) {
            return false;
        }

        return isModuleValid(cap);
    }

    /**
     * Returns a range of slots (partition) for a given module category, or null if there is none
     * @param category
     * @return
     */
    @Nullable
    @Override
    public Pair<Integer, Integer> getRangeForCategory(ModuleCategory category) {
        RangedWrapper rangedWrapper = getRangedWrappers().get(category);
        if (rangedWrapper != null){
            return ((IMixinRangedWrapper)rangedWrapper).mpsremodularized$getRange();
        }
        return null;
    }

    @Override
    public int getStackLimit(final int slot, final ItemStack module) {
        // allow empty slots, else empty slots will crash on opening container
        if (module.isEmpty()) {
            return 1;
        }

//        MPSLogger.logDebug("checking stack limit 1");

        if (isModuleValid(module)) {
            if (module.getItem() instanceof mpsremodularized.common.compat.NativeModuleItem nativeArmor
                    && mpsremodularized.common.compat.NativeModuleItem.armorSlot(nativeArmor.kind) != null) {
                // Approved: one originating-mod armor variant per host piece, even when offline.
                for (int other = 0; other < getSlots(); other++) {
                    if (other != slot && getStackInSlot(other).getItem() instanceof mpsremodularized.common.compat.NativeModuleItem installed
                            && installed.provider == nativeArmor.provider && installed.kind == nativeArmor.kind) return 0;
                }
            }
            //  Check if the module is already in this slot instead of just blindly
            // checking if module is already installed somewhere. This is because this method will be called
            // on loading to check if the module is valid for the given slot.
            int isInstalled = findInstalledModule(module);
            if (isInstalled != -1 && isInstalled != slot) {
//                MPSLogger.logDebug("module already installed in slot: " + isInstalled);
                return 0;
            }

            // get the module category... CATEGORY_NONE) is actually just a fallback
            IPowerModule pm = getModuleCapability(module);
            ModuleCategory category = pm == null ? ModuleCategory.NONE : pm.getCategory();

            // Specfic module type for limited modules
            RangedWrapper wrapper = getRangedWrappers().get(category);
//            MPSLogger.logDebug("wrapper: " + wrapper);
            // fallback on generic type if null
            if (wrapper == null) {
                wrapper = getRangedWrappers().get(ModuleCategory.NONE);
            }

            // Note, not using the actual wrapper mechanics, just slot ranges
            if (wrapper != null && ((IMixinRangedWrapper)wrapper).mpsremodularized$contains(slot)) {
                if (getStackInSlot(slot).is(module.getItem())) {
                    return 1;
                }
                return getStackInSlot(slot).isEmpty() ? 1: 0;
            }
//            MPSLogger.logDebug("checking stack limit 2");
        }
        return 0;
    }

    /**
     * An alternative to isItemValid since the requirements are different WILL cause issues
     * @param slot
     * @param module
     * @return
     */
    @Override
    public boolean isModuleValidForPlacement(int slot, ItemStack module) {
        if (!isModuleValid(module)) {
            IPowerModule cap = getModuleCapability(module);
            if (cap == null) {
//                MPSLogger.logDebug("no capability for module");
                return false;
            }
        }
//        MPSLogger.logDebug("==================================================================");
//        MPSLogger.logDebug("isValidForPlacement: <slot, module: <" + slot +", " +module +">"  );
//        MPSLogger.logDebug("isSlotEmpty: " + getStackInSlot(slot).isEmpty());
//        MPSLogger.logDebug("slotLimit: " + getStackLimit(slot, module));
//        MPSLogger.logDebug("isItemValid: " + isItemValid(slot, module));
//        MPSLogger.logDebug("------------------------------------------------------------------");
        return getStackInSlot(slot).isEmpty() && getStackLimit(slot, module) != 0 && isItemValid(slot, module);
    }

    @Override
    public int findInstalledModule(ItemStack module) {
        return findInstalledModule(ItemUtils.getRegistryName(module));
    }

    @Override
    public boolean isModuleInstalled(ResourceLocation regName) {
        return findInstalledModule(regName) > -1;
    }

    @Override
    public boolean isModuleInstalled(ItemStack module) {
        return findInstalledModule(module) > -1;
    }

    @Override
    public boolean isModuleInstalled(Item item) {
        return isModuleInstalled(ItemUtils.getRegistryName(item));
    }

    @Override
    public boolean isModuleOnline(ResourceLocation moduleName) {
        int slot = findInstalledModule(moduleName);
        if (slot > -1) {
            return isModuleOnline(getStackInSlot(slot));
        }
        return false;
    }

    @Override
    public boolean isModuleOnline(ItemStack module) {
        IPowerModule pm = getModuleCapability(module);
        return isModuleOperational(pm);
    }

    @Override
    public ItemStack getOnlineModuleOrEmpty(ResourceLocation moduleName) {
        int i = findInstalledModule(moduleName);
        if (i > -1) {
            ItemStack module = getStackInSlot(i);
            if(isModuleOnline(module)) {
                return module;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void toggleModule(boolean online, int moduleIndex) {
        if (moduleIndex > -1) {
            ItemStack module = getStackInSlot(moduleIndex);
            IPowerModule pm = getModuleCapability(module);
            if (pm instanceof IToggleableModule tm) {
                ItemStack newModule = tm.toggleModule(online);
                updateModuleInSlot(moduleIndex, newModule);
            }
        }
    }

    @Override
    public void toggleModule(ResourceLocation moduleName, boolean online) {
        int slot = findInstalledModule(moduleName);
        toggleModule(online, slot);
    }

    @Override
    public void setModuleBoolean(ResourceLocation moduleName, String key, boolean value) {
        int slot = findInstalledModule(moduleName);
        if (slot > -1) {
            ItemStack module = getStackInSlot(slot);
            IPowerModule pm = getModuleCapability(module);
            if (pm != null) {
                ItemStack newModule = TagUtils.setModuleBoolean(module, key, value);
                updateModuleInSlot(slot, newModule);
            }
        }
    }

    @Override
    public boolean setModuleDouble(int index, String key, double value) {
        if(index > -1 && index < getSlots()) {
            ItemStack module = getStackInSlot(index);
            IPowerModule pm = getModuleCapability(module);
            if (pm != null) {
                double testValue = TagUtils.getModuleDouble(module, key);
                if(value != testValue) {
                    ItemStack newModule = TagUtils.setModuleDouble(module, key, value);
                    updateModuleInSlot(index, newModule);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * TODO: WIP!! Still assessing what changes actually need to be made for things to work
     *
     * @param moduleName
     * @param key
     * @param value
     * @return
     */
    @Override
    public boolean setModuleDouble(ResourceLocation moduleName, String key, double value) {
        return setModuleDouble(findInstalledModule(moduleName), key, value);
    }

    @Override
    public double getModuleDouble(int index, String key) {
        ItemStack module = getStackInSlot(index);
        IPowerModule pm = getModuleCapability(module);
        if (pm != null) {
            return TagUtils.getModuleDouble(module, key);
        }
        return 0;
    }

    @Override
    public double getModuleDouble(ResourceLocation moduleName, String key) {
        return getModuleDouble(findInstalledModule(moduleName), key);
    }

    @Override
    public void setModuleFloat(ResourceLocation moduleName, String key, float value) {
        int i = findInstalledModule(moduleName);
        if(i > -1) {
            ItemStack module = getStackInSlot(i);
            IPowerModule pm = getModuleCapability(module);
            if (pm != null) {
                ItemStack newModule = TagUtils.setModuleFloat(module, key, value);
                updateModuleInSlot(i, newModule);
            }
        }
    }

    @Override
    public void setModuleBlockState(ResourceLocation moduleName, BlockState state) {
        int i = findInstalledModule(moduleName);
        if(i > -1) {
            ItemStack module = getStackInSlot(i);
            IPowerModule pm = getModuleCapability(module);
            if (pm != null) {
                ItemStack newModule = TagUtils.setModuleBlockState(module,state);
                updateModuleInSlot(i, newModule);
            }
        }
    }

    @Override
    public void setModuleString(ResourceLocation moduleName, String key, String value) {
        int i = findInstalledModule(moduleName);
        if(i > -1) {
            ItemStack module = getStackInSlot(i);
            IPowerModule pm = getModuleCapability(module);
            if (pm != null) {
                ItemStack newModule = TagUtils.setModuleString(module, key, value);
                updateModuleInSlot(i, newModule);
            }
        }
    }

    @Override
    public int findInstalledModule(ResourceLocation registryName) {
        for (int i = 0; i < getSlots(); i++) {
            ItemStack testStack = getStackInSlot(i);
            if (!testStack.isEmpty()) {
                ResourceLocation registryNameOther = ItemUtils.getRegistryName(testStack);
                if (registryNameOther.getNamespace().equals(registryName.getNamespace())
                    && registryNameOther.getPath().equals(registryName.getPath())) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void tick(Player player, Level level, ItemStack host) {
        for (int i = 0; i < getSlots(); i++) {
            IPowerModule pm = getModuleCapability(getStackInSlot(i));
            if(pm instanceof IPlayerTickModule ticker) {
                if(isModuleOperational(ticker)) {
                    if(ticker.onPlayerTickActive(player, level, this.getModularItemStack(), i)) {
                        updateModuleInSlot(i, ticker.getModule());
                    }
                } else {
                    if(ticker.onPlayerTickInactive(player, level, this.getModularItemStack(), i)) {
                        updateModuleInSlot(i, ticker.getModule());
                    }
                }
            }
        }
    }

    // call server side only
    @Override
    public void updateModuleInSlot(int slot, ItemStack module) {
        this.updateContents(getContents(), module, slot);
    }

    @Override
    public double getMovementResistance() {
        double movementResistance = 0;
        for(int i = 0; i < getSlots(); i++) {
            IPowerModule pm = getModuleCapability(getStackInSlot(i));
            if(isModuleOperational(pm)) {
                movementResistance += pm.applyPropertyModifiers(MPSConstants.MOVEMENT_RESISTANCE);
            }
        }
        return movementResistance;
    }
}
