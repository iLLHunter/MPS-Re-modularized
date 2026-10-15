package mpsremodularized.common.compat;

import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;

/** Charge the actual augmented donor and retain the native placement, modifiers and controls. */
public final class ConstructionStickIntegration {
    private ConstructionStickIntegration() {}
    @SuppressWarnings("unchecked") public static DataComponentType<Boolean> batteryComponent() {
        return (DataComponentType<Boolean>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.parse("constructionstick:battery_enabled"));
    }
    public static boolean hasBattery(ItemStack source) {
        var component = batteryComponent();
        return component != null && source.getOrDefault(component, false);
    }
    public static NativeToolIntegration.Installed selected(Player player) {
        var owner = NativeToolIntegration.find(player, InteractionHand.MAIN_HAND, null);
        return owner != null && owner.item().kind == NativeModuleItem.Kind.CONSTRUCTION_STICK ? owner : null;
    }
    public static void tick(ItemStack fist, net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity, int slot) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (host == null) return;
        for (int index = 0; index < host.getSlots(); index++) {
            var module = host.getStackInSlot(index);
            if (!(module.getItem() instanceof NativeModuleItem item) || item.kind != NativeModuleItem.Kind.CONSTRUCTION_STICK
                    || !host.isModuleOperational(host.getModuleCapability(module))) continue;
            var source = item.source(module, level.registryAccess());
            if (source.isEmpty() || !hasBattery(source)) continue;
            var from = fist.getCapability(Capabilities.EnergyStorage.ITEM);
            var to = source.getCapability(Capabilities.EnergyStorage.ITEM);
            if (from != null && to != null) ElectricItemUtils.transferEnergy(from, to, Integer.MAX_VALUE);
            source.getItem().inventoryTick(source, level, entity, slot, player.getMainHandItem() == fist && host.getActiveMode() == index);
            var updated = module.copy(); item.store(updated, source, level.registryAccess());
            // Charging updates the Fist battery through a separate inventory wrapper. Merge into that live inventory.
            if (!ItemStack.isSameItemSameComponents(module, updated)) MPSCapabilities.getModeChangingModularItem(fist).updateModuleInSlot(index, updated);
        }
    }
}
