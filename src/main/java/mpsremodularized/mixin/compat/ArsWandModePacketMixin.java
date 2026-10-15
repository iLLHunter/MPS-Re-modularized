package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="com.github.jarva.arsadditions.setup.networking.PacketUpdateAdvancedDominionWand",remap=false)
public abstract class ArsWandModePacketMixin {
 @Redirect(method="lambda$handleData$0",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
 private ItemStack mps$held(Player player){return NativeToolIntegration.held(player,InteractionHand.MAIN_HAND);}
}
