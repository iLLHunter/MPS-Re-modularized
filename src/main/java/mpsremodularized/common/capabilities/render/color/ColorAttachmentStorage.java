package mpsremodularized.common.capabilities.render.color;

import mpsremodularized.common.math.Color;
import mpsremodularized.common.registration.MPSDataComponents;
import net.neoforged.neoforge.attachment.AttachmentHolder;

public class ColorAttachmentStorage implements IColorTag {
    protected final AttachmentHolder parent;

    public ColorAttachmentStorage(AttachmentHolder parent) {
        this.parent = parent;
    }

    @Override
    public Color getColor() {
        return Color.fromARGB(parent.getData(MPSDataComponents.COLOR_ATTACHMENT));
    }

    @Override
    public void setColor(Color color) {
        parent.setData(MPSDataComponents.COLOR_ATTACHMENT, color.getARGBInt());
    }
}
