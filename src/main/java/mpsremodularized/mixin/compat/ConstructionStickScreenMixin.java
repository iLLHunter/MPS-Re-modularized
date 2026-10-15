package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.ConstructionStickIntegration;
import mpsremodularized.common.compat.NativeToolIntegration;
import mpsremodularized.imixin.common.item.INativeToolScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bind native options to the module that opened the screen, including after a mode change. */
@Pseudo @Mixin(targets = "mrbysco.constructionstick.client.StickScreen", remap = false)
public abstract class ConstructionStickScreenMixin implements INativeToolScreen {
    @Unique private NativeToolIntegration.Installed mps$owner;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void mps$bind(ItemStack source, CallbackInfo ci) {
        var owner = ConstructionStickIntegration.selected(Minecraft.getInstance().player);
        if (owner != null && source.is(owner.source().getItem())) mps$owner = owner;
    }
    @Override public NativeToolIntegration.Installed mpsremodularized$screenOwner() { return mps$owner; }
}
