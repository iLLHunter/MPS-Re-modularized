package mpsremodularized.mixin.compat;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import mpsremodularized.common.compat.MekanismHelmetIntegration;
import mpsremodularized.client.compat.MekanismOutlinedReading;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Pseudo @Mixin(targets="mekanism.client.render.HUDRenderer",remap=false)
public abstract class MekanismHudUnitsMixin {
    @Unique private boolean mps$unitContainer;
    @Unique private boolean mps$outlineReading;

    @WrapOperation(method="renderMekaSuitModuleIcons",at=@At(value="INVOKE",target="Lmekanism/api/gear/IModuleHelper;getModuleContainer(Lnet/minecraft/world/item/ItemStack;)Lmekanism/api/gear/IModuleContainer;"))
    @Coerce private Object mps$units(@Coerce Object helper,ItemStack stack,Operation<Object> original) {
        var context=MekanismHelmetIntegration.hudContainer(stack);
        mps$unitContainer=context!=null;
        return context==null?original.call(helper,stack):context;
    }

    @WrapOperation(method="renderMekaSuitModuleIcons",at=@At(value="INVOKE",target="Lmekanism/client/render/HUDRenderer;renderHUDElement(Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/GuiGraphics;Lorg/joml/Matrix4f;Ljava/util/List;IILmekanism/api/gear/IHUDElement;Z)V"))
    private void mps$unitText(@Coerce Object renderer, Font font, GuiGraphics graphics, Matrix4f matrix,
                              java.util.List<?> delayedDraws, int x, int y, @Coerce Object element,
                              boolean reverse, Operation<Void> original) {
        mps$outlineReading=mps$unitContainer;
        try { original.call(renderer,font,graphics,matrix,delayedDraws,x,y,element,reverse); }
        finally { mps$outlineReading=false; }
    }

    @WrapOperation(method="renderHUDElement",at=@At(value="NEW",target="mekanism/client/render/hud/MekanismHUD$DelayedString"))
    @Coerce private Object mps$markReading(Matrix4f matrix, Component text, float x, float y, int color,
                                          boolean shadow, Operation<Object> original) {
        var reading=original.call(matrix,text,x,y,color,shadow);
        if(mps$outlineReading)((MekanismOutlinedReading)reading).mps$outline();
        return reading;
    }
    @org.spongepowered.asm.mixin.injection.Inject(method="renderCompass",at=@At("HEAD"),cancellable=true)
    private void mps$selectedBenefits(net.minecraft.world.entity.player.Player player,net.minecraft.client.gui.Font font,net.minecraft.client.gui.GuiGraphics graphics,java.util.List<?> delayedDraws,net.minecraft.client.DeltaTracker delta,int width,int height,int textHeight,boolean reverse,int subtitleWidth,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() instanceof mpsremodularized.common.item.electric.armor.AbstractElectricArmor)ci.cancel();
    }
}
