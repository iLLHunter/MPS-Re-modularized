package mpsremodularized.common.network.serverbound;

import mpsremodularized.common.capabilities.render.modelspec.IModelSpec;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.network.MPSPackets;
import mpsremodularized.common.network.clientbound.ColorInfoPacketClientBound;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ItemUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record ColorInfoPacketServerBound(EquipmentSlot slotType, int[] tagData) implements CustomPacketPayload {
    public static final Type<ColorInfoPacketServerBound> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "color_info_to_server"));

    @Override
    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static final StreamCodec<FriendlyByteBuf, ColorInfoPacketServerBound> STREAM_CODEC =
            StreamCodec.ofMember(ColorInfoPacketServerBound::write, ColorInfoPacketServerBound::new);

    public ColorInfoPacketServerBound(FriendlyByteBuf packetBuffer) {
        this(packetBuffer.readEnum(EquipmentSlot.class), packetBuffer.readVarIntArray());
    }

    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeEnum(slotType);
        packetBuffer.writeVarIntArray(tagData);
    }

    public static void sendToClient(ServerPlayer entity, EquipmentSlot slotType, int[] tagData) {
        MPSPackets.sendToPlayer(new ColorInfoPacketClientBound(slotType, tagData), entity);
    }

    public static void handle(ColorInfoPacketServerBound data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player  = ctx.player();
            IModelSpec spec = ItemUtils.getItemFromEntitySlot(player, data.slotType).getCapability(MPSCapabilities.RENDER);
            if(spec != null) {
                ItemStack newStack = spec.setColorArray(data.tagData);
                player.setItemSlot(data.slotType, newStack);
            }
//                player.containerMenu.broadcastChanges();
                sendToClient((ServerPlayer) player, data.slotType, data.tagData); // this seems faster than letting changes propagate through player.containerMenu mechanics
        });
    }
}
