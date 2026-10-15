package mpsremodularized.common.event;

import mpsremodularized.common.compat.NativeToolIntegration;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/** The selected area tool reserves left click for P2 instead of a Fist attack. */
@EventBusSubscriber(modid = MPSConstants.MOD_ID)
public final class NativeGPSAreaAttackEvent {
    @SubscribeEvent
    public static void attack(AttackEntityEvent event) {
        if (NativeToolIntegration.find(event.getEntity(), InteractionHand.MAIN_HAND, "pneumaticcraft:gps_area_tool") != null)
            event.setCanceled(true);
    }
}
