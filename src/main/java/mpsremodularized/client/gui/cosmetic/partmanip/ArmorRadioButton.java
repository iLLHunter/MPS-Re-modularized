package mpsremodularized.client.gui.cosmetic.partmanip;

import mpsremodularized.client.gui.clickable.RadioButton;
import mpsremodularized.client.gui.geometry.MusePoint2D;
import mpsremodularized.common.math.Color;
import mpsremodularized.common.utils.IconUtils;

public class ArmorRadioButton extends RadioButton {
    public ArmorRadioButton(double leftPos, double topPos, Color color) {
        super(IconUtils.getIcon().normalArmor, 8, 8, leftPos, topPos);
    }

    public ArmorRadioButton(MusePoint2D pos, Color color) {
        super(IconUtils.getIcon().normalArmor, new MusePoint2D(8,8), pos);
    }
}
