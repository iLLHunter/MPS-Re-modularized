package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="com.hollingsworth.arsnouveau.api.util.StackUtil",remap=false)
public abstract class ArsRadialSourceMixin {
 @Inject(method="getHeldRadial",at=@At("HEAD"),cancellable=true)
 private static void mps$held(Player player,CallbackInfoReturnable<ItemStack> ci){var owner=NativeToolIntegration.current();if(owner!=null&&owner.player()==player&&owner.item().donor.toString().equals("ars_additions:advanced_dominion_wand"))ci.setReturnValue(owner.source());}
}
