package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="appeng.menu.locator.InventoryItemLocator",remap=false)
public abstract class AE2NetworkLocatorMixin {
 @Shadow public abstract int itemIndex();
 @Inject(method="locateItem",at=@At("HEAD"),cancellable=true)
 private void mps$held(Player player,CallbackInfoReturnable<ItemStack> ci){
  if(itemIndex()!=player.getInventory().selected)return;
  var owner=NativeToolIntegration.find(player,InteractionHand.MAIN_HAND,"ae2:network_tool");
  if(owner!=null)ci.setReturnValue(owner.source());
 }
}
