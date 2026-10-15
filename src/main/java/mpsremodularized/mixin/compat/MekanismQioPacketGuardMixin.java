package mpsremodularized.mixin.compat;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets={"mekanism.common.network.to_server.qio.PacketQIOItemViewerSlotTake","mekanism.common.network.to_server.qio.PacketQIOItemViewerSlotShiftTake","mekanism.common.network.to_server.qio.PacketQIOItemViewerSlotPlace","mekanism.common.network.to_server.qio.PacketQIOFillCraftingWindow","mekanism.common.network.to_server.qio.PacketQIOClearCraftingWindow","mekanism.common.network.to_server.PacketItemGuiInteract","mekanism.common.network.to_server.button.PacketItemButtonPress","mekanism.common.network.to_server.frequency.PacketSetItemFrequency","mekanism.common.network.to_server.frequency.PacketSetFrequencyColor"},remap=false)
public abstract class MekanismQioPacketGuardMixin {
    @Inject(method="handle",at=@At("HEAD"),cancellable=true)
    private void mps$guard(IPayloadContext context,CallbackInfo ci) { if(context.player().containerMenu instanceof INativeToolMenu menu&&!menu.mpsremodularized$nativeOwnerValid())ci.cancel(); }
    @Inject(method="handle",at=@At("RETURN"))
    private void mps$save(IPayloadContext context,CallbackInfo ci) { if(context.player().containerMenu instanceof INativeToolMenu menu)menu.mpsremodularized$saveNativeSource(); }
}
