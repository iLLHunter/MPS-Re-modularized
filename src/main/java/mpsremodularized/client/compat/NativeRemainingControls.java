package mpsremodularized.client.compat;

import mpsremodularized.common.compat.NativeToolIntegration;
import mpsremodularized.common.compat.NativeToolRequests;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import mpsremodularized.imixin.common.item.INativeToolScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class NativeRemainingControls {
    private NativeRemainingControls() {}
    public static NativeToolIntegration.Installed screenOwner() {
        var screen = Minecraft.getInstance().screen;
        if (screen instanceof INativeToolScreen nativeScreen) return nativeScreen.mpsremodularized$screenOwner();
        if (screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> container
                && container.getMenu() instanceof INativeToolMenu menu) return menu.mpsremodularized$nativeOwner();
        return null;
    }
    public static boolean send(CustomPacketPayload payload) {
        if (!NativeToolRequests.supportsRemaining(payload)) return false;
        var owner = NativeToolIntegration.current();
        if (owner == null) owner = screenOwner();
        if (owner == null) owner = NativeToolIntegration.find(Minecraft.getInstance().player, InteractionHand.MAIN_HAND, null);
        if (owner == null || !NativeToolRequests.supportsRemaining(owner, payload)) return false;
        NativeToolRequests.sendMode(owner, payload);
        return true;
    }
    public static ItemStack programmerSource(ItemStack cursor) {
        if (!cursor.isEmpty()) return cursor;
        var owner = NativeToolIntegration.find(Minecraft.getInstance().player, InteractionHand.MAIN_HAND, null);
        return owner != null && (owner.item().donor.toString().equals("pneumaticcraft:gps_tool")
                || owner.item().donor.toString().equals("pneumaticcraft:gps_area_tool")) ? owner.source() : cursor;
    }
}
