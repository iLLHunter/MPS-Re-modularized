package mpsremodularized.mixin.common.item;

import mpsremodularized.common.compat.NativeStationIntegration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Extra opening data identifies portable IE menus; ordinary placed stations use their native factory. */
@Mixin(value = MenuType.class, remap = false)
public abstract class NativePortableWorkbenchMenuTypeMixin {
    @Inject(method = "create(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/world/inventory/AbstractContainerMenu;", at = @At("HEAD"), cancellable = true)
    private void mps$portable(int id, Inventory inventory, RegistryFriendlyByteBuf buffer, CallbackInfoReturnable<AbstractContainerMenu> ci) {
        var menu = NativeStationIntegration.clientMenu((MenuType<?>)(Object)this, id, inventory, buffer);
        if (menu == null) menu = mpsremodularized.common.compat.PoweredDiamondAnvilIntegration.clientMenu((MenuType<?>)(Object)this, id, inventory, buffer);
        if (menu != null) ci.setReturnValue(menu);
    }
}
