package mpsremodularized.mixin.compat;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mpsremodularized.common.compat.MekanismDropperIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Pseudo @Mixin(targets={"mekanism.client.gui.element.gauge.GuiTankGauge","mekanism.client.gui.element.bar.GuiTankBar"},remap=false)
public abstract class MekanismDropperGuiMixin {
    @ModifyExpressionValue(method="onClick",at=@At(value="INVOKE",target="Lmekanism/client/gui/IGuiWrapper;getCarriedItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$cursor(ItemStack real) { return MekanismDropperIntegration.cursor(Minecraft.getInstance().player,real); }
}
