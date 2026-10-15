package mpsremodularized.mixin.compat;

import mpsremodularized.client.compat.MekanismOutlinedReading;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets="mekanism.client.render.hud.MekanismHUD$DelayedString",remap=false)
public abstract class MekanismOutlinedReadingMixin implements MekanismOutlinedReading {
    @Shadow @Final private Matrix4f matrix;
    @Shadow @Final private Component component;
    @Shadow @Final private float x;
    @Shadow @Final private float y;
    @Shadow @Final private int color;
    @Unique private boolean mps$outlined;

    @Override
    public void mps$outline() {
        mps$outlined=true;
    }

    @Inject(method="draw",at=@At("HEAD"),cancellable=true)
    private void mps$readableText(GuiGraphics graphics, Font font, CallbackInfo ci) {
        if(!mps$outlined)return;
        // Keep the native text, placement and severity colours; outline the glyphs on all sides.
        font.drawInBatch8xOutline(component.getVisualOrderText(),x,y,color | 0xFF000000,
                0xFF000000,matrix,graphics.bufferSource(),LightTexture.FULL_BRIGHT);
        ci.cancel();
    }
}
