package mpsremodularized.client.network;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public class TweakRequestDoublePacketClientHander {
    public static void handlePacket(EquipmentSlot type, ResourceLocation moduleRegName, String tweakName, double tweakValue) {
            final Player player = Minecraft.getInstance().player;
            if (moduleRegName != null && tweakName != null) {
                IModularItem iModularItem = MPSCapabilities.getModularItemOrModeChangingCapability(ItemUtils.getItemFromEntitySlot(player, type));
                if(iModularItem != null) {
                    iModularItem.setModuleDouble(moduleRegName, tweakName, tweakValue);
                }
            }
    }
}
