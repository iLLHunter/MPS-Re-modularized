package mpsremodularized.client.gui.cosmetic.colorpicker;

import mpsremodularized.client.gui.clickable.RadioButton;
import mpsremodularized.client.gui.geometry.MusePoint2D;
import mpsremodularized.common.math.Color;
import mpsremodularized.common.utils.IconUtils;
import net.minecraft.client.gui.GuiGraphics;

public class PlusButton extends RadioButton {
    public PlusButton(double leftPos, double topPos) {
        super(IconUtils.INSTANCE.getIcon().plusSign, 8, 8, leftPos, topPos);
    }

    public PlusButton(MusePoint2D ul) {
        super(IconUtils.INSTANCE.getIcon().plusSign, new MusePoint2D(8,8), ul);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        getIcon().draw(gfx.pose(), this.left(), this.top(), Color.GREEN);
    }
}
