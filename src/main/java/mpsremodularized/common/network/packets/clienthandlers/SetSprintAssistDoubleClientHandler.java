package mpsremodularized.common.network.packets.clienthandlers;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.network.packets.clientbound.SetSprintAssistDoubleClientBound;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SetSprintAssistDoubleClientHandler {
    public static void handlePacket(SetSprintAssistDoubleClientBound data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            double value = data.boost();

            IModularItem iModularItem = MPSCapabilities.getModularItem(ItemUtils.getItemFromEntitySlot(player, EquipmentSlot.LEGS));
            if(iModularItem != null) {
                if (iModularItem.setModuleDouble(MPSConstants.SPRINT_ASSIST_MODULE, MPSConstants.MOVEMENT_SPEED, value)) {
//                    MPSLogger.logDebug("set sprint value client side: " + value);
                }
            }
        });
    }
}
