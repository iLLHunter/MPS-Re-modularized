package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="mekanism.common.network.PacketUtils",remap=false)
public abstract class MekanismNativeGuiRequestsMixin {
    @Inject(method="sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)Z",at=@At("HEAD"),cancellable=true)
    private static void mps$request(CustomPacketPayload packet,CallbackInfoReturnable<Boolean> ci) {
        var player=Minecraft.getInstance().player;if(player==null)return;
        NativeToolIntegration.Installed owner=null;
        if(packet.type().id().toString().equals("mekanism:use_dropper")&&player.containerMenu.getCarried().isEmpty())owner=NativeToolIntegration.find(player,InteractionHand.MAIN_HAND,"mekanism:gauge_dropper");
        else if(NativeToolRequests.supportsQio(packet)&&player.containerMenu instanceof INativeToolMenu menu)owner=menu.mpsremodularized$nativeOwner();
        if(owner!=null) {if(owner.valid())PacketDistributor.sendToServer(NativeToolRequests.wrap(owner.identity(),packet));ci.setReturnValue(true);}
    }
}
