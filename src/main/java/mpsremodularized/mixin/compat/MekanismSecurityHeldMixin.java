package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.NativeToolIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Pseudo @Mixin(targets="mekanism.common.lib.security.ItemSecurityUtils",remap=false)
public abstract class MekanismSecurityHeldMixin {
    @Redirect(method="claimOrOpenGui",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack mps$source(Player player,InteractionHand hand) { return NativeToolIntegration.held(player,hand); }
}
