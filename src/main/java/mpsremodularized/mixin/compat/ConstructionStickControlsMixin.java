package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.ConstructionStickIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo @Mixin(targets = "mrbysco.constructionstick.client.KeybindHandler", remap = false)
public abstract class ConstructionStickControlsMixin {
    @Redirect(method = "onClientTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$source(Player player, InteractionHand hand) {
        var owner = ConstructionStickIntegration.selected(player);
        return owner == null ? player.getItemInHand(hand) : owner.source();
    }
}
