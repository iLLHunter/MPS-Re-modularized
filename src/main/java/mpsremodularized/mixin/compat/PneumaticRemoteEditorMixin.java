package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeToolIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The editor saves the contained Remote when its native close action runs. */
@Pseudo
@Mixin(targets = "me.desht.pneumaticcraft.client.gui.remote.RemoteEditorScreen", remap = false)
public abstract class PneumaticRemoteEditorMixin {
    @Redirect(method = "onClose", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$remote(Player player, InteractionHand hand) {
        return NativeToolIntegration.selected(player, hand, "pneumaticcraft:remote");
    }
}
