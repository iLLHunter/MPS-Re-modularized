package mpsremodularized.common.compat;

import io.netty.buffer.Unpooled;
import mpsremodularized.common.network.clientbound.NativeWandSearchPacket;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class NativeWandSearchIntegration {
    private static final String TYPE = "com.github.jarva.arsadditions.setup.networking.PacketRequestEntitySearch";
    private NativeWandSearchIntegration() {}
    @SuppressWarnings("unchecked") private static StreamCodec<io.netty.buffer.ByteBuf, CustomPacketPayload> codec() throws ReflectiveOperationException {
        return (StreamCodec<io.netty.buffer.ByteBuf, CustomPacketPayload>) Class.forName(TYPE).getField("STREAM_CODEC").get(null);
    }
    public static boolean send(CustomPacketPayload nativePacket, ServerPlayer player) {
        var owner = NativeToolIntegration.current();
        if (owner == null || owner.player() != player || !owner.item().donor.toString().equals("ars_additions:advanced_dominion_wand")
                || !nativePacket.getClass().getName().equals(TYPE)) return false;
        if (!owner.valid()) return true;
        var buffer = Unpooled.buffer();
        try {
            codec().encode(buffer, nativePacket); var data = new byte[buffer.readableBytes()]; buffer.readBytes(data);
            PacketDistributor.sendToPlayer(player, new NativeWandSearchPacket(owner.identity(), data)); return true;
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported native wand search codec", error); }
        finally { buffer.release(); }
    }
    public static boolean receive(NativeWandSearchPacket response, IPayloadContext context) {
        if (context.flow().isServerbound()) return false;
        var owner = NativeToolIntegration.find(context.player(), InteractionHand.MAIN_HAND, "ars_additions:advanced_dominion_wand");
        if (owner == null || !owner.identity().equals(response.identity()) || !owner.valid()) return false;
        var buffer = Unpooled.wrappedBuffer(response.data());
        try {
            var nativePacket = codec().decode(buffer);
            if (buffer.isReadable() || nativePacket.getClass().getMethod("hand").invoke(nativePacket) != InteractionHand.MAIN_HAND) return false;
            var work = nativePacket.getClass().getDeclaredMethod("lambda$handleData$1", IPayloadContext.class); work.setAccessible(true);
            return NativeToolIntegration.use(owner, () -> {
                try { work.invoke(nativePacket, context); return true; }
                catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported native entity-search handler", error); }
            });
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported native wand search API", error); }
        finally { buffer.release(); }
    }
}
