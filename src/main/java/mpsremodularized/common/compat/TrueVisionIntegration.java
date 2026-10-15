package mpsremodularized.common.compat;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

/** The native client owns camouflage rendering and chunk refreshes. */
public final class TrueVisionIntegration {
    private TrueVisionIntegration() {}

    public static boolean hasOnlineGoggles(Player player) {
        return player != null && NativeArmorIntegration.find(player.getItemBySlot(EquipmentSlot.HEAD),
                EquipmentSlot.HEAD, CompatibleMod.SECRET_ROOMS) != null;
    }
}
