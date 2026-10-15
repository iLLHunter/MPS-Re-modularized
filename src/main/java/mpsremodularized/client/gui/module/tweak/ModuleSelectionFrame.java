package mpsremodularized.client.gui.module.tweak;

import mpsremodularized.client.gui.clickable.ClickableModule;
import mpsremodularized.client.gui.clickable.slider.VanillaFrameScrollBar;
import mpsremodularized.client.gui.frame.ModularItemSelectionFrame;
import mpsremodularized.client.gui.frame.ScrollableFrame;
import mpsremodularized.client.gui.geometry.MusePoint2D;
import mpsremodularized.client.gui.geometry.Rect;
import mpsremodularized.client.gui.geometry.SwirlyMuseCircle;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.utils.IconUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModuleSelectionFrame extends ScrollableFrame {
    protected ModularItemSelectionFrame target;
    protected Map<ModuleCategory, ModuleSelectionSubFrame> categories = new LinkedHashMap<>();
    protected Rect lastPosition;
    ClickableModule selectedModule = null;
    VanillaFrameScrollBar scrollBar;
    SwirlyMuseCircle circle;

    public ModuleSelectionFrame(ModularItemSelectionFrame itemSelectFrameIn, Rect rect) {
        super(rect);
        this.target = itemSelectFrameIn;
        this.scrollBar = new VanillaFrameScrollBar(this, "scrollbar");
        this.scrollBar.setValue(0);
        this.currentScrollPixels = 0;
        loadModules(false);
        circle = new SwirlyMuseCircle();
    }

    protected ModuleSelectionSubFrame getOrCreateCategory(ModuleCategory category) {
        if (categories.containsKey(category)) {
            return categories.get(category);
        } else {
            Rect position = new Rect(
                    left() + 4,
                    top() + 4,
                    right() - 4,
                    top() + 36);
            position.setBelow(lastPosition);
            lastPosition = position;
            ModuleSelectionSubFrame frame = new ModuleSelectionSubFrame(category, position);

            categories.put(category, frame);
            frame.setDoOnNewSelect(thing-> {
                this.selectedModule = thing.getSelectedModule();
                this.onSelected();
            });
            return frame;
        }
    }

    /**
     * This is required for the new ItemStack data setup in order for changes to propagate through the GUI code
     */
    public void refreshModule() {
        if(selectedModule != null) {
            IModularItem iModularItem = target.getModularItemCapability();
            if(iModularItem != null) {
                if(selectedModule.getInventorySlot() >= 0) {
                    final int selected = selectedModule.getInventorySlot();
                    // -1 indicates module is not installed, just a precaution
                    if (selected >= 0 && selected < iModularItem.getSlots()) {
                        ItemStack otherModule = iModularItem.getStackInSlot(selected);
                        if (selectedModule.getModule().is(otherModule.getItem())) {
                            selectedModule.setModule(otherModule);
                        } else {
                            loadModules(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Populates the module list
     * load this whenever a modular item is selected or when a module is installed
     */
    public void loadModules(boolean preserveSelected) {
        if (!preserveSelected) {
            setCurrentScrollPixels(0);
        }

        this.lastPosition = null;
        ClickableModule previous = preserveSelected ? getSelectedModule() : null;
        categories.clear();

        IModularItem iModularItem = target.getModularItemCapability();
        if(iModularItem != null) {
            // Occupied slots in the Modular Item
            for (int index = 0; index < iModularItem.getSlots(); index++) {
                ItemStack module = iModularItem.getStackInSlot(index);
                if (!module.isEmpty()) {
                    int finalIndex = index;
                    IPowerModule m = iModularItem.getModuleCapability(module);
                    if(m != null) {
                        if (m.isAllowed()) {
                            getOrCreateCategory(m.getCategory()).addModule(module, finalIndex).setInstalled(true);
                        }
                    }
                }
            }
        }

        finishLoadingModules(previous);
    }

    protected void finishLoadingModules(@Nullable ClickableModule previous) {
        selectedModule = null;
        for (ModuleSelectionSubFrame frame : categories.values()) {
            frame.refreshButtonPositions();
            frame.resetSelection();
            if (previous != null) {
                for (ClickableModule button : frame.moduleButtons) {
                    if (ItemStack.isSameItem(button.getModule(), previous.getModule())) {
                        frame.selectedModule = frame.moduleButtons.indexOf(button);
                        frame.oldSelected = frame.selectedModule;
                        selectedModule = button;
                        break;
                    }
                }
            }
        }
        setTotalSize(8 + categories.values().stream().mapToInt(frame -> (int) frame.border.height()).sum());
        setCurrentScrollPixels(currentScrollPixels);
        scrollBar.setMaxValue(getMaxScrollPixels());
        scrollBar.setValue(currentScrollPixels);
        onSelected();
    }

    /** Keep the selected live button and page when the screen is resized. */
    public void restoreSelection(@Nullable ModuleSelectionFrame previous) {
        if (previous != null) {
            finishLoadingModules(previous.getSelectedModule());
            setCurrentScrollPixels(previous.getCurrentScrollPixels());
            scrollBar.setValue(currentScrollPixels);
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);
        this.scrollBar.render(gfx, mouseX, mouseY, partialTick);

        IModularItem iModularItem = target.getModularItemCapability();

        categories.values().forEach(ModuleSelectionSubFrame::refreshButtonPositions);

        if (iModularItem != null) {
            int totalHeight=8;

            for (ModuleSelectionSubFrame frame : categories.values()) {
                //                totalSize = (int) Math.max(frame.border.bottom() - this.top(), totalSize);
                totalHeight += (int)frame.border.height();
            }
            setTotalSize(totalHeight);

            super.preRender(gfx, mouseX, mouseY, partialTick);
            gfx.pose().pushPose();
            gfx.pose().translate(0, (float)-currentScrollPixels, 0);
            drawItems(gfx, partialTick);
            drawSelection(gfx);
            gfx.pose().popPose();
            super.postRender(gfx, mouseX, mouseY, partialTick);
        } else {
            super.render(gfx, mouseX, mouseY, partialTick);
        }
    }

    private void drawItems(GuiGraphics gfx, float partialTicks) {
        for (ModuleSelectionSubFrame frame : categories.values()) {
            frame.drawPartial(gfx, (int) (this.currentScrollPixels + top() + 4),
                    (int) (this.currentScrollPixels + top() + height() - 4), partialTicks);
        }
    }

    public void drawSelection(GuiGraphics gfx) {
        if (selectedModule != null) {
            MusePoint2D pos = selectedModule.center();
            if (pos.y() > this.currentScrollPixels + top() + 4 && pos.y() < this.currentScrollPixels + top() + height() - 4) {
                IconUtils.drawCircleAround(circle, gfx.pose(), pos.x(), pos.y(), 10, 100);
            }
        }
    }

    @Nullable
    public ClickableModule getSelectedModule() {
        return selectedModule;
    }

    @Nullable
    public IPowerModule getModuleCap() {
        if (selectedModule != null) {
            IModularItem iModularItem = target.getModularItemCapability();
            if(iModularItem != null) {
                int slot = selectedModule.getInventorySlot();
                if (slot >= 0 && slot < iModularItem.getSlots()) {
                    ItemStack module = iModularItem.getStackInSlot(slot);
                    if (ItemStack.isSameItem(module, selectedModule.getModule())) {
                        selectedModule.setModule(module);
                        return iModularItem.getModuleCapability(module);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dWheel) {
        boolean retVal = super.mouseScrolled(mouseX, mouseY, dWheel);
        scrollBar.setValue(currentScrollPixels);
        return retVal;
    }

    @Override
    public void update(double mouseX, double mouseY) {
        refreshModule();
        scrollBar.setMaxValue(getMaxScrollPixels());
        scrollBar.setValueByMouse(mouseY);
        setCurrentScrollPixels(scrollBar.getValue());
        super.update(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (scrollBar.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(scrollBar.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (super.mouseClicked(mouseX, mouseY, button)) {
            ModuleSelectionSubFrame sel = null;
            mouseY += currentScrollPixels;
            for (ModuleSelectionSubFrame frame : categories.values()) {
                if (frame.mouseClicked(mouseX, mouseY, button)) {
                    sel = frame;
                }
            }

            if (sel != null && sel.getSelectedModule() != null) {
                for (ModuleSelectionSubFrame frame : categories.values()) {
                    if (frame != sel) {
                        frame.resetSelection();
                    }
                    else {
                        final ClickableModule selectedOther = sel.getSelectedModule();
                        if(selectedModule!= selectedOther) {
                            selectedModule = selectedOther;
                            onSelected();
                        }
                    }
                }
            }

            return sel != null;
        }
        return false;
    }

    @Override
    public List<Component> getToolTip(int x, int y) {
        if (containsPoint(x, y)) {
            y += (int)currentScrollPixels;
            if (!categories.isEmpty()) {
                for (ModuleSelectionSubFrame category : categories.values()) {
                    List<Component> tooltip = category.getToolTip(x, y);
                    if(tooltip != null) {
                        return tooltip;
                    }
                }
            }
        }
        return null;
    }

    /** Retain the item context for tooltip renderers supplied by other mods. */
    public ItemStack getHoveredModule(int x, int y) {
        if (containsPoint(x, y)) {
            y += (int) currentScrollPixels;
            for (ModuleSelectionSubFrame category : categories.values()) {
                if (category.border.containsPoint(x, y)) {
                    for (ClickableModule module : category.moduleButtons) {
                        if (module.containsPoint(x, y)) return module.getModule();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Sets code to be executed when a new item is selected
     */
    OnSelectNewModule doThis;
    public void setDoOnNewSelect(OnSelectNewModule doThisIn) {
        doThis = doThisIn;
    }

    /**
     * runs preset code when new module is selected
     */
    void onSelected() {
        if(this.doThis != null) {
//            MPSLogger.logDebug("onSelected running");
            this.doThis.onSelected(this);
        }
    }

    public interface OnSelectNewModule {
        void onSelected(ModuleSelectionFrame doThis);
    }
}
