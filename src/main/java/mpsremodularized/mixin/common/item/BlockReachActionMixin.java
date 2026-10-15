package mpsremodularized.mixin.common.item;

import java.util.ArrayDeque;
import java.util.Deque;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mpsremodularized.common.item.module.environmental.BlockReachModule;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class BlockReachActionMixin {
    @Shadow @Final protected ServerPlayer player;
    @Unique private final Deque<BlockReachModule.Payment> mps$reachPayments = new ArrayDeque<>();

    @WrapMethod(method = "destroyBlock")
    private boolean mps$break(BlockPos pos, Operation<Boolean> original) {
        var payment = BlockReachModule.prepare(player, BlockReachModule.blockDistance(player, pos));
        if (payment == null) return false;
        mps$reachPayments.push(payment);
        try {
            return original.call(pos);
        } finally {
            mps$reachPayments.pop();
            payment.finish(payment.blockRemoved);
        }
    }

    @Inject(method = "removeBlock", at = @At("RETURN"))
    private void mps$removed(BlockPos pos, BlockState state, boolean harvest, CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue() && !mps$reachPayments.isEmpty()) mps$reachPayments.peek().blockRemoved = true;
    }

    @WrapMethod(method = "useItemOn")
    private InteractionResult mps$use(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand,
                                      BlockHitResult hit, Operation<InteractionResult> original) {
        var payment = BlockReachModule.prepare(player, player.getEyePosition().distanceTo(hit.getLocation()));
        if (payment == null) return InteractionResult.FAIL;
        boolean successful = false;
        try {
            var result = original.call(player, level, stack, hand, hit);
            successful = result.consumesAction();
            return result;
        } finally {
            payment.finish(successful);
        }
    }
}
