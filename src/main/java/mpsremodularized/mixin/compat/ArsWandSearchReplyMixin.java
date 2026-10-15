package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeWandSearchIntegration;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo @Mixin(targets="com.github.jarva.arsadditions.setup.networking.NetworkHandler",remap=false)
public abstract class ArsWandSearchReplyMixin {
    @Inject(method="sendToPlayerClient",at=@At("HEAD"),cancellable=true)
    private static void mps$ownedResponse(CustomPacketPayload packet, ServerPlayer player, CallbackInfo ci) {
        if (NativeWandSearchIntegration.send(packet, player)) ci.cancel();
    }
}
