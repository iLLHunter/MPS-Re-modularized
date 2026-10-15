package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.NativeToolIntegration;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Pseudo @Mixin(targets="me.desht.pneumaticcraft.common.item.GPSAreaToolItem$EventHandler",remap=false)
public abstract class PneumaticAreaClickMixin {
 @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method="onBlockLeftClick")
 private static void mps$block(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event,com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original){var owner=NativeToolIntegration.find(event.getEntity(),event.getHand(),"pneumaticcraft:gps_area_tool");if(owner==null)original.call(event);else NativeToolIntegration.use(owner,()->{original.call(event);return null;});}
 @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method="onLeftClickAir")
 private static void mps$air(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event,com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original){var owner=NativeToolIntegration.find(event.getEntity(),event.getHand(),"pneumaticcraft:gps_area_tool");if(owner==null)original.call(event);else NativeToolIntegration.use(owner,()->{original.call(event);return null;});}
 @Redirect(method="onBlockLeftClick",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$LeftClickBlock;getItemStack()Lnet/minecraft/world/item/ItemStack;"))
 private static net.minecraft.world.item.ItemStack mps$blockStack(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event){return NativeToolIntegration.held(event.getEntity(),event.getHand());}
 @Redirect(method="onLeftClickAir",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$LeftClickEmpty;getItemStack()Lnet/minecraft/world/item/ItemStack;"))
 private static net.minecraft.world.item.ItemStack mps$airStack(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event){return NativeToolIntegration.held(event.getEntity(),event.getHand());}
}
