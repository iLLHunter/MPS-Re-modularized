package mpsremodularized.mixin.compat;

import java.util.function.Supplier;
import mpsremodularized.common.compat.PaxelIntegration;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Preserve the native AOE offset packet, its bounds and its source-stack mutation. */
@Pseudo
@Mixin(targets = "com.blakebr0.mysticalagriculture.network.payloads.UpdateAOEAugmentOffsetPayload", remap = false)
public abstract class MysticalPaxelOffsetMixin {
    @Redirect(method = "lambda$handle$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack mps$source(Player player) { return PaxelIntegration.mysticalAoeTool(player); }

    @Redirect(method = "lambda$handle$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Ljava/util/function/Supplier;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <T> T mps$saveOffset(ItemStack source, Supplier<DataComponentType<T>> type, T value,
            IPayloadContext context, @Coerce Object payload) {
        T previous = source.set(type.get(), value);
        PaxelIntegration.saveMysticalAoeTool(context.player(), source);
        return previous;
    }
}
