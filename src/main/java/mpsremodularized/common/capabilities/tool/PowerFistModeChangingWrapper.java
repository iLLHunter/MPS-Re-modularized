package mpsremodularized.common.capabilities.tool;

import mpsremodularized.common.capabilities.inventory.modechanging.ModeChangingModularItem;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class PowerFistModeChangingWrapper extends ModeChangingModularItem {
    private final int paxelSlot;
    private final int constructionStickSlot;
    private static int paxelSlot(ItemStack stack, int baseSize) {
        int saved = stack.getOrDefault(mpsremodularized.common.registration.MPSDataComponents.PAXEL_SLOT, -1);
        if (saved >= 5 && saved < 255) return saved;
        return mpsremodularized.common.compat.IntermodModules.paxelProviderPresent() ? Math.max(baseSize, 5) : -1;
    }
    private static int inventorySize(ItemStack stack, int size) {
        int slot = paxelSlot(stack, size);
        int stick = constructionStickSlot(stack, size);
        return Math.max(size + (slot >= 0 ? 1 : 0) + (stick >= 0 ? 1 : 0),
                Math.max(Math.max(slot + 1, stick + 1), stack.getOrDefault(net.minecraft.core.component.DataComponents.CONTAINER,
                        net.minecraft.world.item.component.ItemContainerContents.EMPTY).getSlots()));
    }
    private static int constructionStickSlot(ItemStack stack, int size) {
        int saved = stack.getOrDefault(mpsremodularized.common.registration.MPSDataComponents.CONSTRUCTION_STICK_SLOT, -1);
        if (saved >= 5 && saved < 255) return saved;
        if (!net.neoforged.fml.ModList.get().isLoaded("constructionstick")) return -1;
        // Append without repurposing an occupied general slot or a saved Paxel slot.
        return Math.max(Math.max(size, paxelSlot(stack, size) + 1), stack.getOrDefault(net.minecraft.core.component.DataComponents.CONTAINER,
                net.minecraft.world.item.component.ItemContainerContents.EMPTY).getSlots());
    }
    public PowerFistModeChangingWrapper(@Nonnull ItemStack modularItem, int tier, int size) {
        super(modularItem, tier, Math.max(inventorySize(modularItem, size), 5));
        paxelSlot = paxelSlot(modularItem, size);
        constructionStickSlot = constructionStickSlot(modularItem, size);
        Map<ModuleCategory, RangedWrapper> rangedWrapperMap = new HashMap<>();
        if (size > 0) {
            rangedWrapperMap.put(ModuleCategory.ENERGY_STORAGE, new RangedWrapper(this, 0, 1));
            if (size > 1) {
                rangedWrapperMap.put(ModuleCategory.PICKAXE, new RangedWrapper(this, 1, 2));
            }
            if (size > 2) {
                rangedWrapperMap.put(ModuleCategory.SHOVEL, new RangedWrapper(this, 2, 3));
            }
            if (size > 3) {
                rangedWrapperMap.put(ModuleCategory.AXE, new RangedWrapper(this, 3, 4));
            }
            if (size > 4) {
                rangedWrapperMap.put(ModuleCategory.HOE, new RangedWrapper(this, 4, 5));
            }
            if (size > 5) {
                rangedWrapperMap.put(ModuleCategory.NONE, new RangedWrapper(this, 5, this.getSlots()));
            }
        }
        if (paxelSlot >= 0) rangedWrapperMap.put(ModuleCategory.PAXEL, new RangedWrapper(this, paxelSlot, paxelSlot + 1));
        if (constructionStickSlot >= 0) rangedWrapperMap.put(ModuleCategory.CONSTRUCTION_STICK,
                new RangedWrapper(this, constructionStickSlot, constructionStickSlot + 1));
        this.setRangedWrapperMap(rangedWrapperMap);
    }

    @Override public int getStackLimit(int slot, ItemStack module) {
        if (!module.isEmpty()) {
            var capability = getModuleCapability(module);
            if (capability != null && ((slot == paxelSlot) != (capability.getCategory() == ModuleCategory.PAXEL))) return 0;
            if (capability != null && ((slot == constructionStickSlot) != (capability.getCategory() == ModuleCategory.CONSTRUCTION_STICK))) return 0;
        }
        return super.getStackLimit(slot, module);
    }
    @Override protected void onContentsChanged(int slot, ItemStack oldStack, ItemStack newStack) {
        super.onContentsChanged(slot, oldStack, newStack);
        if (paxelSlot >= 0) getModularItemStack().set(mpsremodularized.common.registration.MPSDataComponents.PAXEL_SLOT, paxelSlot);
        if (constructionStickSlot >= 0) getModularItemStack().set(mpsremodularized.common.registration.MPSDataComponents.CONSTRUCTION_STICK_SLOT, constructionStickSlot);
    }

    @Override
    public boolean isTool() {
        return true;
    }
}
