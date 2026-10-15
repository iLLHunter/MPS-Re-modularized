package mpsremodularized.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo @Mixin(targets = "mrbysco.constructionstick.network.ModMessages", remap = false)
public abstract class ConstructionStickRequestsMixin {
    @Inject(method = "sendToServer", at = @At("HEAD"), cancellable = true)
    private static void mps$request(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload, CallbackInfo ci) {
        if (mpsremodularized.client.compat.NativeRemainingControls.send(payload)) ci.cancel();
    }
}
