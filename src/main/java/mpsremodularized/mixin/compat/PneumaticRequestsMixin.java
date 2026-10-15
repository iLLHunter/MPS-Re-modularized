package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="me.desht.pneumaticcraft.common.network.NetworkHandler",remap=false)
public abstract class PneumaticRequestsMixin {
 @Inject(method="sendToServer",at=@At("HEAD"),cancellable=true)
 private static void mps$request(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload,CallbackInfo ci){if(mpsremodularized.client.compat.NativeRemainingControls.send(payload))ci.cancel();}
}
