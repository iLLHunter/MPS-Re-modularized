package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.NativeToolIntegration;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="dev.su5ed.mffs.menu.FortronMenu",remap=false)
public abstract class MffsRemoteMenuMixin {
 @Inject(method="<init>",at=@At("RETURN"))
 private void mps$bind(CallbackInfo ci)throws ReflectiveOperationException {
  var playerField=Class.forName("dev.su5ed.mffs.menu.FortronMenu").getDeclaredField("player");playerField.setAccessible(true);
  var player=(Player)playerField.get(this);
  if(!player.level().isClientSide())return;
  var owner=NativeToolIntegration.find(player,InteractionHand.MAIN_HAND,"mffs:remote_controller");
  var block=(net.minecraft.world.level.block.entity.BlockEntity)getClass().getField("blockEntity").get(this);
  if(owner!=null&&block.getBlockPos().equals(owner.source().getItem().getClass().getMethod("getLink",ItemStack.class).invoke(owner.source().getItem(),owner.source())))
   ((INativeToolMenu)this).mpsremodularized$bindNativeOwner(owner,owner::save);
 }
 @Redirect(method="lambda$stillValid$0",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
 private static ItemStack mps$remote(Player player,InteractionHand hand){return NativeToolIntegration.selected(player,hand,"mffs:remote_controller");}
 @Inject(method="stillValid",at=@At("HEAD"),cancellable=true)
 private void mps$valid(Player player,CallbackInfoReturnable<Boolean> ci)throws ReflectiveOperationException {
  var menu=(INativeToolMenu)this;
  if(menu.mpsremodularized$nativeOwner()==null)return;
  var block=(net.minecraft.world.level.block.entity.BlockEntity)getClass().getField("blockEntity").get(this);
  if(!menu.mpsremodularized$nativeOwnerValid()||block.isRemoved()||block.getLevel()==null||!block.getLevel().isLoaded(block.getBlockPos()))ci.setReturnValue(false);
 }
 @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
 private void mps$move(Player player,int slot,CallbackInfoReturnable<ItemStack> ci){if(!((INativeToolMenu)this).mpsremodularized$nativeOwnerValid())ci.setReturnValue(ItemStack.EMPTY);}
 @Inject(method="clicked",at=@At("HEAD"),cancellable=true)
 private void mps$click(int slot,int button,net.minecraft.world.inventory.ClickType type,Player player,CallbackInfo ci){if(!mpsremodularized.common.compat.PortableStationIntegration.canClick((net.minecraft.world.inventory.AbstractContainerMenu)(Object)this,slot,button,type))ci.cancel();}
 @Inject(method="clicked",at=@At("RETURN")) private void mps$save(CallbackInfo ci){((INativeToolMenu)this).mpsremodularized$saveNativeSource();}
}
