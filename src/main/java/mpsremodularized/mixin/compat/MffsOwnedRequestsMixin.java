package mpsremodularized.mixin.compat;

import mpsremodularized.client.compat.NativeRemainingControls;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only the fixed MFFS menu controls belonging to a sealed remote are wrapped. */
@Mixin(value = PacketDistributor.class, remap = false)
public abstract class MffsOwnedRequestsMixin {
    @Inject(method = "sendToServer", at = @At("HEAD"), cancellable = true)
    private static void mps$ownedRequest(CustomPacketPayload packet, CustomPacketPayload[] additional, CallbackInfo ci) {
        if (packet.type().id().getNamespace().equals("mffs") && NativeRemainingControls.send(packet)) {
            if (additional.length != 0) PacketDistributor.sendToServer(additional[0], java.util.Arrays.copyOfRange(additional, 1, additional.length));
            ci.cancel();
        }
    }
}
