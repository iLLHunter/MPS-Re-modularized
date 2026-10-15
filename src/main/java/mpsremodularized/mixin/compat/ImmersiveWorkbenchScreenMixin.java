package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeToolRequests;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo @Mixin(targets = "blusunrize.immersiveengineering.client.gui.ModWorkbenchScreen", remap = false)
public abstract class ImmersiveWorkbenchScreenMixin {
    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void mps$owned(CompoundTag message, CallbackInfo ci) {
        var binding = (INativeToolMenu)((AbstractContainerScreen<?>)(Object)this).getMenu();
        var owner = binding.mpsremodularized$nativeOwner();
        if (owner == null) return;
        if (owner.valid()) {
            try {
                var packet = (CustomPacketPayload) Class.forName("blusunrize.immersiveengineering.common.network.MessageBlockEntitySync")
                        .getConstructor(BlockPos.class, CompoundTag.class).newInstance(owner.player().blockPosition(), message);
                PacketDistributor.sendToServer(NativeToolRequests.wrap(owner.identity(), packet));
            } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported workbench configuration packet", error); }
        }
        ci.cancel();
    }
}
