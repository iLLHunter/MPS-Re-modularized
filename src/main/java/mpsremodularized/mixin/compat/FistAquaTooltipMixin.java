package mpsremodularized.mixin.compat;

import com.google.common.collect.Multimap;
import mpsremodularized.common.item.electric.tool.PowerFist;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Display the Fist's Aqua Affinity under its actual use slot, without changing attributes. */
@Mixin(value = AttributeUtil.class, remap = false)
public abstract class FistAquaTooltipMixin {
    @Inject(method = "getSortedModifiers", at = @At("RETURN"))
    private static void mps$heldAquaAffinity(ItemStack stack, EquipmentSlotGroup slot,
            CallbackInfoReturnable<Multimap<Holder<Attribute>, AttributeModifier>> cir) {
        if (!(stack.getItem() instanceof PowerFist)) return;
        var modifiers = cir.getReturnValue();
        if (slot == EquipmentSlotGroup.HEAD) {
            modifiers.entries().removeIf(entry -> entry.getKey().equals(Attributes.SUBMERGED_MINING_SPEED)
                    && isAquaAffinity(entry.getValue()));
        } else if (slot == EquipmentSlotGroup.MAINHAND) {
            stack.forEachModifier(EquipmentSlotGroup.HEAD, (attribute, modifier) -> {
                if (attribute.equals(Attributes.SUBMERGED_MINING_SPEED)
                        && isAquaAffinity(modifier)
                        && !modifiers.containsEntry(attribute, modifier)) modifiers.put(attribute, modifier);
            });
        }
    }

    private static boolean isAquaAffinity(AttributeModifier modifier) {
        return modifier.id().getNamespace().equals("minecraft")
                && modifier.id().getPath().startsWith("enchantment.aqua_affinity/");
    }
}
