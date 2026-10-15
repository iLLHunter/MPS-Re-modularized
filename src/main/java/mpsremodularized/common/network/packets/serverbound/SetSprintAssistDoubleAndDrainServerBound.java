package mpsremodularized.common.network.packets.serverbound;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

/** Client supplies movement intent only; boost and energy billing are server-owned. */
public record SetSprintAssistDoubleAndDrainServerBound(boolean moving) implements CustomPacketPayload {
    public static final Type<SetSprintAssistDoubleAndDrainServerBound> ID = new Type<>(
        ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "sprint_assist_value_request_to_server"));

    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static final StreamCodec<FriendlyByteBuf, SetSprintAssistDoubleAndDrainServerBound> STREAM_CODEC =
        StreamCodec.ofMember(SetSprintAssistDoubleAndDrainServerBound::write, SetSprintAssistDoubleAndDrainServerBound::new);

    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeBoolean(moving);
    }

    public SetSprintAssistDoubleAndDrainServerBound(FriendlyByteBuf packetBuffer) {
        this(packetBuffer.readBoolean());
    }

    public static void handle(SetSprintAssistDoubleAndDrainServerBound data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            IModularItem host = MPSCapabilities.getModularItem(ItemUtils.getItemFromEntitySlot(player, EquipmentSlot.LEGS));
            if (host == null) {
                return;
            }
            var stack = host.getOnlineModuleOrEmpty(MPSConstants.SPRINT_ASSIST_MODULE);
            var module = host.getModuleCapability(stack);
            double boost = 0;
            if (data.moving && host.isModuleOperational(module)) {
                boost = module.applyPropertyModifiers(player.isSprinting()
                        ? MPSConstants.SPRINT_SPEED_MULTIPLIER : MPSConstants.WALKING_SPEED_MULTIPLIER)
                        * (player.isSprinting() ? 0.13 : 0.1);
                double cost = module.applyPropertyModifiers(player.isSprinting()
                        ? MPSConstants.SPRINT_ENERGY_CONSUMPTION : MPSConstants.WALKING_ENERGY_CONSUMPTION);
                if (!Double.isFinite(boost) || !Double.isFinite(cost) || cost < 0
                        || ElectricItemUtils.getPlayerEnergy(player) <= cost) {
                    boost = 0;
                }
            }
            host.setModuleDouble(MPSConstants.SPRINT_ASSIST_MODULE, MPSConstants.MOVEMENT_SPEED, Math.max(0, boost));
        });
    }
}
