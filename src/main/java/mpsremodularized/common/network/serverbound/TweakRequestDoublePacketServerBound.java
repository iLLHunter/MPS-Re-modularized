package mpsremodularized.common.network.serverbound;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.network.MPSPackets;
import mpsremodularized.common.network.clientbound.TweakRequestDoublePacketClientBound;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ItemUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record TweakRequestDoublePacketServerBound(EquipmentSlot slotType, ResourceLocation moduleRegName, String tweakName, double tweakValue) implements CustomPacketPayload {
    public static final Type<TweakRequestDoublePacketServerBound> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "tweak_request_to_server"));

    @Nonnull
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static final StreamCodec<FriendlyByteBuf, TweakRequestDoublePacketServerBound> STREAM_CODEC =
        StreamCodec.ofMember(TweakRequestDoublePacketServerBound::write, TweakRequestDoublePacketServerBound::new);

    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeEnum(slotType);
        packetBuffer.writeResourceLocation(moduleRegName);
        packetBuffer.writeUtf(tweakName);
        packetBuffer.writeDouble(tweakValue);
    }

    public TweakRequestDoublePacketServerBound(FriendlyByteBuf packetBuffer) {
        this(
            packetBuffer.readEnum(EquipmentSlot.class),
            packetBuffer.readResourceLocation(),
            packetBuffer.readUtf(500),
            packetBuffer.readDouble());
    }

    public static void sendToClient(ServerPlayer entity, EquipmentSlot slotType, ResourceLocation moduleRegName, String tweakName, double tweakValue) {
        MPSPackets.sendToPlayer(new TweakRequestDoublePacketClientBound(slotType, moduleRegName, tweakName, tweakValue), entity);
    }

    public static void handle(TweakRequestDoublePacketServerBound data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            if (data.moduleRegName != null && data.tweakName != null) {
                ItemStack stack = ItemUtils.getItemFromEntitySlot(player, data.slotType);
                IModularItem iModularItem = MPSCapabilities.getModularItemOrModeChangingCapability(stack);
                if (iModularItem != null) {
                    int index = iModularItem.findInstalledModule(data.moduleRegName);
                    if (index < 0) {
                        return;
                    }
                    IPowerModule module = iModularItem.getModuleCapability(iModularItem.getStackInSlot(index));
                    if (MPSPackets.isAllowedTweak(module, data.tweakName, data.tweakValue)) {
                        iModularItem.setModuleDouble(data.moduleRegName, data.tweakName, data.tweakValue);
                        sendToClient(player, data.slotType, data.moduleRegName, data.tweakName, data.tweakValue);
                    }
                }
            }
        });
    }
}
