package mpsremodularized.client.network;

import mpsremodularized.common.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ModeChangeRequestPacketClientHandler {
    public static void handlePacket(int mode) {
        final Player player = Minecraft.getInstance().player;
        if (player != null) {
//            MPSLogger.logDebug("trying to handle packet client side");
            ItemUtils.setModeAndSwapIfNeeded(player, mode);
//            MPSLogger.logDebug("supposedly updated mode client side");
        }
    }
}
