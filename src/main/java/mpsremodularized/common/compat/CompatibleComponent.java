package mpsremodularized.common.compat;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import static mpsremodularized.common.compat.CompatibleMod.*;

/** Verified Stage 1 candidates. Tier choices follow the supplied research sheet. */
public enum CompatibleComponent {
    CIRCUIT_1("circuit_1", MPSItems.CONTROL_CIRCUIT_1, false,
            from(MEKANISM, "basic_control_circuit"), from(AE2, "printed_calculation_processor"),
            from(MODERN_INDUSTRIALIZATION, "analog_circuit"), from(ORITECH, "processing_unit")),
    CIRCUIT_2("circuit_2", MPSItems.CONTROL_CIRCUIT_2, false,
            from(MEKANISM, "advanced_control_circuit"), from(AE2, "printed_logic_processor"),
            from(MODERN_INDUSTRIALIZATION, "electronic_circuit"), from(ORITECH, "advanced_computing_engine")),
    CIRCUIT_3("circuit_3", MPSItems.CONTROL_CIRCUIT_3, false,
            from(MEKANISM, "elite_control_circuit"), from(AE2, "printed_engineering_processor"),
            from(MODERN_INDUSTRIALIZATION, "digital_circuit"), from(ORITECH, "super_ai_chip")),
    CIRCUIT_4("circuit_4", MPSItems.CONTROL_CIRCUIT_4, false,
            from(MEKANISM, "ultimate_control_circuit"), from(MODERN_INDUSTRIALIZATION, "quantum_circuit"),
            from(ORITECH, "heisenberg_compensator")),
    CAPACITOR_1("capacitor_1", MPSItems.CAPACITOR_1, false,
            from(MODERN_INDUSTRIALIZATION, "capacitor"), from(PNEUMATICCRAFT, "capacitor"),
            from(ENDER_IO_EVOLUTION, "capacitor_silver"), from(ENDER_IO, "basic_capacitor"), from(POWAH, "ender_gate_hardened")),
    CAPACITOR_2("capacitor_2", MPSItems.CAPACITOR_2, false,
            from(ENDER_IO_EVOLUTION, "capacitor_energetic_silver"), from(ENDER_IO, "double_layer_capacitor"), from(POWAH, "ender_gate_blazing")),
    CAPACITOR_3("capacitor_3", MPSItems.CAPACITOR_3, false,
            from(ENDER_IO_EVOLUTION, "capacitor_vivid"), from(ENDER_IO, "octadic_capacitor"), from(POWAH, "ender_gate_niotic")),
    CAPACITOR_4("capacitor_4", MPSItems.CAPACITOR_4, false, from(ENDER_IO_EVOLUTION, "capacitor_melodic"), from(POWAH, "ender_gate_spirited")),
    COMPUTER_CHIP("computer_chip", MPSItems.COMPUTER_CHIP, false,
            from(PNEUMATICCRAFT, "printed_circuit_board"), from(ENDER_IO, "sentient_ender")),
    BATTERY_1("battery_1", MPSItems.BATTERY_1, true,
            from(MEKANISM, "basic_energy_cube"), from(MODERN_INDUSTRIALIZATION, "silicon_battery"), from(ORITECH, "basic_battery"), from(POWAH, "battery_blazing")),
    BATTERY_2("battery_2", MPSItems.BATTERY_2, true,
            from(MEKANISM, "advanced_energy_cube"), from(MODERN_INDUSTRIALIZATION, "sodium_battery"), from(ORITECH, "advanced_battery"), from(POWAH, "battery_niotic")),
    BATTERY_3("battery_3", MPSItems.BATTERY_3, true,
            from(MEKANISM, "elite_energy_cube"), from(MODERN_INDUSTRIALIZATION, "cadmium_battery"), from(POWAH, "battery_spirited")),
    BATTERY_4("battery_4", MPSItems.BATTERY_4, true,
            from(MEKANISM, "ultimate_energy_cube"), from(MODERN_INDUSTRIALIZATION, "plutonium_battery"), from(POWAH, "battery_nitro")),
    SOLENOID("solenoid", MPSItems.SOLENOID, false, from(ORITECH, "magnetic_coil")),
    ION_THRUSTER("ion_thruster", MPSItems.ION_THRUSTER, false, from(ORITECH, "ion_thruster")),
    SERVO("servo", MPSItems.SERVO, false, from(MODERN_INDUSTRIALIZATION, "motor"), from(ORITECH, "motor")),
    IRON_PLATING("iron_plating", MPSItems.PLATING_IRON, false, from(ORITECH, "reinforced_carbon_sheet")),
    DIAMOND_PLATING("diamond_plating", MPSItems.PLATING_DIAMOND, false),
    RUBBER_HOSE("rubber_hose", MPSItems.RUBBER_HOSE, false),
    SOLAR_PANEL("solar_panel", MPSItems.SOLAR_PANEL, false,
            from(ENDER_IO, "energetic_photovoltaic_module"), from(MEKANISM, "mekanismgenerators:solar_panel"),
            from(ORITECH, "big_solar_panel_block"), from(PNEUMATICCRAFT, "solar_compressor"),
            from(POWAH, "solar_panel_basic"), from(SOLAR_FLUX, "sp_3"));

    public final String key;
    public final Supplier<? extends Item> nativeItem;
    public final boolean battery;
    public final List<Alternative> alternatives;

    CompatibleComponent(String key, Supplier<? extends Item> nativeItem, boolean battery, Alternative... alternatives) {
        this.key = key;
        this.nativeItem = nativeItem;
        this.battery = battery;
        this.alternatives = List.of(alternatives);
    }

    public ResourceLocation nativeId() {
        return BuiltInRegistries.ITEM.getKey(nativeItem.get());
    }

    public ResourceLocation conversionId() {
        return ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "compat/conversion/" + key);
    }

    /** Discovery tags unlock recipes; ingredient policy separately enforces each mod switch. */
    public TagKey<Item> discoveryTag() {
        return TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "components/" + key));
    }

    public static CompatibleComponent byKey(String key) {
        for (CompatibleComponent component : values()) if (component.key.equals(key)) return component;
        throw new IllegalArgumentException("Unknown compatibility component: " + key);
    }

    public static Optional<CompatibleComponent> forItem(ItemLike item) {
        for (CompatibleComponent component : values()) if (component.nativeItem.get() == item.asItem()) return Optional.of(component);
        return Optional.empty();
    }

    static Alternative from(CompatibleMod mod, String path) {
        return new Alternative(mod, path.contains(":") ? ResourceLocation.parse(path) : ResourceLocation.fromNamespaceAndPath(mod.modId, path));
    }

    public record Alternative(CompatibleMod mod, ResourceLocation itemId) {}
}
