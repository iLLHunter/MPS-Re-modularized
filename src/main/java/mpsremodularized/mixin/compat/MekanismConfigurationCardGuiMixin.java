package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeToolIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Let Mekanism's own GUI gate recognize the selected card before opening a machine. */
@Pseudo
@Mixin(targets = "mekanism.common.tile.base.TileEntityMekanism", remap = false)
public abstract class MekanismConfigurationCardGuiMixin {
    @Redirect(method = "openGui", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$configurationCard(Player player) {
        return NativeToolIntegration.selected(player, InteractionHand.MAIN_HAND, "mekanism:configuration_card");
    }
}
