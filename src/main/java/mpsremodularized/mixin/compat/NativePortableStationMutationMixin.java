package mpsremodularized.mixin.compat;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="com.blakebr0.mysticalagriculture.container.TinkeringTableContainer",remap=false)
public abstract class NativePortableStationMutationMixin {
 @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
 private void mps$move(Player player,int slot,CallbackInfoReturnable<ItemStack> ci){if(!((INativeToolMenu)this).mpsremodularized$nativeOwnerValid())ci.setReturnValue(ItemStack.EMPTY);}
 @Inject(method="quickMoveStack",at=@At("RETURN"))private void mps$afterMove(CallbackInfoReturnable<ItemStack> ci){((INativeToolMenu)this).mpsremodularized$saveNativeSource();}
 @Inject(method="slotsChanged",at=@At("RETURN"))private void mps$afterChange(net.minecraft.world.Container container,CallbackInfo ci){((INativeToolMenu)this).mpsremodularized$saveNativeSource();}
}
