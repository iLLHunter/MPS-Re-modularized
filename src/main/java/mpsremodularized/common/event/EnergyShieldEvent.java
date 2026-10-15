package mpsremodularized.common.event;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = MPSConstants.MOD_ID)
public final class EnergyShieldEvent {
    // MPS armor points historically absorb 4% each, with six points available per armor piece.
    private static final double DAMAGE_REDUCTION_PER_ARMOR_POINT = 0.04;
    private static final double MAX_DAMAGE_REDUCTION = 0.96;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };

    private EnergyShieldEvent() {}

    @SubscribeEvent
    public static void absorbDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)
            || player.level().isClientSide
            || event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }

        ShieldProperties shield = getShieldProperties(player);
        if (shield.armorPoints() <= 0) {
            return;
        }

        event.addReductionModifier(DamageContainer.Reduction.ARMOR,
            (container, vanillaReduction) -> applyShield(player, container.getNewDamage(), vanillaReduction, shield));
    }

    static float applyShield(Player player, float incomingDamage, float vanillaReduction, ShieldProperties shield) {
        if (incomingDamage <= 0) {
            return vanillaReduction;
        }

        double protectionRatio = Math.min(
            MAX_DAMAGE_REDUCTION,
            shield.armorPoints() * DAMAGE_REDUCTION_PER_ARMOR_POINT
        );
        double maximumTotalReduction = incomingDamage * MAX_DAMAGE_REDUCTION;
        double availableReduction = Math.max(0, maximumTotalReduction - vanillaReduction);
        double requestedReduction = Math.min(incomingDamage * protectionRatio, availableReduction);
        if (requestedReduction <= 0) {
            return vanillaReduction;
        }

        double absorbedDamage = requestedReduction;
        if (shield.energyPerDamage() > 0 && !player.getAbilities().instabuild) {
            double requestedEnergy = Math.ceil(requestedReduction * shield.energyPerDamage());
            double drainedEnergy = ElectricItemUtils.drainPlayerEnergy(player, requestedEnergy, false);
            absorbedDamage = Math.min(requestedReduction, drainedEnergy / shield.energyPerDamage());
        }

        return (float) Math.min(incomingDamage, vanillaReduction + absorbedDamage);
    }

    private static ShieldProperties getShieldProperties(Player player) {
        double armorPoints = 0;
        double energyPerDamage = 0;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armor = player.getItemBySlot(slot);
            IModularItem modularItem = MPSCapabilities.getModularItem(armor);
            if (modularItem == null) {
                continue;
            }

            ItemStack shieldStack = modularItem.getOnlineModuleOrEmpty(MPSConstants.ENERGY_SHIELD_MODULE);
            IPowerModule shield = modularItem.getModuleCapability(shieldStack);
            if (shield == null) {
                continue;
            }

            armorPoints += Math.max(0, shield.applyPropertyModifiers(MPSConstants.ARMOR_VALUE_ENERGY));
            energyPerDamage += Math.max(0, shield.applyPropertyModifiers(MPSConstants.ARMOR_ENERGY_CONSUMPTION));
        }

        return new ShieldProperties(armorPoints, energyPerDamage);
    }

    record ShieldProperties(double armorPoints, double energyPerDamage) {}
}
