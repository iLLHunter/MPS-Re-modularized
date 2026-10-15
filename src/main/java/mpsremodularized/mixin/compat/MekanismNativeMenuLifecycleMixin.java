package mpsremodularized.mixin.compat;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="mekanism.common.inventory.container.MekanismContainer",remap=false)
public abstract class MekanismNativeMenuLifecycleMixin {
    @Inject(method={"broadcastChanges","removed"},at=@At("HEAD"))
    private void mps$save(CallbackInfo ci) { if(this instanceof INativeToolMenu menu)menu.mpsremodularized$saveNativeSource(); }
    @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
    private void mps$move(Player player,int slot,CallbackInfoReturnable<ItemStack> ci) { if(this instanceof INativeToolMenu menu&&!menu.mpsremodularized$nativeOwnerValid())ci.setReturnValue(ItemStack.EMPTY); }
}
