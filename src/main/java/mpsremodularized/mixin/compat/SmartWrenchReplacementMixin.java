package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="mcjty.rftoolsbase.modules.various.items.SmartWrenchItem",remap=false)
public abstract class SmartWrenchReplacementMixin {
 @Redirect(method="use",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
 private void mps$replace(Player player,InteractionHand hand,ItemStack stack){NativeToolIntegration.replaceWrench(player,hand,stack);}
}
