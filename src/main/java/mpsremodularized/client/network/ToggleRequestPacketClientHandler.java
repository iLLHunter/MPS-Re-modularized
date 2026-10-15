package mpsremodularized.client.network;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.registration.MPSCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ToggleRequestPacketClientHandler {
    public static void handlePacket(ResourceLocation registryName, boolean toggleval) {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                IModularItem iModularItem = MPSCapabilities.getModularItemOrModeChangingCapability(player.getInventory().getItem(i));
                if(iModularItem != null) {
                    iModularItem.toggleModule(registryName, toggleval);
                }
            }
    }
}
