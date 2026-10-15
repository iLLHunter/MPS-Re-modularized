package mpsremodularized.mixin.compat;

import mezz.jei.common.util.ImmutableRect2i;
import mpsremodularized.client.gui.module.install_salvage.InstallSalvageGui;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** JEI's fixed corner buttons do not use its ingredient-grid exclusion areas. */
@Pseudo
@Mixin(targets = "mezz.jei.gui.elements.IconButton", remap = false)
public abstract class JeiTinkerButtonsMixin {
    @ModifyVariable(method = "updateBounds", at = @At("HEAD"), argsOnly = true)
    private ImmutableRect2i mps$keepSidebarClear(ImmutableRect2i proposed) {
        if (!(Minecraft.getInstance().screen instanceof InstallSalvageGui screen)) return proposed;
        var sidebar = screen.getJeiSidebarArea();
        if (!proposed.intersects(new ImmutableRect2i(sidebar))) return proposed;
        int above = sidebar.getY() - proposed.height();
        if (above >= 0) return proposed.setPosition(proposed.x(), above);
        int below = sidebar.getY() + sidebar.getHeight();
        if (below + proposed.height() <= screen.height) return proposed.setPosition(proposed.x(), below);
        return proposed;
    }
}
