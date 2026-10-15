package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets={"me.desht.pneumaticcraft.common.item.ManometerItem","me.desht.pneumaticcraft.common.item.RemoteItem","com.sakurain.enderio_evolution.util.item.EnderMemoryCardItem","dev.su5ed.mffs.item.RemoteControllerItem","mcjty.rftoolsbase.modules.various.items.SmartWrenchItem"},remap=false)
public abstract class NativeRemainingUseHeldMixin {
 @Redirect(method="use",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
 private ItemStack mps$held(Player player,InteractionHand hand){return NativeToolIntegration.held(player,hand);}
}
