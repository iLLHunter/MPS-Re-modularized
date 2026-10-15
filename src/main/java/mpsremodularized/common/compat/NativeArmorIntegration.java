package mpsremodularized.common.compat;

import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.item.electric.armor.AbstractElectricArmor;
import mpsremodularized.common.registration.MPSCapabilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/** Native armor benefits without replacing equipment or importing physical defenses. */
@EventBusSubscriber(modid="mpsremodularized")
public final class NativeArmorIntegration {
    public record Installed(ItemStack armor, IModularItem host, int index, NativeModuleItem item, ItemStack source) {
        public void save(net.minecraft.core.HolderLookup.Provider registries) {
            var module = host.getStackInSlot(index).copy();
            item.store(module, source, registries);
            host.updateModuleInSlot(index, module);
        }
    }
    private NativeArmorIntegration() {}

    public static Installed find(ItemStack armor, EquipmentSlot slot, CompatibleMod provider) {
        if (!(armor.getItem() instanceof AbstractElectricArmor item) || item.getEquipmentSlot() != slot) return null;
        var host = MPSCapabilities.getModularItem(armor);
        if (host == null) return null;
        for (int i = 0; i < host.getSlots(); i++) {
            var module = host.getStackInSlot(i);
            if (!(module.getItem() instanceof NativeModuleItem nativeItem) || nativeItem.provider != provider
                    || NativeModuleItem.armorSlot(nativeItem.kind) != slot
                    || !host.isModuleOperational(host.getModuleCapability(module))) continue;
            var source = nativeItem.source(module);
            if (!source.isEmpty()) return new Installed(armor, host, i, nativeItem, source);
        }
        return null;
    }

    /** Only Auroral's local reads call this; global equipment remains untouched. */
    public static ItemStack auroralEquipment(LivingEntity entity, EquipmentSlot slot) {
        var real = entity.getItemBySlot(slot);
        var installed = find(real, slot, CompatibleMod.AURORAL);
        return installed == null ? real : installed.source();
    }

    public static void auroralWear(ItemStack source, int damage, LivingEntity entity, EquipmentSlot slot) {
        var installed = find(entity.getItemBySlot(slot), slot, CompatibleMod.AURORAL);
        if (installed == null || !ItemStack.isSameItemSameComponents(source, installed.source())) {
            source.hurtAndBreak(damage, entity, slot);
            return;
        }
        if (entity.level() instanceof ServerLevel level) {
            // Retain native enchantment/creative wear rules. The break callback cannot clear MPS equipment.
            source.hurtAndBreak(damage, level, entity instanceof ServerPlayer player ? player : null,
                    broken -> entity.onEquippedItemBroken(broken, slot));
            new Installed(installed.armor(), installed.host(), installed.index(), installed.item(), source).save(level.registryAccess());
        }
    }

    @SubscribeEvent public static void attributes(ItemAttributeModifierEvent event) {
        if (!(event.getItemStack().getItem() instanceof AbstractElectricArmor armor)) return;
        var slot = armor.getEquipmentSlot();
        for (var provider : java.util.List.of(CompatibleMod.IRONS_SPELLBOOKS, CompatibleMod.FORBIDDEN_ARCANUS, CompatibleMod.MALUM)) {
            var installed = find(event.getItemStack(), slot, provider);
            if (installed == null) continue;
            for (var entry : installed.source().getAttributeModifiers().modifiers()) {
                var key = BuiltInRegistries.ATTRIBUTE.getKey(entry.attribute().value());
                if (key != null && selectedAttribute(provider, key) && entry.slot().test(slot)) {
                    event.replaceModifier(entry.attribute(), entry.modifier(), entry.slot());
                }
            }
            if (provider == CompatibleMod.IRONS_SPELLBOOKS) {
                var resistance = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("irons_spellbooks:spell_resist")).orElseThrow();
                event.addModifier(resistance, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("mpsremodularized",
                        "battlemage_resistance_" + slot.getName()), 0.25, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(slot));
            }
        }
    }

    private static boolean selectedAttribute(CompatibleMod provider, ResourceLocation attribute) {
        String id = attribute.toString();
        return switch (provider) {
            case IRONS_SPELLBOOKS -> java.util.Set.of("irons_spellbooks:max_mana", "irons_spellbooks:spell_power", "irons_spellbooks:spell_resist").contains(id);
            case FORBIDDEN_ARCANUS -> id.equals("forbidden_arcanus:generic.aureal_regeneration");
            case MALUM -> java.util.Set.of("malum:soul_ward_capacity", "malum:soul_ward_recovery_rate", "malum:malignant_conversion", "lodestone:magic_resistance").contains(id);
            default -> false;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        if (!net.neoforged.fml.ModList.get().isLoaded("mekanism")) return;
        try {
            var capability = (net.neoforged.neoforge.capabilities.ItemCapability) Class.forName("mekanism.common.capabilities.Capabilities").getField("RADIATION_SHIELDING").get(null);
            var api = Class.forName("mekanism.api.radiation.capability.IRadiationShielding");
            net.minecraft.world.level.ItemLike[] armor = armorItems();
            event.registerItem(capability, (stack, context) -> java.lang.reflect.Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[]{api}, (proxy, method, args) -> {
                if (method.getName().equals("getRadiationShielding")) {
                    var piece = (AbstractElectricArmor) stack.getItem();
                    var installed = find(stack, piece.getEquipmentSlot(), CompatibleMod.MEKANISM);
                    var nativeShield = installed == null ? null : installed.source().getCapability(capability);
                    return nativeShield == null ? 0D : method.invoke(nativeShield, args);
                }
                return switch (method.getName()) {
                    case "toString" -> "MPS native Hazmat shielding";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                };
            }), armor);
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported Mekanism radiation shielding API", error); }
    }

    private static net.minecraft.world.level.ItemLike[] armorItems() {
        return BuiltInRegistries.ITEM.stream().filter(item -> item instanceof AbstractElectricArmor).toArray(net.minecraft.world.level.ItemLike[]::new);
    }
}
