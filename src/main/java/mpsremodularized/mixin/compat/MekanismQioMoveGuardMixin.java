package mpsremodularized.mixin.compat;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="mekanism.common.inventory.container.QIOItemViewerContainer",remap=false)
public abstract class MekanismQioMoveGuardMixin {
    @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
    private void mps$guard(Player player,int slot,CallbackInfoReturnable<ItemStack> ci) {if(this instanceof INativeToolMenu menu&&!menu.mpsremodularized$nativeOwnerValid())ci.setReturnValue(ItemStack.EMPTY);}
    @Inject(method="quickMoveStack",at=@At("RETURN"))
    private void mps$save(Player player,int slot,CallbackInfoReturnable<ItemStack> ci) {if(this instanceof INativeToolMenu menu)menu.mpsremodularized$saveNativeSource();}
}
