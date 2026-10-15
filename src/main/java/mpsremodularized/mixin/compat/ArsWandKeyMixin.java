package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="com.hollingsworth.arsnouveau.client.keybindings.KeyHandler",remap=false)
public abstract class ArsWandKeyMixin {
 @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method="checkCasterKeys")
 private static void mps$keys(int key,com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original){
  var owner=NativeToolIntegration.find(net.minecraft.client.Minecraft.getInstance().player,InteractionHand.MAIN_HAND,"ars_additions:advanced_dominion_wand");
  if(owner==null)original.call(key);else NativeToolIntegration.use(owner,()->{original.call(key);return null;});
 }
}
