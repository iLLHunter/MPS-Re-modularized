package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets={"appeng.block.AEBaseEntityBlock","appeng.block.networking.CableBusBlock"},remap=false)
public abstract class NativeAEBlockUseMixin {
 @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method="useItemOn")
 private net.minecraft.world.ItemInteractionResult mps$use(ItemStack held,net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,Player player,InteractionHand hand,net.minecraft.world.phys.BlockHitResult hit,com.llamalad7.mixinextras.injector.wrapoperation.Operation<net.minecraft.world.ItemInteractionResult> original){
  var owner=NativeToolIntegration.find(player,hand,null);
  if(owner==null||held!=owner.fist()||!java.util.Set.of("ae2:memory_card","enderio_evolution:ender_memory_card","advanced_ae:throughput_monitor_configurator").contains(owner.item().donor.toString()))return original.call(held,state,level,pos,player,hand,hit);
  return NativeToolIntegration.use(owner,()->original.call(owner.source(),state,level,pos,player,hand,hit));
 }
}
