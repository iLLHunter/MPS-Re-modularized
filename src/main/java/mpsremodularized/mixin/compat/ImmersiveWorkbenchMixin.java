package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.PortableStationIntegration;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets = "blusunrize.immersiveengineering.common.gui.ModWorkbenchContainer", remap = false)
public abstract class ImmersiveWorkbenchMixin {
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void mps$move(Player player, int slot, CallbackInfoReturnable<ItemStack> ci) {
        if (!PortableStationIntegration.canClick((AbstractContainerMenu)(Object)this, slot, 0, ClickType.QUICK_MOVE)) ci.setReturnValue(ItemStack.EMPTY);
    }
    @Inject(method = "quickMoveStack", at = @At("RETURN"))
    private void mps$save(CallbackInfoReturnable<ItemStack> ci) { ((INativeToolMenu)this).mpsremodularized$saveNativeSource(); }
    @Inject(method = "rebindSlots", at = @At("RETURN"))
    private void mps$dynamicSlots(CallbackInfo ci) {
        var binding = (INativeToolMenu)this;
        if (binding.mpsremodularized$nativeOwner() != null) {
            PortableStationIntegration.protectHost((AbstractContainerMenu)(Object)this, binding.mpsremodularized$nativeOwner());
            binding.mpsremodularized$saveNativeSource();
        }
    }
    @WrapWithCondition(method = "clicked", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/common/blocks/wooden/ModWorkbenchBlockEntity;markContainingBlockForUpdate(Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private boolean mps$worldUpdate(@Coerce Object block, net.minecraft.world.level.block.state.BlockState state) {
        return ((INativeToolMenu)this).mpsremodularized$nativeOwner() == null;
    }
}
