package mpsremodularized.client.gui.common;

import mpsremodularized.client.gui.IContainerULOffSet;
import mpsremodularized.client.gui.clickable.ModularItemTabToggleWidget;
import mpsremodularized.client.gui.frame.InventoryFrame;
import mpsremodularized.client.gui.frame.ModularItemSelectionFrameContainered;
import mpsremodularized.client.gui.geometry.Rect;
import mpsremodularized.common.container.InstallSalvageMenu;
import mpsremodularized.common.network.MPSPackets;
import mpsremodularized.common.network.packets.serverbound.ContainerGuiOpenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.List;

public class ScrollableInventoryFrame2 <C extends InstallSalvageMenu> extends InventoryFrame implements IContainerULOffSet {
    ModularItemSelectionFrameContainered<InstallSalvageMenu> modularItemSelectionFrame;
    ModularItemTabToggleWidget selected;

    public ScrollableInventoryFrame2(C containerIn,
                                     Rect rect,
                                     @Nonnull ModularItemSelectionFrameContainered<InstallSalvageMenu> modularItemSelectionFrame,
                                     IContainerULOffSet.ulGetter ulgetter) {
        super(rect, containerIn, 9, containerIn.getModularInventorySlotIndexes(), ulgetter);
        this.modularItemSelectionFrame = modularItemSelectionFrame;
        modularItemSelectionFrame.getSelectedTab().ifPresent(tab-> selected = tab);
    }

    @Override
    public void update(double mouseX, double mouseY) {
        modularItemSelectionFrame.getSelectedTab().ifPresent(tab->{
            if (selected != tab) {
                selected = tab;
                EquipmentSlot type = tab.getSlotType();
                Minecraft mc = Minecraft.getInstance();

                if (mc.screen == null) {
                    return;
                }
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                long windowHandle = mc.getWindow().getWindow();
                GLFW.glfwGetCursorPos(windowHandle, xPos, yPos);
                MPSPackets.sendToServer(new ContainerGuiOpenPacket(type, true, xPos[0], yPos[0]));
            }
        });
        super.update(mouseX, mouseY);
    }

    @Override
    public List<Component> getToolTip(int i, int i1) {
        return super.getToolTip(i, i1);
    }
}
