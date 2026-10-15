package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="me.desht.pneumaticcraft.common.item.GPSAreaToolItem",remap=false)
public abstract class PneumaticAreaHeldMixin {
 @Redirect(method="setGPSPosAndNotify(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/core/BlockPos;I)V",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
 private static ItemStack mps$held(Player player,InteractionHand hand){return NativeToolIntegration.held(player,hand);}
 @Redirect(method="onLeftClickEmpty",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
 private ItemStack mps$left(net.minecraft.server.level.ServerPlayer player){return NativeToolIntegration.held(player,InteractionHand.MAIN_HAND);}
}
