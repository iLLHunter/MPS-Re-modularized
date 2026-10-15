package mpsremodularized.mixin.compat;

import com.llamalad7.mixinextras.sugar.Local;
import mpsremodularized.common.compat.NativeCompassIntegration;
import mpsremodularized.common.compat.NativeToolIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets={"com.chaosthedude.naturescompass.items.NaturesCompassItem", "com.chaosthedude.explorerscompass.items.ExplorersCompassItem"}, remap=false)
public abstract class CompassItemMixin {
    @Redirect(method="use", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$source(Player player, InteractionHand hand) { return NativeToolIntegration.held(player, hand); }
    @Redirect(method="use", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;isCrouching()Z"))
    private boolean mps$openBranch(Player player) { return NativeToolIntegration.current() == null && player.isCrouching(); }
    @Inject(method="isBroken", at=@At("HEAD"), cancellable=true)
    private void mps$noBreak(ItemStack source, CallbackInfoReturnable<Boolean> ci) { if (NativeCompassIntegration.bound(source)) ci.setReturnValue(false); }
    @Inject(method="damageCompass", at=@At("HEAD"), cancellable=true)
    private void mps$noWear(ItemStack source, CallbackInfo ci) { if (NativeCompassIntegration.bound(source)) ci.cancel(); }
    @Inject(method={"succeed", "fail", "setSearching", "setFound", "setNotFound", "setInactive", "setState", "setSearchRadius", "setSamples", "setDisplayCoordinates"}, at=@At("RETURN"))
    private void mps$save(CallbackInfo ci, @Local(argsOnly=true) ItemStack source) { NativeCompassIntegration.save(source); }
}
