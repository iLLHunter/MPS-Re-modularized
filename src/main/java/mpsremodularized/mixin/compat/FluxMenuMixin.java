package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeToolIntegration;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "sonar.fluxnetworks.common.connection.FluxMenu", remap = false)
public abstract class FluxMenuMixin implements INativeToolMenu {
    @Unique private NativeToolIntegration.Installed mps$owner;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void mps$bind(int id, Inventory inventory, @Coerce Object provider, CallbackInfo ci) {
        if (!provider.getClass().getName().equals("sonar.fluxnetworks.common.item.ItemFluxConfigurator$Provider")) return;
        var installed = NativeToolIntegration.find(inventory.player, InteractionHand.MAIN_HAND, "fluxnetworks:flux_configurator");
        if (installed == null) return;
        try {
            var source = (ItemStack) provider.getClass().getField("mStack").get(provider);
            if (ItemStack.isSameItemSameComponents(source, installed.source())) {
                mps$owner = new NativeToolIntegration.Installed(installed.player(), installed.hand(), installed.fist(),
                        installed.host(), installed.index(), installed.item(), installed.identity(), source);
            }
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException("Unsupported Flux menu ownership API", exception); }
    }
    @Override public NativeToolIntegration.Installed mpsremodularized$nativeOwner() { return mps$owner; }
    @Override public boolean mpsremodularized$nativeOwnerValid() { return mps$owner == null || mps$owner.valid(); }
    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void mps$valid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (mps$owner != null) cir.setReturnValue(player == mps$owner.player() && mps$owner.valid());
    }
}
