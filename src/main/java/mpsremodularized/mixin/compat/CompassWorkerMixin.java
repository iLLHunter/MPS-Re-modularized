package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeCompassIntegration;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets={"com.chaosthedude.naturescompass.util.BiomeSearchWorker", "com.chaosthedude.explorerscompass.worker.StructureSearchWorker"}, remap=false)
public abstract class CompassWorkerMixin {
    @Shadow(remap=false) private ItemStack stack;
    @Inject(method="hasWork", at=@At("HEAD"), cancellable=true)
    private void mps$owner(CallbackInfoReturnable<Boolean> ci) { if (!NativeCompassIntegration.jobValid(stack)) ci.setReturnValue(false); }
}
