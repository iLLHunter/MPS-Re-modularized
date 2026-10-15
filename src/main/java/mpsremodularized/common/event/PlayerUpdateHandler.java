package mpsremodularized.common.event;

import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.item.module.movement.SprintAssistModule;
import mpsremodularized.common.item.module.vision.NightVisionModule;
import mpsremodularized.common.container.InstallationAccess;
import mpsremodularized.common.utils.HeatUtils;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.common.utils.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class PlayerUpdateHandler {

    @SubscribeEvent
    public static void beforePlayerUpdate(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            InstallationAccess.clearInvalid(player);
            SprintAssistModule.onServerPlayerPreTick(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onPlayerUpdate(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!shouldTickPlayer(player.level().isClientSide(), player.isLocalPlayer())) {
            return;
        }
        Level level = player.level();
        NightVisionModule.beforePlayerTick(player);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack modularItem = ItemUtils.getItemFromEntitySlot(player, slot);
            if(modularItem.isEmpty()) {
                continue;
            }

            IModularItem iModularItemCap = MPSCapabilities.getModularItemOrModeChangingCapability(modularItem);
            if(iModularItemCap != null) {
                if(slot.isArmor() || iModularItemCap instanceof IModeChangingItem) {
                    iModularItemCap.tick(player, level, modularItem);
                }
            }
        }
        // Inventory gear cools on the server even when no modular gear is equipped.
        if (!level.isClientSide()) {
            double coolPlayerAmount = PlayerUtils.getPlayerCoolingBasedOnMaterial(player) * 0.55;
            if (coolPlayerAmount > 0) {
                HeatUtils.coolPlayer(player, coolPlayerAmount);
            }
            HeatUtils.PlayerHeat equippedHeat = HeatUtils.getPlayerHeat(player);
            if (equippedHeat.maxHeat() > 0 && equippedHeat.currentHeat() < equippedHeat.maxHeat() * 0.95) {
                player.clearFire();
            }
        }
    }

    /** Every server player ticks; client input belongs only to the local player. */
    public static boolean shouldTickPlayer(boolean clientSide, boolean localPlayer) {
        return !clientSide || localPlayer;
    }

    // Vanilla persists the complete inventory and applies keepInventory after death is confirmed.
    // Leave death and clone handling to that path so revival and grave mods see normal items.
}
