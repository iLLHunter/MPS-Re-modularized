package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="mcjty.lib.blocks.BaseBlock",remap=false)
public abstract class SmartWrenchBlockMixin {
 @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method="useWithoutItem")
 private net.minecraft.world.InteractionResult mps$use(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,Player player,net.minecraft.world.phys.BlockHitResult hit,com.llamalad7.mixinextras.injector.wrapoperation.Operation<net.minecraft.world.InteractionResult> original){
  var owner=NativeToolIntegration.find(player,InteractionHand.MAIN_HAND,"rftoolsbase:smartwrench");
  return owner==null?original.call(state,level,pos,player,hit):NativeToolIntegration.use(owner,()->original.call(state,level,pos,player,hit));
 }
 @Redirect(method={"testWrenchUsage","useWithoutItem"},at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
 private ItemStack mps$held(Player player,InteractionHand hand){return NativeToolIntegration.held(player,hand);}
}
