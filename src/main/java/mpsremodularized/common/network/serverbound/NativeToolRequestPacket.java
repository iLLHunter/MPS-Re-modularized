package mpsremodularized.common.network.serverbound;

import mpsremodularized.common.compat.NativeToolRequests;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Native request bytes plus the sealed donor identity captured by its originating screen/control. */
public record NativeToolRequestPacket(String identity, ResourceLocation nativeType, byte[] data) implements CustomPacketPayload {
    public static final Type<NativeToolRequestPacket> ID = new Type<>(ResourceLocation.parse("mpsremodularized:native_tool_request"));
    public static final StreamCodec<FriendlyByteBuf, NativeToolRequestPacket> STREAM_CODEC = StreamCodec.ofMember(NativeToolRequestPacket::write, NativeToolRequestPacket::new);
    public NativeToolRequestPacket(FriendlyByteBuf buffer) { this(buffer.readUtf(), buffer.readResourceLocation(), buffer.readByteArray(1_048_576)); }
    public void write(FriendlyByteBuf buffer) { buffer.writeUtf(identity); buffer.writeResourceLocation(nativeType); buffer.writeByteArray(data); }
    @Override public Type<NativeToolRequestPacket> type() { return ID; }
    public static void handle(NativeToolRequestPacket packet, IPayloadContext context) { NativeToolRequests.handle(packet, context); }
}
