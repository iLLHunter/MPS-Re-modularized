package mpsremodularized.client.network;

import mpsremodularized.common.capabilities.render.modelspec.IModelSpec;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CosmeticInfoPacketClientHandler {
    public static void handlePacket(EquipmentSlot slotType, String tagName, CompoundTag tagData) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            IModelSpec spec = ItemUtils.getItemFromEntitySlot(player, slotType).getCapability(MPSCapabilities.RENDER);
            if(spec != null) {
                ItemStack newStack = spec.setRenderTag(tagData, tagName);
            }
        }
    }
}
