package mpsremodularized.common.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class PortableStationIntegration {
    private static final ThreadLocal<NativeToolIntegration.Installed> ANVIL = new ThreadLocal<>();
    private PortableStationIntegration() {}

    public static void protectHost(AbstractContainerMenu menu, NativeToolIntegration.Installed owner) {
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot original = menu.slots.get(i);
            if (original.getItem() != owner.fist() && (original.container != owner.player().getInventory() || original.getContainerSlot() != owner.player().getInventory().selected)) continue;
            var locked = new Slot(original.container, original.getContainerSlot(), original.x, original.y) {
                @Override public ItemStack getItem() { return original.getItem(); }
                @Override public void set(ItemStack stack) { original.set(stack); }
                @Override public void setChanged() { original.setChanged(); }
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
            };
            locked.index = original.index;
            menu.slots.set(i, locked);
        }
    }
    public static boolean canClick(AbstractContainerMenu menu, int slot, int button, net.minecraft.world.inventory.ClickType type) {
        var binding = (mpsremodularized.imixin.common.item.INativeToolMenu) menu;
        if (!binding.mpsremodularized$nativeOwnerValid()) return false;
        var owner = binding.mpsremodularized$nativeOwner();
        return owner == null || (type != net.minecraft.world.inventory.ClickType.SWAP || button != owner.player().getInventory().selected)
                && (slot < 0 || slot >= menu.slots.size() || menu.slots.get(slot).getItem() != owner.fist());
    }

    public static void takeAnvil(NativeToolIntegration.Installed owner, Runnable nativeTake) {
        if (!owner.valid()) return;
        if (ANVIL.get() != null) throw new IllegalStateException("Nested portable anvil action");
        ANVIL.set(owner);
        try { nativeTake.run(); }
        finally { ANVIL.remove(); }
    }

    public static BlockState anvilState(Level level, BlockPos pos) {
        var owner = ANVIL.get();
        if (owner == null) return level.getBlockState(pos);
        var source = owner.item().source(owner.host().getStackInSlot(owner.index()), level.registryAccess());
        return source.is(Items.CHIPPED_ANVIL) ? Blocks.CHIPPED_ANVIL.defaultBlockState()
                : source.is(Items.DAMAGED_ANVIL) ? Blocks.DAMAGED_ANVIL.defaultBlockState() : Blocks.ANVIL.defaultBlockState();
    }

    public static boolean damageAnvil(Level level, BlockPos pos, BlockState state, int flags) {
        var owner = ANVIL.get();
        if (owner == null) return level.setBlock(pos, state, flags);
        return owner.valid();
    }

    public static boolean breakAnvil(Level level, BlockPos pos, boolean moving) {
        var owner = ANVIL.get();
        if (owner == null) return level.removeBlock(pos, moving);
        return owner.valid();
    }
}
