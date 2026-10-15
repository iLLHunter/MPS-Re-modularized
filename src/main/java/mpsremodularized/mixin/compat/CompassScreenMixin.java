package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeToolRequests;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo @Mixin(targets={"com.chaosthedude.naturescompass.gui.NaturesCompassScreen", "com.chaosthedude.explorerscompass.gui.ExplorersCompassScreen"}, remap=false)
public abstract class CompassScreenMixin {
    @Shadow(remap=false) private ItemStack stack;
    @Redirect(method={"searchForBiome", "searchForStructure", "searchForGroup", "searchForNext", "teleport"},
            at=@At(value="INVOKE", target="Lnet/neoforged/neoforge/network/PacketDistributor;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;[Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"))
    private void mps$request(CustomPacketPayload packet, CustomPacketPayload[] extra) { NativeToolRequests.sendCompass(stack, packet, extra); }
}
