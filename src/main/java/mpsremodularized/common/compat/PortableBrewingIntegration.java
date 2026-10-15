package mpsremodularized.common.compat;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Native brewing state belongs to the sealed donor; cached containers hold no strong host/player reference. */
@EventBusSubscriber(modid = "mpsremodularized")
public final class PortableBrewingIntegration {
    private static final Map<ItemStack, Map<String, Brew>> CACHE = new WeakHashMap<>();
    private static final ThreadLocal<Brew> TICK = new ThreadLocal<>();
    private PortableBrewingIntegration() {}

    public static final class Brew extends BrewingStandBlockEntity {
        private final WeakReference<ItemStack> fist;
        private WeakReference<Player> player;
        private final int index;
        private final NativeModuleItem item;
        private final String identity;
        private CompoundTag savedPayload;
        private boolean ready;
        private long lastTick = Long.MIN_VALUE;

        private Brew(ItemStack fist, Player player, int index, NativeModuleItem item, String identity, ItemStack source) {
            super(BlockPos.ZERO, Blocks.BREWING_STAND.defaultBlockState());
            this.fist = new WeakReference<>(fist); this.player = new WeakReference<>(player);
            this.index = index; this.item = item; this.identity = identity;
            savedPayload = MPSCapabilities.getModeChangingModularItem(fist).getStackInSlot(index).get(MPSDataComponents.NATIVE_ITEM).copy();
            setLevel(player.level());
            var data = source.get(DataComponents.BLOCK_ENTITY_DATA);
            if (data != null) loadCustomOnly(data.copyTag(), player.registryAccess());
            applyComponentsFromItemStack(source);
            ready = true;
        }
        public ContainerData data() { return dataAccess; }
        private static void nativeChanged(Level level, BlockPos pos, BlockState state) { setChanged(level, pos, state); }
        private boolean owned() {
            var hostStack = fist.get(); var carrier = player.get();
            if (hostStack == null || carrier == null || carrier.level().isClientSide()) return false;
            boolean carried = false;
            for (int slot = 0; slot < carrier.getInventory().getContainerSize(); slot++) carried |= carrier.getInventory().getItem(slot) == hostStack;
            if (!carried) return false;
            var host = MPSCapabilities.getModeChangingModularItem(hostStack);
            if (host == null || index >= host.getSlots()) return false;
            var module = host.getStackInSlot(index);
            return module.is(item) && Objects.equals(identity, module.get(MPSDataComponents.NATIVE_ID))
                    && savedPayload.equals(module.get(MPSDataComponents.NATIVE_ITEM))
                    && host.isModuleOperational(host.getModuleCapability(module));
        }
        @Override public boolean stillValid(Player player) { return player == this.player.get() && owned(); }
        @Override public void setChanged() { if (ready) save(); }
        public void save() {
            if (!owned()) return;
            var carrier = player.get(); var host = MPSCapabilities.getModeChangingModularItem(fist.get());
            var module = host.getStackInSlot(index).copy(); var source = item.source(module, carrier.registryAccess());
            if (source.isEmpty()) return;
            CompoundTag data = source.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
            data.merge(saveWithoutMetadata(carrier.registryAccess()));
            data.putString("id", "minecraft:brewing_stand");
            // Native block items may also carry inventory/custom-name components. Keep them current.
            source.applyComponents(collectComponents());
            source.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(data));
            item.store(module, source, carrier.registryAccess());
            savedPayload = module.get(MPSDataComponents.NATIVE_ITEM).copy();
            host.updateModuleInSlot(index, module);
        }
        private void tick(Player carrier) {
            player = new WeakReference<>(carrier); setLevel(carrier.level());
            if (!owned() || lastTick == carrier.level().getGameTime()) return;
            lastTick = carrier.level().getGameTime();
            if (TICK.get() != null) throw new IllegalStateException("Nested portable brewing tick");
            TICK.set(this);
            try { BrewingStandBlockEntity.serverTick(carrier.level(), carrier.blockPosition(), getBlockState(), this); }
            finally { TICK.remove(); save(); }
        }
    }

    public static Brew get(ItemStack fist, Player player, int index) {
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (host == null || index >= host.getSlots()) return null;
        var module = host.getStackInSlot(index);
        if (!(module.getItem() instanceof NativeModuleItem item) || !item.donor.toString().equals("minecraft:brewing_stand")
                || !host.isModuleOperational(host.getModuleCapability(module))) return null;
        var source = item.source(module, player.registryAccess()); var identity = module.get(MPSDataComponents.NATIVE_ID);
        if (source.isEmpty() || identity == null) return null;
        var states = CACHE.computeIfAbsent(fist, unused -> new HashMap<>());
        // Module removal/replacement cannot reuse a retired container.
        states.entrySet().removeIf(entry -> !entry.getValue().owned());
        return states.computeIfAbsent(identity, unused -> new Brew(fist, player, index, item, identity, source));
    }

    public static void tick(ItemStack fist, Level level, Entity owner) {
        if (level.isClientSide() || !(owner instanceof Player player)) return;
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (host == null) return;
        for (int index = 0; index < host.getSlots(); index++) {
            var module = host.getStackInSlot(index);
            if (module.getItem() instanceof NativeModuleItem item && item.donor.toString().equals("minecraft:brewing_stand")) {
                var brew = get(fist, player, index);
                if (brew != null) brew.tick(player);
            }
        }
    }
    public static void markChanged(Level level, BlockPos pos, BlockState state) {
        if (TICK.get() == null) Brew.nativeChanged(level, pos, state);
    }
    public static boolean updateBottles(Level level, BlockPos pos, BlockState state, int flags) {
        return TICK.get() != null || level.setBlock(pos, state, flags);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { CACHE.clear(); }
}
