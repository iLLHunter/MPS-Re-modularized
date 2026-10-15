package mpsremodularized.client.gui.module.install_salvage;

import mpsremodularized.client.gui.clickable.ClickableModule;
import mpsremodularized.client.gui.frame.ModularItemSelectionFrame;
import mpsremodularized.client.gui.geometry.Rect;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.client.gui.module.tweak.ModuleSelectionFrame;
import mpsremodularized.client.gui.module.tweak.ModuleSelectionSubFrame;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;


public class CompatibleModuleDisplayFrame extends ModuleSelectionFrame {
    NonNullList<ItemStack> possibleItems;

    public CompatibleModuleDisplayFrame(ModularItemSelectionFrame itemSelectFrameIn, Rect rect) {
        super(itemSelectFrameIn, rect);
    }

    public NonNullList<ItemStack> getPossibleItems() {
        return possibleItems;
    }

    @Override
    public void update(double mouseX, double mouseY) {
        super.update(mouseX, mouseY);
        IModularItem iModularItem = target.getModularItemCapability();
        if(iModularItem != null) {
            for (ModuleSelectionSubFrame subframe : categories.values()) {
                for (ClickableModule module : subframe.moduleButtons) {
                    module.setInstalled(iModularItem.isModuleInstalled(module.getRegName()));
                }
            }
        }
    }

    /**
     * Populates the module list
     * load this whenever a modular item is selected or when a module is installed
     */
    @Override
    public void loadModules(boolean preserveSelected) {
        this.lastPosition = null;
        if (!preserveSelected) setCurrentScrollPixels(0);
        possibleItems = NonNullList.create();
        ClickableModule previous = preserveSelected ? getSelectedModule() : null;
        categories.clear();

        IModularItem iModularItem = target.getModularItemCapability();
        if(iModularItem != null) {
            possibleItems = iModularItem.getCompatibleModules();
            for (ItemStack module : possibleItems) {
                IPowerModule m = iModularItem.getModuleCapability(module);
                if (m != null) {
                    ClickableModule clickie = getOrCreateCategory(m.getCategory()).addModule(module, -1);
                    clickie.setInstalled(iModularItem.isModuleInstalled(module));
                }
            }
        }
        finishLoadingModules(previous);
    }
}
