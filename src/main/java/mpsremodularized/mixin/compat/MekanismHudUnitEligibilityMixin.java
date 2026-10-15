package mpsremodularized.mixin.compat;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mpsremodularized.common.compat.MekanismHelmetIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Pseudo @Mixin(targets="mekanism.client.render.hud.MekanismHUD",remap=false)
public abstract class MekanismHudUnitEligibilityMixin {
    @ModifyExpressionValue(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean mps$eligible(boolean original) {var player=Minecraft.getInstance().player;return original||player!=null&&MekanismHelmetIntegration.hudContainer(player.getItemBySlot(EquipmentSlot.HEAD))!=null;}
}
