package mpsremodularized.common.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Empty-cursor fallback, leaving native droppers and all other cursor stacks untouched. */
public final class MekanismDropperIntegration {
    private MekanismDropperIntegration() {}
    public static ItemStack cursor(Player player, ItemStack real) {
        if (player==null || !real.isEmpty()) return real;
        var active=NativeToolIntegration.current();
        if (active!=null && active.player()==player && active.item().donor.toString().equals("mekanism:gauge_dropper")) return active.source();
        var owner=NativeToolIntegration.find(player,InteractionHand.MAIN_HAND,"mekanism:gauge_dropper");
        return owner==null ? real : owner.source();
    }
    public static boolean allowed(Player player, Object packet) throws ReflectiveOperationException {
        var menu=player.containerMenu;
        var type=Class.forName("mekanism.common.inventory.container.tile.MekanismTileContainer");
        if (!type.isInstance(menu) || !menu.getCarried().isEmpty() || !menu.stillValid(player)
                || !(boolean)type.getMethod("canPlayerAccess",Player.class).invoke(menu,player)) return false;
        var tile=(net.minecraft.world.level.block.entity.BlockEntity) type.getMethod("getTileEntity").invoke(menu);
        return tile.getBlockPos().equals(packet.getClass().getMethod("pos").invoke(packet));
    }
}
