package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="de.ellpeck.actuallyadditions.mod.items.ItemEngineerGoggles",remap=false)
public abstract class EngineerGogglesMixin {
 @Inject(method="isWearing",at=@At("HEAD"),cancellable=true)
 private static void mps$helmet(Player player,CallbackInfoReturnable<Boolean> ci){
  if(NativeArmorIntegration.find(player.getItemBySlot(EquipmentSlot.HEAD),EquipmentSlot.HEAD,CompatibleMod.ACTUALLY_ADDITIONS)!=null)ci.setReturnValue(true);
 }
}
