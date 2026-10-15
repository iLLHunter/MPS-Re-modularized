package mpsremodularized.common.network.packets.serverbound;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSDataComponents;
import mpsremodularized.common.item.Battery;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CreativeInstallPacketServerBound(EquipmentSlot slotType, ResourceLocation regName) implements CustomPacketPayload {
    public static final Type<CreativeInstallPacketServerBound> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "creative_install_to_server"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, CreativeInstallPacketServerBound> STREAM_CODEC =
        StreamCodec.ofMember(CreativeInstallPacketServerBound::write, CreativeInstallPacketServerBound::new);

    public void write(RegistryFriendlyByteBuf packetBuffer) {
        packetBuffer.writeEnum(slotType);
        packetBuffer.writeResourceLocation(regName);
    }

    public CreativeInstallPacketServerBound(RegistryFriendlyByteBuf packetBuffer) {
        this(packetBuffer.readEnum(EquipmentSlot.class), packetBuffer.readResourceLocation());
    }

    //    public static void sendToClient(ServerPlayer entity, EquipmentSlot slotType, ResourceLocation regName) {
    //        MPSPackets.sendToPlayer(new CreativeInstallPacketClientBound(slotType, regName), entity);
    //    }

    public static void handle(CreativeInstallPacketServerBound data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player) || !player.isCreative()
                    || data.regName == null || !BuiltInRegistries.ITEM.containsKey(data.regName)) {
                return;
            }
            EquipmentSlot slotType = data.slotType;
            ResourceLocation regName = data.regName;
            Item item = BuiltInRegistries.ITEM.get(regName);
            ItemStack module = item instanceof mpsremodularized.common.compat.NativeModuleItem nativeItem
                    ? nativeItem.creativeModule(player.registryAccess()) : new ItemStack(item, 1);
            if (module.isEmpty()) return;

            if (module.getCapability(MPSCapabilities.Module.POWER_MODULE) == null) {
                return;
            }

            IEnergyStorage iEnergyStorage = module.getCapability(Capabilities.EnergyStorage.ITEM);
            if (iEnergyStorage != null && item instanceof Battery) {
                // Creative MPS batteries are full even when their transfer limit is zero.
                module.set(MPSDataComponents.ENERGY, iEnergyStorage.getMaxEnergyStored());
            }

            IModularItem iModularItem = MPSCapabilities.getModularItemOrModeChangingCapability(ItemUtils.getItemFromEntitySlot(player, slotType));
            if(iModularItem != null) {
                int index = iModularItem.findInstalledModule(module);
                // Todo: upgrade modules
                if(index < 0) {
                    for (index = 0; index < iModularItem.getSlots(); index++) {
                        if(iModularItem.isModuleValidForPlacement(index, module) && iModularItem.insertItem(index, module, false).isEmpty())  {
                            break;
                        }
                    }
                } else {
                    ItemStack tmp = iModularItem.getStackInSlot(index);
                    iModularItem.setStackInSlot(index, ItemStack.EMPTY);
                    if(iModularItem.isModuleValidForPlacement(index, module)) {
                        iModularItem.setStackInSlot(index, module);
                    } else {
                        iModularItem.setStackInSlot(index, tmp);
                    }
                }
            }
        });
    }
}
