package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="com.blakebr0.cucumber.container.BaseContainerMenu",remap=false)
public abstract class NativePortableTinkeringValidityMixin {
 @Inject(method="stillValid",at=@At("HEAD"),cancellable=true)
 private void mps$valid(Player player,CallbackInfoReturnable<Boolean> ci){var menu=(mpsremodularized.imixin.common.item.INativeToolMenu)this;if(menu.mpsremodularized$nativeOwner()!=null)ci.setReturnValue(menu.mpsremodularized$nativeOwnerValid());}
}
