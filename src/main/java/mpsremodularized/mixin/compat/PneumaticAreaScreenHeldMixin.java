package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="me.desht.pneumaticcraft.client.gui.GPSAreaToolScreen",remap=false)
public abstract class PneumaticAreaScreenHeldMixin {
 @Redirect(method={"init","changed","toggleP1P2"},at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
 private ItemStack mps$held(Player player,InteractionHand hand){var owner=((mpsremodularized.imixin.common.item.INativeToolScreen)this).mpsremodularized$screenOwner();return owner==null?player.getItemInHand(hand):owner.source();}
}
