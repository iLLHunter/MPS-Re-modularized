package mpsremodularized.common.compat;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A native search keeps its exact donor after deselection, never a replacement module. */
public final class NativeCompassIntegration {
    private static final Map<ItemStack, Owner> SOURCES = Collections.synchronizedMap(new WeakHashMap<>());
    private NativeCompassIntegration() {}
    public static boolean isCompass(ResourceLocation donor) { return donor.getNamespace().equals("naturescompass") || donor.getNamespace().equals("explorerscompass"); }
    public static final class Owner {
        private final WeakReference<ItemStack> fist;
        private final WeakReference<Player> player;
        private final Item fistItem;
        private final int index;
        private final NativeModuleItem item;
        public final String identity;
        private CompoundTag saved;
        private Owner(NativeToolIntegration.Installed installed) {
            fist = new WeakReference<>(installed.fist()); player = new WeakReference<>(installed.player());
            fistItem = installed.fist().getItem();
            index = installed.index(); item = installed.item(); identity = installed.identity();
            saved = installed.host().getStackInSlot(index).get(MPSDataComponents.NATIVE_ITEM).copy();
        }
        public boolean valid(boolean selected) {
            var carrier = player.get(); var held = fist.get();
            // Only client request ownership follows synchronized held copies. Server workers
            // must still write to the exact original carried host, never a replacement.
            if (carrier != null && carrier.level().isClientSide() && carrier.getMainHandItem().is(fistItem)) held = carrier.getMainHandItem();
            if (carrier == null || held == null || !carrier.isAlive() || carrier.isRemoved()) return false;
            boolean carried = false;
            for (int slot = 0; slot < carrier.getInventory().getContainerSize(); slot++) carried |= carrier.getInventory().getItem(slot) == held;
            if (!carried) return false;
            var host = MPSCapabilities.getModeChangingModularItem(held);
            if (host == null || index >= host.getSlots()) return false;
            var module = host.getStackInSlot(index);
            return module.is(item) && Objects.equals(identity, module.get(MPSDataComponents.NATIVE_ID))
                    && (carrier.level().isClientSide() || saved.equals(module.get(MPSDataComponents.NATIVE_ITEM))) && host.isModuleOperational(host.getModuleCapability(module))
                    && (!selected || carrier.getMainHandItem() == held && host.getActiveMode() == index);
        }
        private void save(ItemStack source) {
            var carrier = player.get();
            if (carrier == null || carrier.level().isClientSide() || !valid(false)) return;
            var host = MPSCapabilities.getModeChangingModularItem(fist.get()); var module = host.getStackInSlot(index).copy();
            item.store(module, source, carrier.registryAccess()); saved = module.get(MPSDataComponents.NATIVE_ITEM).copy();
            host.updateModuleInSlot(index, module);
        }
    }
    public static Owner owner(ItemStack source) { return SOURCES.get(source); }
    public static void bind(NativeToolIntegration.Installed installed) {
        if (isCompass(installed.item().donor)) SOURCES.put(installed.source(), new Owner(installed));
    }
    /** Identify an ephemeral module render stack without changing any donor components. */
    public static void bindRendered(NativeToolIntegration.Installed installed) {
        SOURCES.put(installed.source(), new Owner(installed));
    }
    public static void save(ItemStack source) { var owner = owner(source); if (owner != null) owner.save(source); }
    public static boolean jobValid(ItemStack source) { var owner = owner(source); return owner == null || owner.valid(false); }
    public static boolean bound(ItemStack source) { return owner(source) != null; }
    public static ItemStack held(Player player, Item item) {
        var installed = NativeToolIntegration.current();
        if (installed != null && installed.player() == player && installed.source().is(item)) return installed.source();
        installed = NativeToolIntegration.find(player, InteractionHand.MAIN_HAND, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString());
        if (installed == null || !isCompass(installed.item().donor)) return ItemStack.EMPTY;
        bind(installed); return installed.source();
    }
}
