package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.ConstructionStickIntegration;
import mpsremodularized.common.compat.NativeToolIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets = "mrbysco.constructionstick.items.stick.ItemStick", remap = false)
public abstract class ConstructionStickHeldMixin {
    @Redirect(method = {"use", "useOn"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$held(Player player, InteractionHand hand) { return NativeToolIntegration.held(player, hand); }
}
