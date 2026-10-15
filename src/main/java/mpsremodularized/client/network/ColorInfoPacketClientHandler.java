package mpsremodularized.client.network;

import mpsremodularized.common.capabilities.render.modelspec.IModelSpec;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public class ColorInfoPacketClientHandler {
    public static void handlePacket(EquipmentSlot slotType, int[] tagData) {
        final Player player = Minecraft.getInstance().player;
        assert player != null;
        IModelSpec iModelSpec = ItemUtils.getItemFromEntitySlot(player, slotType).getCapability(MPSCapabilities.RENDER);
        if(iModelSpec != null) {
            iModelSpec.setColorArray(tagData);
        }
    }
}
