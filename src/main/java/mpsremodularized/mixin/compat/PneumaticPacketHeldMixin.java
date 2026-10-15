package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets={"me.desht.pneumaticcraft.common.network.PacketChangeGPSToolCoordinate","me.desht.pneumaticcraft.common.network.PacketUpdateGPSAreaTool","me.desht.pneumaticcraft.common.network.PacketUpdateRemoteLayout","me.desht.pneumaticcraft.common.network.PacketShiftScrollWheel"},remap=false)
public abstract class PneumaticPacketHeldMixin {
 @Redirect(method="handle",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
 private static ItemStack mps$held(Player player,InteractionHand hand){return NativeToolIntegration.held(player,hand);}
}
