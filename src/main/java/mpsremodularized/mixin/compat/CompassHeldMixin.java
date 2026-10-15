package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.NativeCompassIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo @Mixin(targets={"com.chaosthedude.naturescompass.util.ItemUtils", "com.chaosthedude.explorerscompass.util.ItemUtils"}, remap=false)
public abstract class CompassHeldMixin {
    @Inject(method="getHeldItem", at=@At("HEAD"), cancellable=true)
    private static void mps$held(Player player, Item item, CallbackInfoReturnable<ItemStack> ci) {
        var source = NativeCompassIntegration.held(player, item);
        if (!source.isEmpty()) ci.setReturnValue(source);
    }
}
