package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Pseudo @Mixin(targets="mekanism.common.network.to_server.PacketDropperUse",remap=false)
public abstract class MekanismDropperPacketMixin {
    @Redirect(method="handle",at=@At(value="INVOKE",target="Lnet/minecraft/world/inventory/AbstractContainerMenu;getCarried()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$source(AbstractContainerMenu menu) {
        var owner=NativeToolIntegration.current();return owner==null?menu.getCarried():MekanismDropperIntegration.cursor(owner.player(),menu.getCarried());
    }
}
