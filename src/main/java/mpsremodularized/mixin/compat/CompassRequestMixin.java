package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeToolIntegration;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo @Mixin(targets={"com.chaosthedude.naturescompass.network.SearchPacket", "com.chaosthedude.naturescompass.network.SearchForNextPacket", "com.chaosthedude.naturescompass.network.TeleportPacket", "com.chaosthedude.explorerscompass.network.SearchPacket", "com.chaosthedude.explorerscompass.network.SearchForNextPacket", "com.chaosthedude.explorerscompass.network.TeleportPacket"}, remap=false)
public abstract class CompassRequestMixin {
    @Inject(method="handle", at=@At("HEAD"), cancellable=true)
    private static void mps$identity(@Coerce Object packet, IPayloadContext context, CallbackInfo ci) {
        if (!context.flow().isServerbound()) return;
        String namespace = ((CustomPacketPayload) packet).type().id().getNamespace();
        if (NativeToolIntegration.find(context.player(), InteractionHand.MAIN_HAND, namespace + ":" + namespace) != null) ci.cancel();
    }
}
