package mpsremodularized.mixin.common.item;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mpsremodularized.common.item.module.environmental.BlockReachModule;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BucketItem.class)
public abstract class BucketBlockReachMixin extends Item {
    @Shadow @Final public Fluid content;
    protected BucketBlockReachMixin(Properties properties) { super(properties); }

    @WrapMethod(method = "use")
    private InteractionResultHolder<ItemStack> mps$bucket(Level level, Player player, InteractionHand hand,
                                                         Operation<InteractionResultHolder<ItemStack>> original) {
        if (level.isClientSide() || BlockReachModule.online(player) == null) return original.call(level, player, hand);
        var hit = getPlayerPOVHitResult(level, player, content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) return original.call(level, player, hand);
        var payment = BlockReachModule.prepare(player, player.getEyePosition().distanceTo(hit.getLocation()));
        if (payment == null) return InteractionResultHolder.fail(player.getItemInHand(hand));
        boolean successful = false;
        try {
            var result = original.call(level, player, hand);
            successful = result.getResult().consumesAction();
            return result;
        } finally {
            payment.finish(successful);
        }
    }
}
