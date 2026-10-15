package mpsremodularized.client.gui.cosmetic.colorpicker;

import mpsremodularized.client.gui.clickable.RadioButton;
import mpsremodularized.client.gui.geometry.MusePoint2D;
import mpsremodularized.common.math.Color;
import mpsremodularized.common.utils.IconUtils;
import net.minecraft.client.gui.GuiGraphics;

public class MinusButton extends RadioButton {
    public MinusButton(double leftPos, double topPos) {
        super(IconUtils.getIcon().minusSign, 8, 4, leftPos, topPos);
    }

    public MinusButton(MusePoint2D ul) {
        super(IconUtils.getIcon().minusSign, new MusePoint2D(8, 4), ul);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        getIcon().draw(gfx.pose(), this.left(), this.top() - 4, Color.RED); // offset
    }
}
