package mpsremodularized.common.compat;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSDataComponents;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** The real Cyclic machine, with an owned buffer/inventory and its native repair tick. */
@net.neoforged.fml.common.EventBusSubscriber(modid = "mpsremodularized")
public final class PoweredDiamondAnvilIntegration {
    private static final Map<ItemStack, Map<String, State>> CACHE = new WeakHashMap<>();
    private static final Map<BlockEntity, Boolean> BLOCKS = new WeakHashMap<>();
    private static final ThreadLocal<BlockEntity> OPEN = new ThreadLocal<>();
    private PoweredDiamondAnvilIntegration() {}
    public static boolean portable(BlockEntity tile) { return BLOCKS.containsKey(tile); }
    public static BlockEntity openingTile(net.minecraft.world.level.Level level, BlockPos position) {
        return OPEN.get() == null ? level.getBlockEntity(position) : OPEN.get();
    }
    public static IEnergyStorage energy(BlockEntity tile) {
        try { return (IEnergyStorage) tile.getClass().getMethod("getEnergyHandler", net.minecraft.core.Direction.class).invoke(tile, new Object[] {null}); }
        catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported Cyclic energy API", error); }
    }
    public static void setEnergy(BlockEntity tile, int amount) {
        try { energy(tile).getClass().getMethod("setEnergy", int.class).invoke(energy(tile), amount); }
        catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported Cyclic energy synchronization", error); }
    }
    public static final class State {
        private final WeakReference<ItemStack> fist;
        private WeakReference<Player> player;
        private final int index;
        private final NativeModuleItem item;
        private final String identity;
        public final BlockEntity tile;
        private CompoundTag savedPayload;
        private long lastTick = Long.MIN_VALUE;
        private State(ItemStack fist, Player player, int index, NativeModuleItem item, String identity, ItemStack source) {
            this.fist = new WeakReference<>(fist); this.player = new WeakReference<>(player);
            this.index = index; this.item = item; this.identity = identity;
            savedPayload = MPSCapabilities.getModeChangingModularItem(fist).getStackInSlot(index).get(MPSDataComponents.NATIVE_ITEM).copy();
            try {
                tile = (BlockEntity) Class.forName("com.lothrazar.cyclic.block.anvil.TileAnvilAuto")
                        .getConstructor(BlockPos.class, net.minecraft.world.level.block.state.BlockState.class)
                        .newInstance(player.blockPosition(), BuiltInRegistries.BLOCK.get(item.donor).defaultBlockState());
                tile.setLevel(player.level()); BLOCKS.put(tile, true);
                tile.loadCustomOnly(source.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag(), player.registryAccess());
                tile.applyComponentsFromItemStack(source);
                tile.getClass().getMethod("setNeedsRedstone", int.class).invoke(tile, 0);
            } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported Cyclic anvil API", error); }
        }
        public boolean owned() {
            var stack = fist.get(); var carrier = player.get();
            if (stack == null || carrier == null) return false;
            boolean carried = false;
            for (int i = 0; i < carrier.getInventory().getContainerSize(); i++) carried |= carrier.getInventory().getItem(i) == stack;
            var host = MPSCapabilities.getModeChangingModularItem(stack);
            if (!carried || host == null || index >= host.getSlots()) return false;
            var module = host.getStackInSlot(index);
            return module.is(item) && Objects.equals(identity, module.get(MPSDataComponents.NATIVE_ID))
                    && savedPayload.equals(module.get(MPSDataComponents.NATIVE_ITEM)) && host.isModuleOperational(host.getModuleCapability(module));
        }
        public void save() {
            var carrier = player.get();
            if (carrier == null || carrier.level().isClientSide() || !owned()) return;
            var host = MPSCapabilities.getModeChangingModularItem(fist.get());
            var module = host.getStackInSlot(index).copy(); var source = item.source(module, carrier.registryAccess());
            var data = source.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
            data.merge(tile.saveWithoutMetadata(carrier.registryAccess())); data.putString("id", "cyclic:anvil");
            source.applyComponents(tile.collectComponents()); source.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(data));
            item.store(module, source, carrier.registryAccess());
            var changed = module.get(MPSDataComponents.NATIVE_ITEM);
            if (!savedPayload.equals(changed)) { savedPayload = changed.copy(); host.updateModuleInSlot(index, module); }
        }
        private void tick(Player carrier) {
            player = new WeakReference<>(carrier); tile.setLevel(carrier.level());
            if (!owned() || lastTick == carrier.level().getGameTime()) return;
            lastTick = carrier.level().getGameTime();
            var buffer = energy(tile);
            int accepted = buffer.receiveEnergy((int)Math.min(Integer.MAX_VALUE, ElectricItemUtils.getPlayerEnergy(carrier)), true);
            int withdrawn = (int)ElectricItemUtils.drainPlayerEnergy(carrier, accepted, false);
            buffer.receiveEnergy(withdrawn, false);
            try { tile.getClass().getMethod("tick").invoke(tile); }
            catch (ReflectiveOperationException error) { throw new IllegalStateException("Native powered anvil tick failed", error); }
            finally { save(); }
        }
    }
    public static State get(ItemStack fist, Player player, int index) {
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (host == null || index >= host.getSlots()) return null;
        var module = host.getStackInSlot(index);
        if (!(module.getItem() instanceof NativeModuleItem item) || !item.donor.toString().equals("cyclic:anvil")
                || !host.isModuleOperational(host.getModuleCapability(module))) return null;
        var source = item.source(module, player.registryAccess()); var identity = module.get(MPSDataComponents.NATIVE_ID);
        if (source.isEmpty() || identity == null) return null;
        var states = CACHE.computeIfAbsent(fist, unused -> new HashMap<>());
        states.entrySet().removeIf(entry -> !entry.getValue().owned());
        return states.computeIfAbsent(identity, unused -> new State(fist, player, index, item, identity, source));
    }
    public static void tick(ItemStack fist, net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (host == null) return;
        for (int index = 0; index < host.getSlots(); index++) {
            var module = host.getStackInSlot(index);
            if (module.getItem() instanceof NativeModuleItem item && item.donor.toString().equals("cyclic:anvil")) {
                var state = get(fist, player, index); if (state != null) state.tick(player);
            }
        }
    }
    public static void open(NativeToolIntegration.Installed owner) {
        owner.player().openMenu(new SimpleMenuProvider((id, inv, player) -> menu(id, inv, owner), owner.source().getHoverName()),
                buffer -> { buffer.writeBlockPos(owner.player().blockPosition()); buffer.writeUtf(owner.identity()); });
    }
    public static AbstractContainerMenu menu(int id, Inventory inventory, NativeToolIntegration.Installed owner) {
        var state = get(owner.fist(), owner.player(), owner.index());
        if (state == null || OPEN.get() != null) throw new IllegalStateException("Missing or nested powered anvil owner");
        OPEN.set(state.tile);
        try {
            var menu = (AbstractContainerMenu) Class.forName("com.lothrazar.cyclic.block.anvil.ContainerAnvil")
                    .getConstructor(int.class, net.minecraft.world.level.Level.class, BlockPos.class, Inventory.class, Player.class)
                    .newInstance(id, owner.player().level(), state.tile.getBlockPos(), inventory, owner.player());
            ((INativeToolMenu) menu).mpsremodularized$bindNativeOwner(owner, state::save);
            PortableStationIntegration.protectHost(menu, owner); return menu;
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported Cyclic anvil menu", error); }
        finally { OPEN.remove(); }
    }
    public static AbstractContainerMenu clientMenu(MenuType<?> type, int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        var key = BuiltInRegistries.MENU.getKey(type);
        if (key == null || !key.toString().equals("cyclic:anvil") || buffer == null || buffer.readableBytes() <= Long.BYTES) return null;
        buffer.readBlockPos(); var identity = buffer.readUtf();
        var owner = NativeToolIntegration.find(inventory.player, net.minecraft.world.InteractionHand.MAIN_HAND, "cyclic:anvil");
        if (owner == null || !identity.equals(owner.identity())) throw new IllegalStateException("Portable powered anvil lost its owner");
        return menu(id, inventory, owner);
    }
    @net.neoforged.bus.api.SubscribeEvent public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) { CACHE.clear(); BLOCKS.clear(); }
}
