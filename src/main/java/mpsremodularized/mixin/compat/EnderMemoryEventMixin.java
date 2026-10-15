package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="com.sakurain.enderio_evolution.event.MemoryCardInteractionHandler",remap=false)
public abstract class EnderMemoryEventMixin {
 @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method="onRightClickBlock")
 private static void mps$use(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event,com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original){
  var owner=NativeToolIntegration.find(event.getEntity(),event.getHand(),"enderio_evolution:ender_memory_card");
  if(owner==null)original.call(event);else NativeToolIntegration.use(owner,()->{original.call(event);return null;});
 }
 @Redirect(method="onRightClickBlock",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$RightClickBlock;getItemStack()Lnet/minecraft/world/item/ItemStack;"))
 private static ItemStack mps$held(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event){return NativeToolIntegration.held(event.getEntity(),event.getHand());}
}
