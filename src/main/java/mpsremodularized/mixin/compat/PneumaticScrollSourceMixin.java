package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="me.desht.pneumaticcraft.client.event.ClientEventHandler",remap=false)
public abstract class PneumaticScrollSourceMixin {
 @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method="tryHand")
 private static boolean mps$scroll(net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent event,InteractionHand hand,com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original){
  var player=net.minecraft.client.Minecraft.getInstance().player;var owner=NativeToolIntegration.find(player,hand,null);
  if(owner==null||!java.util.Set.of("pneumaticcraft:gps_tool","pneumaticcraft:gps_area_tool").contains(owner.item().donor.toString()))return original.call(event,hand);
  return NativeToolIntegration.use(owner,()->original.call(event,hand));
 }
 @Redirect(method="tryHand",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
 private static ItemStack mps$held(Player player,InteractionHand hand){return NativeToolIntegration.held(player,hand);}
}
