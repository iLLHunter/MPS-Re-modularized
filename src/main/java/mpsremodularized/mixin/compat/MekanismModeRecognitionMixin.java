package mpsremodularized.mixin.compat;

import mpsremodularized.common.compat.MekanismToolIntegration;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo @Mixin(targets="mekanism.common.item.interfaces.IModeItem", remap=false)
public interface MekanismModeRecognitionMixin {
    @Redirect(method="isModeItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/EquipmentSlot;Z)Z",
            at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack mps$selected(Player player, EquipmentSlot slot) { return MekanismToolIntegration.slot(player, slot); }
}
