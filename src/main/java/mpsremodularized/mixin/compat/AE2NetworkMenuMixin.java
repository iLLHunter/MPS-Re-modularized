package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="appeng.menu.AEBaseMenu",remap=false)
public abstract class AE2NetworkMenuMixin {
 @Inject(method="<init>",at=@At("RETURN"))
 private void mps$bind(net.minecraft.world.inventory.MenuType<?> type,int id,net.minecraft.world.entity.player.Inventory inventory,Object host,CallbackInfo ci){
  if(host==null||!host.getClass().getName().equals("appeng.items.contents.NetworkToolMenuHost"))return;
  var owner=NativeToolIntegration.find(inventory.player,InteractionHand.MAIN_HAND,"ae2:network_tool");
  if(owner!=null&&inventory.player.level().isClientSide())((mpsremodularized.imixin.common.item.INativeToolMenu)this).mpsremodularized$bindNativeOwner(owner,owner::save);
 }
 @Inject(method="stillValid",at=@At("HEAD"),cancellable=true)
 private void mps$valid(Player player,CallbackInfoReturnable<Boolean> ci){var menu=(mpsremodularized.imixin.common.item.INativeToolMenu)this;if(menu.mpsremodularized$nativeOwner()!=null&&!menu.mpsremodularized$nativeOwnerValid())ci.setReturnValue(false);}
 @Inject(method="isValidMenu",at=@At("HEAD"),cancellable=true)
 private void mps$actions(CallbackInfoReturnable<Boolean> ci){if(!((mpsremodularized.imixin.common.item.INativeToolMenu)this).mpsremodularized$nativeOwnerValid())ci.setReturnValue(false);}
 @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
 private void mps$move(Player player,int slot,CallbackInfoReturnable<ItemStack> ci){if(!((mpsremodularized.imixin.common.item.INativeToolMenu)this).mpsremodularized$nativeOwnerValid())ci.setReturnValue(ItemStack.EMPTY);}
}
