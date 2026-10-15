package mpsremodularized.common.compat;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import mpsremodularized.common.item.electric.tool.PowerFist;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

/** Only the selected main-hand Configurator exposes native abilities; native buffers stay on donors. */
public final class MekanismToolIntegration {
    private static final Map<ItemStack, WeakReference<Player>> OWNERS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<ItemStack, Long> LAST_TICK = new WeakHashMap<>();
    private MekanismToolIntegration() {}

    public static void remember(ItemStack fist, Player player) {
        if (fist.getItem() instanceof PowerFist) OWNERS.put(fist, new WeakReference<>(player));
    }
    public static ItemStack configurator(ItemStack fist) {
        var reference = OWNERS.get(fist); var player = reference == null ? null : reference.get();
        if (player == null || player.getMainHandItem() != fist) return ItemStack.EMPTY;
        var installed = NativeToolIntegration.find(player, InteractionHand.MAIN_HAND, "mekanism:configurator");
        return installed == null ? ItemStack.EMPTY : installed.source();
    }
    public static ItemStack slot(Player player, EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            var installed = NativeToolIntegration.find(player, InteractionHand.MAIN_HAND, "mekanism:configurator");
            if (installed != null) return installed.source();
        }
        return player.getItemBySlot(slot);
    }
    public static ItemStack mainHand(Player player) { return slot(player, EquipmentSlot.MAINHAND); }
    public static boolean canAccess(Player player, ItemStack source) {
        try {
            var api=Class.forName("mekanism.api.security.IItemSecurityUtils");
            return (boolean)api.getMethod("canAccess",Player.class,Object.class).invoke(api.getField("INSTANCE").get(null),player,source);
        } catch(ReflectiveOperationException error) {throw new IllegalStateException("Unsupported Mekanism item security API",error);}
    }

    public static void tick(ItemStack fist, Level level, Entity owner) {
        if (!(owner instanceof Player player)) return;
        remember(fist, player);
        if (level.isClientSide()) return;
        boolean carried = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) carried |= player.getInventory().getItem(slot) == fist;
        if (!carried || Long.valueOf(level.getGameTime()).equals(LAST_TICK.put(fist, level.getGameTime()))) return;
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        var supply = fist.getCapability(Capabilities.EnergyStorage.ITEM);
        if (host == null || supply == null) return;
        for (int index = 0; index < host.getSlots(); index++) {
            var module = host.getStackInSlot(index);
            if (!(module.getItem() instanceof NativeModuleItem item) || item.provider != CompatibleMod.MEKANISM
                    || item.kind != NativeModuleItem.Kind.TOOL || !host.isModuleOperational(host.getModuleCapability(module))) continue;
            var source = item.source(module, player.registryAccess());
            if (item.donor.toString().equals("mekanism:portable_qio_dashboard")) {
                var selected = NativeToolIntegration.find(player, InteractionHand.MAIN_HAND, item.donor.toString());
                if (selected != null && selected.index() == index) source = selected.source();
                var before = source.copy();
                source.getItem().inventoryTick(source, level, player, index, false);
                if (!ItemStack.isSameItemSameComponents(before, source)) {
                    var updated = module.copy(); item.store(updated, source, player.registryAccess()); host.updateModuleInSlot(index, updated);
                }
            }
            var buffer = source.getCapability(Capabilities.EnergyStorage.ITEM);
            if (buffer == null || ElectricItemUtils.transferEnergy(supply, buffer, Integer.MAX_VALUE) <= 0) continue;
            var updated = module.copy(); item.store(updated, source, player.registryAccess()); host.updateModuleInSlot(index, updated);
        }
    }
}
