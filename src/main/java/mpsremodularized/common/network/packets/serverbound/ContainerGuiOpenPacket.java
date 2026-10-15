package mpsremodularized.common.network.packets.serverbound;

import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.container.InstallationAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * A packet for sending a containerGui open request from the client side.
 */
public record ContainerGuiOpenPacket(EquipmentSlot slotType, boolean shouldpreserve, double mouseX, double mouseY) implements CustomPacketPayload {
    public static final Type<ContainerGuiOpenPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "container_gui_open_to_server"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ContainerGuiOpenPacket> STREAM_CODEC =
            StreamCodec.ofMember(ContainerGuiOpenPacket::write, ContainerGuiOpenPacket::new);

    public ContainerGuiOpenPacket(RegistryFriendlyByteBuf packetBuffer) {
        this(packetBuffer.readEnum(EquipmentSlot.class), packetBuffer.readBoolean(), packetBuffer.readDouble(),  packetBuffer.readDouble());
    }

    public void write(RegistryFriendlyByteBuf packetBuffer) {
        packetBuffer.writeEnum(slotType);
        packetBuffer.writeBoolean(shouldpreserve);
        packetBuffer.writeDouble(mouseX);
        packetBuffer.writeDouble(mouseY);
    }

    public static void handle(ContainerGuiOpenPacket data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                InstallationAccess.reopen(player, data.slotType, data.shouldpreserve, data.mouseX, data.mouseY);
            }
        });
    }
}
