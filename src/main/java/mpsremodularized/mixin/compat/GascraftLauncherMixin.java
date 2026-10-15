package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.GascraftIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Redirects only the inspected Gascraft launch transaction. The player's real held item never changes. */
@Pseudo
@Mixin(targets = "gascraft.item.GrenadeLauncherItem", remap = false)
public abstract class GascraftLauncherMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void mps$loaded(CallbackInfo ci) { GascraftIntegration.adapterLoaded(); }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$source(Player player, InteractionHand hand) { return GascraftIntegration.held(player, hand); }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isShiftKeyDown()Z"))
    private boolean mps$nativeMenu(Player player) { return !GascraftIntegration.embedded(player) && player.isShiftKeyDown(); }
}
