package mpsremodularized.common.compat;

import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

public final class RemainingNativePolicies {
    private RemainingNativePolicies() {}
    public static boolean allowed(NativeToolIntegration.Installed owner, CustomPacketPayload packet) throws ReflectiveOperationException {
        var type = packet.getClass();
        if (packet.type().id().getNamespace().equals("mffs")) {
            var menu = owner.player().containerMenu;
            if (!(menu instanceof INativeToolMenu bound) || bound.mpsremodularized$nativeOwner() == null
                    || !bound.mpsremodularized$nativeOwnerValid() || !menu.stillValid(owner.player())
                    || !Class.forName("dev.su5ed.mffs.menu.FortronMenu").isInstance(menu)) return false;
            var block = (net.minecraft.world.level.block.entity.BlockEntity) menu.getClass().getField("blockEntity").get(menu);
            if (block.getLevel() != owner.player().level() || !block.getLevel().isLoaded(block.getBlockPos())) return false;
            try { if (!block.getBlockPos().equals(type.getMethod("pos").invoke(packet))) return false; }
            catch (NoSuchMethodException ignored) { /* Native filter-slot requests target the owning menu. */ }
        }
        try { if (type.getMethod("hand").invoke(packet) != InteractionHand.MAIN_HAND) return false; }
        catch (NoSuchMethodException ignored) { /* Native radial and left-click requests have no hand field. */ }
        if (packet.type().id().getPath().equals("change_gps_tool_coord")) {
            int index = (int) type.getMethod("index").invoke(packet);
            if (index < 0 || index > (owner.item().donor.getPath().equals("gps_area_tool") ? 1 : 0)) return false;
        }
        if (packet.type().id().getPath().equals("update_remote_layout")) {
            var menu = owner.player().containerMenu;
            if (!(menu instanceof INativeToolMenu owned) || owned.mpsremodularized$nativeOwner() == null
                    || !owned.mpsremodularized$nativeOwnerValid()
                    || !BuiltInRegistries.MENU.getKey(menu.getType()).toString().equals("pneumaticcraft:remote_editor")) return false;
            var permission = owner.source().getItem().getClass().getDeclaredMethod("isAllowedToEdit", net.minecraft.world.entity.player.Player.class, net.minecraft.world.item.ItemStack.class);
            permission.setAccessible(true);
            if (!(boolean) permission.invoke(owner.source().getItem(), owner.player(), owner.source())) return false;
        }
        return true;
    }
}
