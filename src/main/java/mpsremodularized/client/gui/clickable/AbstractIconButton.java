package mpsremodularized.client.gui.clickable;

import mpsremodularized.client.gui.MPSIcons;
import mpsremodularized.client.gui.geometry.MusePoint2D;

public abstract class AbstractIconButton extends Clickable {
    MPSIcons.DrawableIcon icon;

    public AbstractIconButton(MPSIcons.DrawableIcon icon, double width, double height, double leftPos, double topPos) {
        this(icon, new MusePoint2D(width, height), new MusePoint2D(leftPos, topPos));
    }

    public AbstractIconButton(MPSIcons.DrawableIcon icon, MusePoint2D wh, MusePoint2D ul) {
        super(ul, wh);
        this.icon = icon;
        super.setWH(wh);
        super.setUL(ul);
    }

    public MPSIcons.DrawableIcon getIcon() {
        return icon;
    }
}
