package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="me.desht.pneumaticcraft.common.inventory.RemoteMenu",remap=false)
public abstract class PneumaticRemoteMenuMixin {
 @Shadow @Final private InteractionHand hand;
 @Redirect(method="<init>",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
 private ItemStack mps$held(Player player,InteractionHand hand){return NativeToolIntegration.selected(player,hand,"pneumaticcraft:remote");}
 @Inject(method="<init>",at=@At("RETURN"))
 private void mps$bind(net.minecraft.world.inventory.MenuType<?> type,int id,net.minecraft.world.entity.player.Inventory inventory,@Coerce Object last,CallbackInfo ci){
  var owner=NativeToolIntegration.find(inventory.player,hand,"pneumaticcraft:remote");
  if(owner!=null&&inventory.player.level().isClientSide())((mpsremodularized.imixin.common.item.INativeToolMenu)this).mpsremodularized$bindNativeOwner(owner,owner::save);
 }
 @Inject(method="stillValid",at=@At("HEAD"),cancellable=true)
 private void mps$valid(Player player,CallbackInfoReturnable<Boolean> ci){var menu=(mpsremodularized.imixin.common.item.INativeToolMenu)this;if(menu.mpsremodularized$nativeOwner()!=null)ci.setReturnValue(menu.mpsremodularized$nativeOwnerValid());}
}
