package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.CompatibleMod;
import mpsremodularized.common.compat.NativeArmorIntegration;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Pseudo @Mixin(targets="com.breakinblocks.auroral.events.ShimmerweaveEventHandler", remap=false)
public abstract class ShimmerweaveArmorMixin {
    @Redirect(method={"handleGoggles", "handleTunic", "handleLeggings", "handleSkates", "onLivingFall"},
            at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack mps$armor(Player player, EquipmentSlot slot) { return NativeArmorIntegration.auroralEquipment(player, slot); }

    @Redirect(method="handleLeggings",
            slice=@Slice(from=@At(value="FIELD", target="Lnet/minecraft/world/level/block/Blocks;SOUL_SAND:Lnet/minecraft/world/level/block/Block;"),
                    to=@At(value="FIELD", target="Lnet/minecraft/world/level/block/Blocks;SOUL_SOIL:Lnet/minecraft/world/level/block/Block;")),
            at=@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private static boolean mps$soulSand(BlockState below, Block block, Player player) {
        // Soul sand's 7/8-height surface puts rounded feet inside it; Auroral reads one block too low.
        // Correct only this predicate for an active MPS module, retaining all other native terrain rules.
        return below.is(block) || NativeArmorIntegration.find(player.getItemBySlot(EquipmentSlot.LEGS),
                EquipmentSlot.LEGS, CompatibleMod.AURORAL) != null
                && player.level().getBlockState(player.getBlockPosBelowThatAffectsMyMovement()).is(Blocks.SOUL_SAND);
    }

    @Redirect(method={"handleTunic", "handleSkates"},
            at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"))
    private static void mps$wear(ItemStack source, int damage, LivingEntity entity, EquipmentSlot slot) {
        NativeArmorIntegration.auroralWear(source, damage, entity, slot);
    }
}
