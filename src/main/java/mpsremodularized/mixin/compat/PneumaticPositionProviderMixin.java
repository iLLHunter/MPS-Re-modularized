package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.NativeToolIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="me.desht.pneumaticcraft.client.render.area.AreaRenderManager",remap=false)
public abstract class PneumaticPositionProviderMixin {
 @Inject(method="getHeldPositionProvider",at=@At("HEAD"),cancellable=true)
 private void mps$selected(Player player,CallbackInfoReturnable<ItemStack> ci){var owner=NativeToolIntegration.find(player,InteractionHand.MAIN_HAND,null);if(owner!=null&&java.util.Set.of("pneumaticcraft:gps_tool","pneumaticcraft:gps_area_tool").contains(owner.item().donor.toString()))ci.setReturnValue(owner.source());}
}
