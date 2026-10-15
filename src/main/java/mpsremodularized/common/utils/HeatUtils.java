package mpsremodularized.common.utils;

import mpsremodularized.common.capabilities.heat.IHeatStorage;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSCapabilities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for heating and cooling.
 * Note: values can be read on either logical side, but should only be set server side
 */
public class HeatUtils {
    public record PlayerHeat(double currentHeat, double maxHeat) {}

    public static PlayerHeat getPlayerHeat(LivingEntity entity) {
        double currentHeat = 0;
        double maxHeat = 0;

        Map<EquipmentSlot, IHeatStorage> heatCapMap = getHeatCapMap(entity);
        for (Map.Entry<EquipmentSlot, IHeatStorage> entry : heatCapMap.entrySet()) {
            currentHeat += entry.getValue().getHeatStored();
            maxHeat += entry.getValue().getMaxHeatStored();
        }

        return  new PlayerHeat(currentHeat, maxHeat);
    }

    public static double coolPlayer(LivingEntity entity, double coolJoules) {
        if (entity.level().isClientSide /*|| entity.abilities.instabuild */) {
            return 0;
        }

        if (entity instanceof Player player) {
            return coolInventory(player.getInventory(), player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY, coolJoules);
        }
        double coolingLeft = coolJoules;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (coolingLeft > 0) {
                coolingLeft -= coolItem(ItemUtils.getItemFromEntitySlot(entity, slot), coolingLeft);
            } else {
                break;
            }
        }
        return coolJoules - coolingLeft;
    }

    /** Called on the server; inventory includes carried, armor and offhand slots. */
    public static double coolInventory(Inventory inventory, ItemStack inUse, double coolJoules) {
        if (!Double.isFinite(coolJoules) || coolJoules <= 0) return 0;
        double coolingLeft = coolJoules;
        for (int slot = 0; slot < inventory.getContainerSize() && coolingLeft > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack != inUse) coolingLeft -= coolItem(stack, coolingLeft);
        }
        double cooled = coolJoules - coolingLeft;
        if (cooled > 0) inventory.setChanged();
        return cooled;
    }

    /**
     * Should only be called server side
     */
    public static double heatPlayer(LivingEntity entity, double heatJoules) {
        if (entity.level().isClientSide || !Double.isFinite(heatJoules) || heatJoules <= 0) {
            return 0;
        }

        Map<EquipmentSlot, IHeatStorage> heatCapMap = getHeatCapMap(entity);
        if(heatCapMap.isEmpty()){
            return 0;
        }

        double heatLeftToGive = heatJoules;

        for (Map.Entry<EquipmentSlot, IHeatStorage> entry : heatCapMap.entrySet()) {
            if (heatLeftToGive == 0) {
                break;
            }
            IHeatStorage iHeatStorage = entry.getValue();
            heatLeftToGive -= iHeatStorage.receiveHeat(heatLeftToGive, false);
        }

        // Existing heat storage still overflows when it has no remaining headroom.
        if (heatLeftToGive > 0) {
            entity.hurt(overheat(entity), (float) heatLeftToGive);
        }

        return heatLeftToGive;
    }

    /**
     * Heats the player's equipment and puts out player >>IF<< the equipment's
     * temperature is below the max threshold.
     * @param event
     */
    @SubscribeEvent
    public static void heatEntity(LivingIncomingDamageEvent event) {

        if (event.getSource().is(DamageTypeTags.IS_FIRE) && event.getEntity() instanceof Player player) {
            // round amount due do float values being weird
            final double originalHeatToGive = Math.round(event.getAmount());
            double heatLeftToGive = originalHeatToGive;

            Map<EquipmentSlot, IHeatStorage> heatCapMap = getHeatCapMap(player);
            boolean allPresent = allPresent(heatCapMap);

            for (Map.Entry<EquipmentSlot, IHeatStorage> entry : heatCapMap.entrySet()) {
                if (heatLeftToGive == 0) {
                    break;
                }
                IHeatStorage iHeatStorage = entry.getValue();
                heatLeftToGive -= iHeatStorage.receiveHeat(heatLeftToGive, false);
            }

            // check if any heat applied to entity otherwise do nothing
            if (heatLeftToGive < originalHeatToGive) {
                if (allPresent) {
                    event.setCanceled(true);
                }

                if (heatLeftToGive > 0) {
                    player.hurt(overheat(player), (float) heatLeftToGive);
                }
            }
        }
    }

    static Map<EquipmentSlot, IHeatStorage> getHeatCapMap (LivingEntity entity) {
        Map<EquipmentSlot, IHeatStorage> capMap = new HashMap<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = ItemUtils.getItemFromEntitySlot(entity, slot);
            IHeatStorage cap = stack.getCapability(MPSCapabilities.HEAT);
            if(cap != null) {
                capMap.put(slot, cap);
            }
        }
        return capMap;
    }

    static boolean allPresent(Map<EquipmentSlot, IHeatStorage> heatCapMap) {
        return heatCapMap.containsKey(EquipmentSlot.HEAD) &&
            heatCapMap.containsKey(EquipmentSlot.CHEST) &&
            heatCapMap.containsKey(EquipmentSlot.LEGS) &&
            heatCapMap.containsKey(EquipmentSlot.FEET);
    }

    public static double heatItem(@Nonnull ItemStack stack, double value) {
        IHeatStorage iHeatStorage = stack.getCapability(MPSCapabilities.HEAT);
        if(iHeatStorage != null) {
            return iHeatStorage.receiveHeat(value, false);
        }
        return 0;
    }

    public static double coolItem(@Nonnull ItemStack stack, double value) {
        IHeatStorage iHeatStorage = stack.getCapability(MPSCapabilities.HEAT);
        if(iHeatStorage != null) {
            return iHeatStorage.extractHeat(value, false);
        }
        return 0;
    }

    public static final ResourceKey<DamageType> OVERHEAT_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, MPSConstants.OVERHEAT_DAMAGE_REGANAME);

    public static DamageSource overheat(LivingEntity target) {
        return new DamageSource(target.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(OVERHEAT_DAMAGE));
    }
}
