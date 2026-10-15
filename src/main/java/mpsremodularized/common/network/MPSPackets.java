package mpsremodularized.common.network;

import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.network.clientbound.ColorInfoPacketClientBound;
import mpsremodularized.common.network.clientbound.CosmeticInfoPacketClientBound;
import mpsremodularized.common.network.clientbound.ModeChangeRequestPacketClientBound;
import mpsremodularized.common.network.clientbound.ToggleRequestPacketClientBound;
import mpsremodularized.common.network.clientbound.TweakRequestDoublePacketClientBound;
import mpsremodularized.common.network.packets.clientbound.CreativeInstallPacketClientBound;
import mpsremodularized.common.network.packets.clientbound.SetSprintAssistDoubleClientBound;
import mpsremodularized.common.network.packets.serverbound.ContainerGuiOpenPacket;
import mpsremodularized.common.network.packets.serverbound.CreativeInstallPacketServerBound;
import mpsremodularized.common.network.packets.serverbound.SetSprintAssistDoubleAndDrainServerBound;
import mpsremodularized.common.network.serverbound.BlockPositionPacketServerBound;
import mpsremodularized.common.network.serverbound.BlockStateClearPacketServerBound;
import mpsremodularized.common.network.serverbound.ColorInfoPacketServerBound;
import mpsremodularized.common.network.serverbound.CosmeticInfoPacketServerBound;
import mpsremodularized.common.network.serverbound.ModeChangeRequestPacketServerBound;
import mpsremodularized.common.network.serverbound.PlayerUpdatePacketServerBound;
import mpsremodularized.common.network.serverbound.ToggleRequestPacketServerBound;
import mpsremodularized.common.network.serverbound.TweakRequestDoublePacketServerBound;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.registration.HandlerThread;

public class MPSPackets {
    public static final String PROTOCOL_VERSION = "4.2.0";

    public static void register(final RegisterPayloadHandlersEvent event) {
        // Required on both peers; gameplay handlers mutate state on the main game thread.
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION).executesOn(HandlerThread.MAIN);
        registrar.playToClient(mpsremodularized.common.network.clientbound.NativeWandSearchPacket.ID,
                mpsremodularized.common.network.clientbound.NativeWandSearchPacket.STREAM_CODEC,
                mpsremodularized.common.network.clientbound.NativeWandSearchPacket::handle);
        registrar.playToServer(mpsremodularized.common.network.serverbound.NativeToolRequestPacket.ID,
                mpsremodularized.common.network.serverbound.NativeToolRequestPacket.STREAM_CODEC,
                mpsremodularized.common.network.serverbound.NativeToolRequestPacket::handle);

        // -----------------------------------------------------
        registrar.playToServer(
            BlockPositionPacketServerBound.ID,
            BlockPositionPacketServerBound.STREAM_CODEC,
            BlockPositionPacketServerBound::handle);

        registrar.playToServer(
            BlockStateClearPacketServerBound.ID,
            BlockStateClearPacketServerBound.STREAM_CODEC,
            BlockStateClearPacketServerBound::handle);

        // -----------------------------------------------------
        registrar.playToServer(
            ColorInfoPacketServerBound.ID,
            ColorInfoPacketServerBound.STREAM_CODEC,
            ColorInfoPacketServerBound::handle);

        registrar.playToClient(
            ColorInfoPacketClientBound.ID,
            ColorInfoPacketClientBound.STREAM_CODEC,
            ColorInfoPacketClientBound::handle);

        // -----------------------------------------------------
        registrar.playToServer(
            CosmeticInfoPacketServerBound.ID,
            CosmeticInfoPacketServerBound.STREAM_CODEC,
            CosmeticInfoPacketServerBound::handle);

        registrar.playToClient(
            CosmeticInfoPacketClientBound.ID,
            CosmeticInfoPacketClientBound.STREAM_CODEC,
            CosmeticInfoPacketClientBound::handle);

        // -----------------------------------------------------
        registrar.playToServer(
            ToggleRequestPacketServerBound.ID,
            ToggleRequestPacketServerBound.STREAM_CODEC,
            ToggleRequestPacketServerBound::handle);

        registrar.playToClient(
            ToggleRequestPacketClientBound.ID,
            ToggleRequestPacketClientBound.STREAM_CODEC,
            ToggleRequestPacketClientBound::handle);

        // -----------------------------------------------------
        registrar.playToServer(
            ModeChangeRequestPacketServerBound.ID,
            ModeChangeRequestPacketServerBound.STREAM_CODEC,
            ModeChangeRequestPacketServerBound::handle);

        registrar.playToClient(
            ModeChangeRequestPacketClientBound.ID,
            ModeChangeRequestPacketClientBound.STREAM_CODEC,
            ModeChangeRequestPacketClientBound::handle);

        // -----------------------------------------------------
        registrar.playToServer(
            PlayerUpdatePacketServerBound.ID,
            PlayerUpdatePacketServerBound.STREAM_CODEC,
            PlayerUpdatePacketServerBound::handle);

        // -----------------------------------------------------
        registrar.playToServer(
            TweakRequestDoublePacketServerBound.ID,
            TweakRequestDoublePacketServerBound.STREAM_CODEC,
            TweakRequestDoublePacketServerBound::handle);

        registrar.playToClient(
            TweakRequestDoublePacketClientBound.ID,
            TweakRequestDoublePacketClientBound.STREAM_CODEC,
            TweakRequestDoublePacketClientBound::handle);

        registrar.playToServer(
            CreativeInstallPacketServerBound.ID,
            CreativeInstallPacketServerBound.STREAM_CODEC,
            CreativeInstallPacketServerBound::handle);

        registrar.playToClient(
            CreativeInstallPacketClientBound.ID,
            CreativeInstallPacketClientBound.STREAM_CODEC,
            CreativeInstallPacketClientBound::handle);

        registrar.playToServer(
            ContainerGuiOpenPacket.ID,
            ContainerGuiOpenPacket.STREAM_CODEC,
            ContainerGuiOpenPacket::handle);

        registrar.playToServer(SetSprintAssistDoubleAndDrainServerBound.ID,
            SetSprintAssistDoubleAndDrainServerBound.STREAM_CODEC,
            SetSprintAssistDoubleAndDrainServerBound::handle);

        registrar.playToClient(SetSprintAssistDoubleClientBound.ID,
            SetSprintAssistDoubleClientBound.STREAM_CODEC,
            SetSprintAssistDoubleClientBound::handle);
    }

    public static boolean isAllowedTweak(IPowerModule module, String name, double value) {
        return module != null && name != null && Double.isFinite(value) && value >= 0 && value <= 1
                && module.getPropertyModifiers().values().stream().flatMap(java.util.Collection::stream)
                .anyMatch(modifier -> modifier instanceof IPowerModule.PropertyModifierLinearAdditive tradeoff
                        && name.equals(tradeoff.tradeoffName));
    }

    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }
}
