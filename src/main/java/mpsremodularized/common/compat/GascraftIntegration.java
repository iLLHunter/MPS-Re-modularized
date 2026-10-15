package mpsremodularized.common.compat;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSDataComponents;
import mpsremodularized.common.utils.ElectricItemUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;

/** Foreign event types are resolved only with Gascraft present; standalone MPS has no linkage to it. */
public final class GascraftIntegration {
    private record Shot(Player player, InteractionHand hand, ItemStack source) {}
    private static final ThreadLocal<Shot> SHOT = new ThreadLocal<>();
    private static final Map<ItemStack, Long> LAST_TICK = new WeakHashMap<>();
    private static boolean launcherReady;
    private GascraftIntegration() {}

    public static void adapterLoaded() { launcherReady = true; }
    public static boolean launcherAdapterReady() { return launcherReady; }
    public static boolean embedded(Player player) { return SHOT.get() != null && SHOT.get().player == player; }
    public static ItemStack held(Player player, InteractionHand hand) {
        var shot = SHOT.get();
        return shot != null && shot.player == player && shot.hand == hand ? shot.source : player.getItemInHand(hand);
    }
    public static InteractionResultHolder<ItemStack> fire(ItemStack donor, Level level, Player player, InteractionHand hand) {
        if (SHOT.get() != null) throw new IllegalStateException("Nested Gascraft launch");
        SHOT.set(new Shot(player, hand, donor));
        try { return donor.getItem().use(level, player, hand); }
        finally { SHOT.remove(); }
    }

    /** Refill the native receive-only buffer once per carried Fist per server tick. */
    public static void tick(ItemStack fist, Level level, net.minecraft.world.entity.Entity owner) {
        if (level.isClientSide() || !(owner instanceof Player player)) return;
        boolean carried = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) carried |= player.getInventory().getItem(slot) == fist;
        if (!carried || Long.valueOf(level.getGameTime()).equals(LAST_TICK.put(fist, level.getGameTime()))) return;
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (host == null) return;
        for (int index = 0; index < host.getSlots(); index++) {
            var module = host.getStackInSlot(index);
            if (!(module.getItem() instanceof NativeModuleItem item) || item.kind != NativeModuleItem.Kind.GRENADE_LAUNCHER
                    || !host.isModuleOperational(host.getModuleCapability(module))) continue;
            var source = item.source(module, level.registryAccess());
            var buffer = source.getCapability(Capabilities.EnergyStorage.ITEM);
            if (buffer == null) continue;
            int accepted = buffer.receiveEnergy((int)Math.min(Integer.MAX_VALUE, ElectricItemUtils.getPlayerEnergy(player)), true);
            if (accepted <= 0) continue;
            int withdrawn = (int)ElectricItemUtils.drainPlayerEnergy(player, accepted, false);
            if (buffer.receiveEnergy(withdrawn, false) > 0) save(fist, index, module, source, level);
        }
    }

    /** Merge donor changes into the live host, retaining separately updated batteries. */
    public static void save(ItemStack fist, int index, ItemStack original, ItemStack source, Level level) {
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (host == null || index < 0 || index >= host.getSlots()) return;
        var module = host.getStackInSlot(index);
        if (!(module.getItem() instanceof NativeModuleItem item) || !module.is(original.getItem())
                || !Objects.equals(module.get(MPSDataComponents.NATIVE_ID), original.get(MPSDataComponents.NATIVE_ID))
                || !Objects.equals(module.get(MPSDataComponents.NATIVE_ITEM), original.get(MPSDataComponents.NATIVE_ITEM))) return;
        var updated = module.copy();
        item.store(updated, source, level.registryAccess());
        if (!ItemStack.isSameItemSameComponents(module, updated)) host.updateModuleInSlot(index, updated);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        if (!ModList.get().isLoaded("gascraft")) return;
        try {
            Class<? extends Event> type = (Class<? extends Event>) Class.forName("gascraft.api.event.GasExposureEvent$GasExposureImmunityEvent");
            Method entity = type.getMethod("getEntity"), immune = type.getMethod("setImmune", boolean.class);
            NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, (Class) type, (Event event) -> {
                try {
                    LivingEntity wearer = (LivingEntity) entity.invoke(event);
                    var helmet = MPSCapabilities.getModularItem(wearer.getItemBySlot(EquipmentSlot.HEAD));
                    if (helmet == null) return;
                    for (int i = 0; i < helmet.getSlots(); i++) {
                        ItemStack module = helmet.getStackInSlot(i);
                        if (module.getItem() instanceof NativeModuleItem item && item.kind == NativeModuleItem.Kind.GAS_MASK
                                && !item.source(module).isEmpty() && helmet.isModuleOperational(helmet.getModuleCapability(module))) {
                            immune.invoke(event, true);
                            return;
                        }
                    }
                } catch (ReflectiveOperationException exception) { throw new IllegalStateException("Gascraft immunity API changed", exception); }
            });
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException("Unsupported Gascraft immunity API", exception); }
    }
}
