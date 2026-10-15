package mpsremodularized.common.compat;

import java.util.Objects;
import java.util.function.Supplier;
import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSDataComponents;
import mpsremodularized.imixin.common.item.IUseOnContextMixn;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** Local source locators; the player's real held stack is never substituted. */
public final class NativeToolIntegration {
    public record Installed(Player player, InteractionHand hand, ItemStack fist, IModeChangingItem host,
                            int index, NativeModuleItem item, String identity, ItemStack source) {
        @Override public ItemStack fist() {
            return synchronizedFist(fist, player.getItemInHand(hand), player.level().isClientSide());
        }
        public boolean valid() {
            var held = fist();
            return player.getItemInHand(hand) == held && matches(held);
        }
        private boolean matches(ItemStack held) {
            // Mode wrappers cache their selected index; reacquire after every external mode change.
            var liveHost = MPSCapabilities.getModeChangingModularItem(held);
            if (liveHost == null || index >= liveHost.getSlots()) return false;
            var current = liveHost.getStackInSlot(index);
            return liveHost.getActiveMode() == index && current.is(item)
                    && Objects.equals(identity, current.get(MPSDataComponents.NATIVE_ID))
                    && !current.getOrDefault(MPSDataComponents.ANVIL_BROKEN, false)
                    && liveHost.isModuleOperational(liveHost.getModuleCapability(current));
        }
        public void save() {
            if (!valid() || player.level().isClientSide()) return;
            var current = host.getStackInSlot(index).copy();
            item.store(current, source);
            host.updateModuleInSlot(index, current);
        }
    }
    private static final ThreadLocal<Installed> USE = new ThreadLocal<>();
    private static final ThreadLocal<ItemStack> REPLACEMENT = new ThreadLocal<>();
    private NativeToolIntegration() {}

    /** Client inventory packets replace stack objects; server actions keep their exact original host. */
    private static ItemStack synchronizedFist(ItemStack original, ItemStack held, boolean client) {
        return client && held.is(original.getItem()) ? held : original;
    }

    public static Installed find(Player player, InteractionHand hand, String donor) {
        if (player == null || hand != InteractionHand.MAIN_HAND) return null;
        var boundAction = USE.get();
        if (boundAction != null && boundAction.player == player && boundAction.hand == hand
                && (donor == null || boundAction.item.donor.toString().equals(donor)) && boundAction.valid()) return boundAction;
        var fist = player.getItemInHand(hand);
        MekanismToolIntegration.remember(fist, player);
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (host == null) return null;
        var module = host.getActiveModule();
        if (!(module.getItem() instanceof NativeModuleItem item) || (item.kind != NativeModuleItem.Kind.TOOL && item.kind != NativeModuleItem.Kind.STATION && item.kind != NativeModuleItem.Kind.CONSTRUCTION_STICK)
                || (donor != null && !item.donor.toString().equals(donor))
                || !host.isModuleOperational(host.getModuleCapability(module))) return null;
        var source = item.source(module, player.registryAccess());
        var identity = module.get(MPSDataComponents.NATIVE_ID);
        if (player.containerMenu instanceof mpsremodularized.imixin.common.item.INativeToolMenu menu) {
            var bound = menu.mpsremodularized$nativeOwner();
            if (bound != null && bound.fist() == fist && bound.item() == item && Objects.equals(identity, bound.identity()) && bound.valid()) return bound;
        }
        return source.isEmpty() || !item.acceptsSource(source) || identity == null ? null
                : new Installed(player, hand, fist, host, host.getActiveMode(), item, identity, source);
    }

    public static ItemStack held(Player player, InteractionHand hand) {
        var installed = USE.get();
        return installed != null && installed.player == player && installed.hand == hand
                ? installed.source : player.getItemInHand(hand);
    }

    public static ItemStack selected(Player player, InteractionHand hand, String donor) {
        var current = USE.get();
        if (current != null && current.player == player && current.hand == hand
                && current.item.donor.toString().equals(donor)) return current.source;
        var installed = find(player, hand, donor);
        return installed == null ? player.getItemInHand(hand) : installed.source;
    }

    /** Render caches may retain an older synchronized copy of this same owned module. */
    public static ItemStack renderedSource(Player player, InteractionHand hand, ItemStack rendered, String donor) {
        var installed = find(player, hand, donor);
        if (installed == null || !rendered.is(installed.fist().getItem()) || !installed.matches(rendered)) return rendered;
        NativeCompassIntegration.bindRendered(installed);
        return installed.source();
    }

    /** Only the native Smart Wrench swaps its contained item variant. */
    public static void replaceWrench(Player player, InteractionHand hand, ItemStack replacement) {
        var owner = USE.get();
        if (owner == null || owner.player != player || owner.hand != hand
                || !owner.item.donor.toString().equals("rftoolsbase:smartwrench")) {
            player.setItemInHand(hand, replacement);
            return;
        }
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(replacement.getItem()).toString();
        if (!id.equals("rftoolsbase:smartwrench") && !id.equals("rftoolsbase:smartwrench_select"))
            throw new IllegalArgumentException("Invalid native Smart Wrench replacement");
        replacement.applyComponents(owner.source.getComponentsPatch());
        REPLACEMENT.set(replacement);
    }

    public static <T> T use(Installed installed, Supplier<T> action) {
        if (USE.get() != null) throw new IllegalStateException("Nested native tool action");
        USE.set(installed);
        NativeCompassIntegration.bind(installed);
        var previousMenu = installed.player.containerMenu;
        try {
            T result = action.get();
            if (installed.player.containerMenu != previousMenu
                    && java.util.Set.of("pneumaticcraft:remote", "ae2:network_tool", "mffs:remote_controller").contains(installed.item.donor.toString())) {
                var menu = installed.player.containerMenu;
                ((mpsremodularized.imixin.common.item.INativeToolMenu) menu).mpsremodularized$bindNativeOwner(installed, installed::save);
                PortableStationIntegration.protectHost(menu, installed);
            }
            return result;
        }
        finally {
            var replacement = REPLACEMENT.get();
            REPLACEMENT.remove(); USE.remove(); NativeCompassIntegration.save(installed.source());
            if (replacement == null) installed.save();
            else new Installed(installed.player, installed.hand, installed.fist, installed.host, installed.index,
                    installed.item, installed.identity, replacement).save();
        }
    }

    public static Installed current() { return USE.get(); }
    public static void tick(ItemStack fist, net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity, int slot) {
        if (!(entity instanceof Player player) || !carried(player, fist)) return;
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (host == null) return;
        var donors = java.util.Set.of("pneumaticcraft:manometer", "pneumaticcraft:remote", "pneumaticcraft:gps_tool",
                "pneumaticcraft:gps_area_tool", "ars_additions:advanced_dominion_wand");
        for (int index = 0; index < host.getSlots(); index++) {
            var module = host.getStackInSlot(index);
            if (!(module.getItem() instanceof NativeModuleItem item) || !donors.contains(item.donor.toString())
                    || !host.isModuleOperational(host.getModuleCapability(module))) continue;
            boolean selected = player.getMainHandItem() == fist && host.getActiveMode() == index;
            var owner = selected ? find(player, InteractionHand.MAIN_HAND, item.donor.toString()) : null;
            var source = owner == null ? item.source(module, player.registryAccess()) : owner.source();
            if (source.isEmpty()) continue;
            var identity = module.get(MPSDataComponents.NATIVE_ID);
            source.getItem().inventoryTick(source, level, entity, slot, selected);
            var live = host.getStackInSlot(index);
            if (!level.isClientSide && live.is(item) && Objects.equals(identity, live.get(MPSDataComponents.NATIVE_ID))
                    && host.isModuleOperational(host.getModuleCapability(live)) && carried(player, fist)) {
                var changed = live.copy(); item.store(changed, source, player.registryAccess()); host.updateModuleInSlot(index, changed);
            }
        }
    }
    private static boolean carried(Player player, ItemStack fist) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot) == fist) return true;
        }
        return false;
    }
    public static ItemStack slot(Player player, net.minecraft.world.entity.EquipmentSlot slot) {
        return slot == net.minecraft.world.entity.EquipmentSlot.MAINHAND ? held(player, InteractionHand.MAIN_HAND) : player.getItemBySlot(slot);
    }

    public static UseOnContext context(Installed installed, UseOnContext original) {
        return new UseOnContext(original.getLevel(), original.getPlayer(), original.getHand(), installed.source,
                ((IUseOnContextMixn) original).mpsremodularized$getBlockHitResult());
    }
}
