package mpsremodularized.common.network.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** The native entity-search request plus its initiating sealed-wand identity. */
public record NativeWandSearchPacket(String identity, byte[] data) implements CustomPacketPayload {
    public static final Type<NativeWandSearchPacket> ID = new Type<>(ResourceLocation.parse("mpsremodularized:native_wand_search"));
    public static final StreamCodec<FriendlyByteBuf, NativeWandSearchPacket> STREAM_CODEC = StreamCodec.ofMember(NativeWandSearchPacket::write, NativeWandSearchPacket::new);
    private NativeWandSearchPacket(FriendlyByteBuf buffer) { this(buffer.readUtf(), buffer.readByteArray(128)); }
    private void write(FriendlyByteBuf buffer) { buffer.writeUtf(identity); buffer.writeByteArray(data); }
    @Override public Type<NativeWandSearchPacket> type() { return ID; }
    public static void handle(NativeWandSearchPacket packet, IPayloadContext context) { mpsremodularized.common.compat.NativeWandSearchIntegration.receive(packet, context); }
}
