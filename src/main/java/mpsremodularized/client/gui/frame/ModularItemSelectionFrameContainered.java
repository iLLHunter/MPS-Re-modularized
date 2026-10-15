package mpsremodularized.client.gui.frame;

import mpsremodularized.client.gui.geometry.MusePoint2D;
import mpsremodularized.common.container.slot.IHideableSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class ModularItemSelectionFrameContainered<C extends AbstractContainerMenu> extends ModularItemSelectionFrame {
    C containerMenu;

    public ModularItemSelectionFrameContainered(C container, MusePoint2D ul, EquipmentSlot type) {
        super(ul, type);
        this.containerMenu = container;
    }

    @Override
    void disableAbstractContainerMenuSlots() {
//        MPSLogger.logDebug("containerMenu.slots: " + containerMenu.slots);
        for (Slot slot : containerMenu.slots) {
            if (slot instanceof IHideableSlot) {
                ((IHideableSlot) slot).disable();
                slot.x = -1000;
                slot.y = -1000;
            }
        }
    }
}
