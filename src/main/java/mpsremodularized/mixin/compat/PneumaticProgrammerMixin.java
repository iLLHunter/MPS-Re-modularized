package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="me.desht.pneumaticcraft.client.gui.ProgrammerScreen",remap=false)
public abstract class PneumaticProgrammerMixin {
 @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method="mouseClicked",at=@At(value="INVOKE",target="Lnet/minecraft/world/inventory/AbstractContainerMenu;getCarried()Lnet/minecraft/world/item/ItemStack;"))
 private ItemStack mps$cursor(ItemStack cursor){return mpsremodularized.client.compat.NativeRemainingControls.programmerSource(cursor);}
}
