package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets={"me.desht.pneumaticcraft.client.gui.GPSToolScreen","me.desht.pneumaticcraft.client.gui.programmer.ProgWidgetAreaToolScreen","com.hollingsworth.arsnouveau.client.gui.radial_menu.GuiRadialMenu"},remap=false)
public abstract class PneumaticToolScreenOwnerMixin implements mpsremodularized.imixin.common.item.INativeToolScreen {
 @Unique private NativeToolIntegration.Installed mps$owner;
 @Inject(method="<init>",at=@At("RETURN"))
 private void mps$bind(CallbackInfo ci){mps$owner=NativeToolIntegration.current();if(mps$owner==null)mps$owner=mpsremodularized.client.compat.NativeRemainingControls.screenOwner();}
 @Override public NativeToolIntegration.Installed mpsremodularized$screenOwner(){return mps$owner;}
}
