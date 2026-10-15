package mpsremodularized.common.compat;

import java.util.function.Predicate;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Owned native station inventories; no world block is installed or substituted. */
public final class NativeStationIntegration {
    private static final java.util.Map<BlockEntity, Boolean> PORTABLE_BLOCKS = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    private NativeStationIntegration() {}
    public static boolean supports(String donor) {
        return donor.equals("immersiveengineering:workbench") || donor.equals("immersiveengineering:circuit_table")
                || donor.equals("mysticalagriculture:tinkering_table");
    }
    public static boolean portableBlock(BlockEntity block) { return PORTABLE_BLOCKS.containsKey(block); }
    public static void open(NativeToolIntegration.Installed owner) {
        owner.player().openMenu(new SimpleMenuProvider((id, inv, player) -> create(id, inv, owner), owner.source().getHoverName()),
                buffer -> {
                    if (owner.item().provider == CompatibleMod.MYSTICAL_AGRICULTURE) buffer.writeBlockPos(owner.player().blockPosition());
                    else if (owner.item().provider == CompatibleMod.IMMERSIVE_ENGINEERING) {
                        buffer.writeBlockPos(owner.player().blockPosition());
                        buffer.writeEnum(owner.hand());
                        buffer.writeUtf(owner.identity());
                    }
                });
    }
    public static AbstractContainerMenu create(int id, Inventory inventory, NativeToolIntegration.Installed owner) {
        try {
            var registries = owner.player().registryAccess();
            var data = owner.source().getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
            AbstractContainerMenu menu;
            Runnable save;
            if (owner.item().provider == CompatibleMod.IMMERSIVE_ENGINEERING) {
                boolean circuit = owner.item().donor.toString().equals("immersiveengineering:circuit_table");
                var beType = Class.forName("blusunrize.immersiveengineering.common.blocks.wooden." + (circuit ? "CircuitTableBlockEntity" : "ModWorkbenchBlockEntity"));
                var block = BuiltInRegistries.BLOCK.get(owner.item().donor);
                var be = (BlockEntity) beType.getConstructor(BlockPos.class, net.minecraft.world.level.block.state.BlockState.class)
                        .newInstance(owner.player().blockPosition(), block.defaultBlockState());
                be.setLevel(owner.player().level());
                PORTABLE_BLOCKS.put(be, true);
                beType.getMethod("readCustomNBT", CompoundTag.class, boolean.class, net.minecraft.core.HolderLookup.Provider.class).invoke(be, data, false, registries);
                save = () -> {
                    if (owner.player().level().isClientSide()) return;
                    try {
                        var stored = data.copy();
                        beType.getMethod("writeCustomNBT", CompoundTag.class, boolean.class, net.minecraft.core.HolderLookup.Provider.class).invoke(be, stored, false, registries);
                        // The circuit GUI's energy gauge is a view of the Fist battery, not another rechargeable store.
                        if (circuit && data.contains("energy")) stored.put("energy", data.get("energy").copy());
                        else if (circuit) stored.remove("energy");
                        stored.putString("id", owner.item().donor.toString());
                        owner.source().set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(stored)); owner.save();
                    } catch (ReflectiveOperationException error) { throw new IllegalStateException("Native workbench inventory persistence failed", error); }
                };
                var arg = Class.forName("blusunrize.immersiveengineering.common.register.IEMenuTypes").getField(circuit ? "CIRCUIT_TABLE" : "MOD_WORKBENCH").get(null);
                var type = (MenuType<?>) arg.getClass().getMethod("getType").invoke(arg);
                if (circuit) {
                    var context = Class.forName("blusunrize.immersiveengineering.common.gui.IEContainerMenu$MenuContext");
                    var ctxConstructor = context.getDeclaredConstructor(MenuType.class, int.class, Runnable.class, Predicate.class); ctxConstructor.setAccessible(true);
                    var ctx = ctxConstructor.newInstance(type, id, save, (Predicate<Player>) player -> player == owner.player() && owner.valid());
                    var energyType = Class.forName("blusunrize.immersiveengineering.api.energy.MutableEnergyStorage");
                    var nativeConstructor = Class.forName("blusunrize.immersiveengineering.common.gui.CircuitTableMenu")
                            .getDeclaredConstructor(context, Inventory.class, IItemHandler.class, energyType);
                    nativeConstructor.setAccessible(true);
                    menu = (AbstractContainerMenu) nativeConstructor.newInstance(ctx, inventory,
                            new ItemStackHandler((NonNullList<ItemStack>) beType.getMethod("getInventory").invoke(be)), beType.getField("energyStorage").get(be));
                } else {
                    menu = (AbstractContainerMenu) Class.forName("blusunrize.immersiveengineering.common.gui.ModWorkbenchContainer")
                            .getConstructor(MenuType.class, int.class, Inventory.class, beType).newInstance(type, id, inventory, be);
                    // The native menu otherwise marks a placed block as changed. Persist this owned inventory instead.
                    var changed = Class.forName("blusunrize.immersiveengineering.common.gui.IEContainerMenu").getDeclaredField("setChanged");
                    changed.setAccessible(true); changed.set(menu, save);
                }
            } else {
                var beType = Class.forName("com.blakebr0.mysticalagriculture.tileentity.TinkeringTableTileEntity");
                var handler = (ItemStackHandler) beType.getMethod("createInventoryHandler").invoke(null);
                if (data.contains("Items")) handler.deserializeNBT(registries, data);
                save = () -> { var stored = data.copy(); stored.merge(handler.serializeNBT(registries)); stored.putString("id", "mysticalagriculture:tinkering_table");
                    owner.source().set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(stored)); owner.save(); };
                menu = (AbstractContainerMenu) Class.forName("com.blakebr0.mysticalagriculture.container.TinkeringTableContainer")
                        .getMethod("create", int.class, Inventory.class, IItemHandlerModifiable.class, BlockPos.class)
                        .invoke(null, id, inventory, handler, owner.player().blockPosition());
            }
            ((INativeToolMenu) menu).mpsremodularized$bindNativeOwner(owner, save);
            refreshCircuitEnergy(menu);
            PortableStationIntegration.protectHost(menu, owner);
            return menu;
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported native station API", error); }
    }

    public static AbstractContainerMenu clientMenu(MenuType<?> type, int id, Inventory inventory, net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        var key = BuiltInRegistries.MENU.getKey(type);
        if (key == null || !java.util.Set.of("immersiveengineering:workbench", "immersiveengineering:circuittable").contains(key.toString())
                || buffer == null || buffer.readableBytes() <= Long.BYTES) return null;
        buffer.readBlockPos();
        var hand = buffer.readEnum(net.minecraft.world.InteractionHand.class);
        var identity = buffer.readUtf();
        var owner = NativeToolIntegration.find(inventory.player, hand, key.getPath().equals("workbench") ? "immersiveengineering:workbench" : "immersiveengineering:circuit_table");
        if (owner == null || !owner.identity().equals(identity) || !owner.valid()) throw new IllegalStateException("Native workbench opening lost its installed owner");
        return create(id, inventory, owner);
    }

    public static NativeToolIntegration.Installed circuitOwner(Object menu) {
        var owner = ((INativeToolMenu) menu).mpsremodularized$nativeOwner();
        return owner != null && owner.item().donor.toString().equals("immersiveengineering:circuit_table") ? owner : null;
    }
    public static void refreshCircuitEnergy(Object menu) {
        var owner = circuitOwner(menu);
        if (owner == null || owner.player().level().isClientSide()) return;
        try {
            var storage = menu.getClass().getField("energyStorage").get(menu);
            storage.getClass().getMethod("setStoredEnergy", int.class).invoke(storage,
                    owner.valid() ? Math.min(32000, mpsremodularized.common.utils.ElectricItemUtils.getItemEnergy(owner.fist())) : 0);
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported circuit workbench energy API", error); }
    }
    public static void refreshCircuitOutput(Object menu) {
        var owner = circuitOwner(menu);
        if (owner == null || owner.player().level().isClientSide()) return;
        ((AbstractContainerMenu)menu).slotsChanged(null);
    }
    public static boolean configureWorkbench(NativeToolIntegration.Installed owner, Object packet) throws ReflectiveOperationException {
        var menu = owner.player().containerMenu;
        if (!(menu instanceof INativeToolMenu nativeMenu) || !nativeMenu.mpsremodularized$nativeOwnerValid()
                || nativeMenu.mpsremodularized$nativeOwner() == null
                || !owner.identity().equals(nativeMenu.mpsremodularized$nativeOwner().identity())
                || !menu.getClass().getName().equals("blusunrize.immersiveengineering.common.gui.ModWorkbenchContainer")) return false;
        var tag = (CompoundTag) packet.getClass().getMethod("nbt").invoke(packet);
        Class.forName("blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchBlockEntity")
                .getMethod("applyConfigTo", ItemStack.class, CompoundTag.class).invoke(null, menu.getSlot(0).getItem(), tag);
        nativeMenu.mpsremodularized$saveNativeSource();
        menu.broadcastChanges();
        return true;
    }
}
