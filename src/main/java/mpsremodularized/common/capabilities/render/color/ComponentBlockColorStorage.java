package mpsremodularized.common.capabilities.render.color;

import mpsremodularized.common.math.Color;
import mpsremodularized.common.registration.MPSDataComponents;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

public class ComponentBlockColorStorage implements IColorTag {
    protected final MutableDataComponentHolder parent;

    public ComponentBlockColorStorage(MutableDataComponentHolder parent) {
        this.parent = parent;
    }

    @Override
    public Color getColor() {
        return (Color) parent.getOrDefault(MPSDataComponents.COLOR, Color.WHITE);
    }

    @Override
    public void setColor(Color color) {
        parent.set(MPSDataComponents.COLOR, color.getARGBInt());
    }
}
