package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeToolIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "sonar.fluxnetworks.register.RegistryMenuTypes", remap = false)
public abstract class FluxMenuFactoryMixin {
    @Redirect(method = "lambda$register$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack mps$source(Player player) {
        var installed = NativeToolIntegration.find(player, InteractionHand.MAIN_HAND, "fluxnetworks:flux_configurator");
        return installed == null ? player.getMainHandItem() : installed.source();
    }
}
