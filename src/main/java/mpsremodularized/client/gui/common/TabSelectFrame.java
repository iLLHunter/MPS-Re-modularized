package mpsremodularized.client.gui.common;

import mpsremodularized.client.gui.clickable.button.VanillaButton;
import mpsremodularized.client.gui.frame.AbstractGuiFrame;
import mpsremodularized.client.gui.geometry.MusePoint2D;
import mpsremodularized.client.gui.geometry.Rect;
import mpsremodularized.client.sound.Musique;
import mpsremodularized.client.sound.MPSSoundDictionary;
import mpsremodularized.client.gui.cosmetic.CosmeticGui;
import mpsremodularized.client.gui.keymap.TinkerKeymapGui;
import mpsremodularized.client.gui.module.tweak.ModuleTweakGui;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.network.MPSPackets;
import mpsremodularized.common.network.packets.serverbound.ContainerGuiOpenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MachineMuse
 * <p>
 */
public class TabSelectFrame extends AbstractGuiFrame {
    public static final int HEIGHT = 20;
    Player player;
    List<VanillaButton> buttons = new ArrayList<>();

    public TabSelectFrame(double left, double bottom, double width, Player player, int active) {
        super(new Rect(left, bottom - HEIGHT, left + width, bottom));
        this.player = player;
        VanillaButton button;

        /** Install Salvage GUI (the only AbstractContainerMenued GUI)*/
        button = new VanillaButton(left(), top(), Component.translatable("gui." + MPSConstants.MOD_ID + ".tab.install.salvage"), active != 0);
        button.setOnPressed(onPressed->{
            Musique.playClientSound(MPSSoundDictionary.SOUND_EVENT_GUI_SELECT.get(), 1);
            MPSPackets.sendToServer(new ContainerGuiOpenPacket(EquipmentSlot.MAINHAND, false, -1, -1));
        });
        buttons.add(button);

        /** Module Tweak Gui */
        button = new VanillaButton(left(), top(), Component.translatable("gui." + MPSConstants.MOD_ID + ".tab.module.tweak"), active != 1);
        button.setOnPressed(onPressed->{
            Musique.playClientSound(MPSSoundDictionary.SOUND_EVENT_GUI_SELECT.get(), 1);
            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new ModuleTweakGui(Component.translatable("gui.tinkertable"))));
        });
        buttons.add(button);

        /** Keybind Gui */
        button = new VanillaButton(left(), top(), Component.translatable("gui." + MPSConstants.MOD_ID + ".tab.keybinds"), active !=2);
        button.setOnPressed(onPressed->{
            Musique.playClientSound(MPSSoundDictionary.SOUND_EVENT_GUI_SELECT.get(), 1);
            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new TinkerKeymapGui(player, Component.translatable("gui." + MPSConstants.MOD_ID + ".tab.keybinds.toggle"))));
        });
        buttons.add(button);

        /** Cosmetic Tweak Frame */
        button = new VanillaButton(left(), top(), Component.translatable("gui." + MPSConstants.MOD_ID + ".tab.visual"), active !=3);
        button.setOnPressed(onPressed->{
            Musique.playClientSound(MPSSoundDictionary.SOUND_EVENT_GUI_SELECT.get(), 1);
            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new CosmeticGui(player.getInventory(), Component.translatable("gui.tinkertable"))));
        });
        buttons.add(button);

        for(VanillaButton b : buttons) {
            b.setVisible(true);
        }

        double totalButtonWidth = 0;
        for (VanillaButton button1 : buttons) {
            totalButtonWidth += button1.width();
        }
        // totalButtonWidth greater than width will produce a negative spacing value
        double spacing = (this.width() - totalButtonWidth) / (buttons.size() +1);

        double x = spacing; // first entry may be negative and will allow an oversized tab frame to be centered
        for (VanillaButton button1 : buttons) {
            button1.setPosition(new MusePoint2D(this.left() + x + button1.width() * 0.5, centerY()));
            int index = buttons.indexOf(button1);
            x += Math.abs(spacing) + button1.width();
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return buttons.stream().anyMatch(b ->b.mouseClicked(x, y, button));
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return buttons.stream().anyMatch(b ->b.mouseReleased(x, y, button));
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        buttons.stream().forEach(b->b.render(gfx, mouseX, mouseY, partialTick));
    }


    public static void switchScreenPreservingMouse(Screen newScreen) {
        Minecraft mc = Minecraft.getInstance();

        // 1. Fall back to standard logic if there's no current screen to inherit from
        if (mc.screen == null) {
            mc.setScreen(newScreen);
            return;
        }

        // 2. Fetch the raw GLFW sub-pixel cursor position relative to the game window
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        long windowHandle = mc.getWindow().getWindow();
        GLFW.glfwGetCursorPos(windowHandle, xPos, yPos);

        // 3. Update the game state with the new screen
        mc.setScreen(newScreen);

        // 4. Force GLFW to restore the exact window-relative mouse position
        GLFW.glfwSetCursorPos(windowHandle, xPos[0], yPos[0]);
    }
}
