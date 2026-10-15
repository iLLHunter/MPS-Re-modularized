package mpsremodularized.common.compat;

import java.util.ArrayList;
import java.util.List;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import mpsremodularized.common.item.module.tool.misc.GascraftLauncherModule;
import mpsremodularized.common.item.module.tool.misc.NativeToolModule;
import mpsremodularized.common.item.module.tool.misc.PortableStationModule;
import mpsremodularized.common.registration.ModuleRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IntermodModules {
    public record Entry(String id, String name, String donor, CompatibleMod provider, int tier, NativeModuleItem.Kind kind,
                        DeferredHolder<Item, NativeModuleItem> item) {}
    public static final List<Entry> ENTRIES = new ArrayList<>();
    private IntermodModules() {}

    public static void register(DeferredRegister.Items items) {
        add(items, "gascraft_gas_mask_module", "Gas Mask Module", "gascraft:gas_mask", CompatibleMod.GASCRAFT, 3, NativeModuleItem.Kind.GAS_MASK);
        add(items, "gascraft_grenade_launcher_module", "Grenade Launcher Module", "gascraft:grenade_launcher", CompatibleMod.GASCRAFT, 4, NativeModuleItem.Kind.GRENADE_LAUNCHER);
        String[] mekanism = {"stone", "osmium", "steel", "refined_obsidian"};
        String[] mystical = {"prudentium", "tertium", "imperium", "supremium", "awakened_supremium"};
        String[] actually = {"stone", "iron", "diamond", "netherite"};
        for (int i = 0; i < mekanism.length; i++) paxel(items, "mekanismtools", mekanism[i], "paxel", CompatibleMod.MEKANISM_TOOLS, i + 1);
        for (int i = 0; i < mystical.length; i++) paxel(items, "mysticalagradditions", mystical[i], "paxel", CompatibleMod.MYSTICAL_AGRADDITIONS, Math.min(i + 1, 4));
        for (int i = 0; i < actually.length; i++) paxel(items, "actuallyadditions", actually[i], "aiot", CompatibleMod.ACTUALLY_ADDITIONS, i + 1);
        add(items, "mob_imprisonment_tool_module", "Mob Imprisonment Tool Module", "industrialforegoing:mob_imprisonment_tool", CompatibleMod.INDUSTRIAL_FOREGOING, 2, NativeModuleItem.Kind.TOOL);
        add(items, "machine_settings_copier_module", "Machine Settings Copier Module", "industrialforegoing:machine_settings_copier", CompatibleMod.INDUSTRIAL_FOREGOING, 2, NativeModuleItem.Kind.TOOL);
        add(items, "flux_configurator_module", "Flux Configurator Module", "fluxnetworks:flux_configurator", CompatibleMod.FLUX_NETWORKS, 2, NativeModuleItem.Kind.TOOL);
        add(items, "seismic_sensor_module", "Seismic Sensor Module", "pneumaticcraft:seismic_sensor", CompatibleMod.PNEUMATICCRAFT, 2, NativeModuleItem.Kind.TOOL);
        add(items, "smithing_table_module", "Smithing Table Module", "minecraft:smithing_table", null, 2, NativeModuleItem.Kind.STATION);
        add(items, "stonecutter_module", "Stonecutter Module", "minecraft:stonecutter", null, 2, NativeModuleItem.Kind.STATION);
        add(items, "anvil_module", "Anvil Module", "minecraft:anvil", null, 3, NativeModuleItem.Kind.STATION);
        add(items, "brewing_stand_module", "Brewing Stand Module", "minecraft:brewing_stand", null, 2, NativeModuleItem.Kind.STATION);
        add(items, "natures_compass_module", "Nature's Compass Module", "naturescompass:naturescompass", CompatibleMod.NATURES_COMPASS, 2, NativeModuleItem.Kind.TOOL);
        add(items, "explorers_compass_module", "Explorer's Compass Module", "explorerscompass:explorerscompass", CompatibleMod.EXPLORERS_COMPASS, 2, NativeModuleItem.Kind.TOOL);
        add(items, "mekanism_configurator_module", "Configurator Module", "mekanism:configurator", CompatibleMod.MEKANISM, 2, NativeModuleItem.Kind.TOOL);
        add(items, "configuration_card_module", "Configuration Card Module", "mekanism:configuration_card", CompatibleMod.MEKANISM, 2, NativeModuleItem.Kind.TOOL);
        add(items, "network_reader_module", "Network Reader Module", "mekanism:network_reader", CompatibleMod.MEKANISM, 3, NativeModuleItem.Kind.TOOL);
        add(items, "seismic_reader_module", "Seismic Reader Module", "mekanism:seismic_reader", CompatibleMod.MEKANISM, 2, NativeModuleItem.Kind.TOOL);
        add(items, "portable_qio_dashboard_module", "Portable QIO Dashboard Module", "mekanism:portable_qio_dashboard", CompatibleMod.MEKANISM, 3, NativeModuleItem.Kind.TOOL);
        add(items, "gauge_dropper_module", "Gauge Dropper Module", "mekanism:gauge_dropper", CompatibleMod.MEKANISM, 2, NativeModuleItem.Kind.TOOL);
        add(items, "remote_control_module", "Remote Control Module", "mekanism_lasers:remote_control", CompatibleMod.MEKANISM_LASERS, 2, NativeModuleItem.Kind.TOOL);
        add(items, "geiger_unit_module", "Geiger Unit Module", "mekanism:module_geiger_unit", CompatibleMod.MEKANISM, 3, NativeModuleItem.Kind.HELMET_UNIT);
        add(items, "dosimeter_unit_module", "Dosimeter Unit Module", "mekanism:module_dosimeter_unit", CompatibleMod.MEKANISM, 3, NativeModuleItem.Kind.HELMET_UNIT);
        add(items, "remote_lever_module", "Remote Lever Module", "cyclic:lever_remote", CompatibleMod.CYCLIC, 2, NativeModuleItem.Kind.TOOL);
        add(items, "gps_compass_module", "GPS Compass Module", "cyclic:compass_gps", CompatibleMod.CYCLIC, 2, NativeModuleItem.Kind.TOOL);
        add(items, "powered_diamond_anvil_module", "Powered Diamond Anvil Module", "cyclic:anvil", CompatibleMod.CYCLIC, 4, NativeModuleItem.Kind.STATION);
        String[] sticks = {"copper", "iron", "diamond", "netherite"};
        for (int i = 0; i < sticks.length; i++) add(items, sticks[i] + "_construction_stick_module",
                title(sticks[i]) + " Construction Stick Module", "constructionstick:" + sticks[i] + "_stick",
                CompatibleMod.CONSTRUCTION_STICKS, i + 1, NativeModuleItem.Kind.CONSTRUCTION_STICK);
        add(items, "engineers_goggles_module", "Engineer's Goggles Module", "actuallyadditions:engineers_goggles", CompatibleMod.ACTUALLY_ADDITIONS, 3, NativeModuleItem.Kind.ARMOR_HEAD);
        add(items, "true_vision_goggles_module", "Goggles of True Vision Module", "secretroomsmod:true_vision_goggles", CompatibleMod.SECRET_ROOMS, 4, NativeModuleItem.Kind.ARMOR_HEAD);
        add(items, "engineers_multimeter_module", "Engineer's Multimeter Module", "immersiveengineering:voltmeter", CompatibleMod.IMMERSIVE_ENGINEERING, 2, NativeModuleItem.Kind.TOOL);
        add(items, "manometer_module", "Manometer Module", "pneumaticcraft:manometer", CompatibleMod.PNEUMATICCRAFT, 2, NativeModuleItem.Kind.TOOL);
        add(items, "memory_card_module", "Memory Card Module", "ae2:memory_card", CompatibleMod.AE2, 2, NativeModuleItem.Kind.TOOL);
        add(items, "ender_memory_card_module", "Ender Memory Card Module", "enderio_evolution:ender_memory_card", CompatibleMod.ENDER_IO_EVOLUTION, 2, NativeModuleItem.Kind.TOOL);
        add(items, "advanced_dominion_wand_module", "Advanced Dominion Wand Module", "ars_additions:advanced_dominion_wand", CompatibleMod.ARS_ADDITIONS, 4, NativeModuleItem.Kind.TOOL);
        add(items, "throughput_monitor_configurator_module", "Throughput Monitor Configurator Module", "advanced_ae:throughput_monitor_configurator", CompatibleMod.ADVANCED_AE, 3, NativeModuleItem.Kind.TOOL);
        add(items, "pneumatic_remote_module", "Remote Module", "pneumaticcraft:remote", CompatibleMod.PNEUMATICCRAFT, 3, NativeModuleItem.Kind.TOOL);
        add(items, "remote_controller_module", "Remote Controller Module", "mffs:remote_controller", CompatibleMod.MFFS, 3, NativeModuleItem.Kind.TOOL);
        add(items, "smart_wrench_module", "Smart Wrench Module", "rftoolsbase:smartwrench", CompatibleMod.RFTOOLS_BASE, 2, NativeModuleItem.Kind.TOOL);
        add(items, "gps_tool_module", "GPS Tool Module", "pneumaticcraft:gps_tool", CompatibleMod.PNEUMATICCRAFT, 2, NativeModuleItem.Kind.TOOL);
        add(items, "gps_area_tool_module", "GPS Area Tool Module", "pneumaticcraft:gps_area_tool", CompatibleMod.PNEUMATICCRAFT, 3, NativeModuleItem.Kind.TOOL);
        add(items, "engineers_workbench_module", "Engineer's Workbench Module", "immersiveengineering:workbench", CompatibleMod.IMMERSIVE_ENGINEERING, 2, NativeModuleItem.Kind.STATION);
        add(items, "engineers_circuit_workbench_module", "Engineer's Circuit Workbench Module", "immersiveengineering:circuit_table", CompatibleMod.IMMERSIVE_ENGINEERING, 3, NativeModuleItem.Kind.STATION);
        add(items, "tinkering_table_module", "Tinkering Table Module", "mysticalagriculture:tinkering_table", CompatibleMod.MYSTICAL_AGRICULTURE, 3, NativeModuleItem.Kind.STATION);
        add(items, "ae2_network_tool_module", "Network Tool Module", "ae2:network_tool", CompatibleMod.AE2, 3, NativeModuleItem.Kind.TOOL);
        String[] armorPieces = {"helmet", "chestplate", "leggings", "boots"};
        String[] shimmerPieces = {"goggles", "tunic", "leggings", "skates"};
        String[] hazmatPieces = {"mask", "gown", "pants", "boots"};
        NativeModuleItem.Kind[] armorKinds = {NativeModuleItem.Kind.ARMOR_HEAD, NativeModuleItem.Kind.ARMOR_CHEST,
                NativeModuleItem.Kind.ARMOR_LEGS, NativeModuleItem.Kind.ARMOR_FEET};
        for (int i = 0; i < armorPieces.length; i++) {
            String piece = armorPieces[i];
            add(items, "tyr_" + piece + "_module", "Tyr " + title(piece) + " Module",
                    "forbidden_arcanus:tyr_" + piece, CompatibleMod.FORBIDDEN_ARCANUS, 2, armorKinds[i], false);
            add(items, "soulstained_steel_" + piece + "_module", "Soulstained Steel " + title(piece) + " Module",
                    "malum:soul_stained_steel_" + piece, CompatibleMod.MALUM, 3, armorKinds[i], false);
            add(items, "malignant_stronghold_" + piece + "_module", "Malignant Stronghold " + title(piece) + " Module",
                    "malum:malignant_stronghold_" + piece, CompatibleMod.MALUM, 4, armorKinds[i], false);
            add(items, "hazmat_" + hazmatPieces[i] + "_module", "Hazmat " + title(hazmatPieces[i]) + " Module",
                    "mekanism:hazmat_" + hazmatPieces[i], CompatibleMod.MEKANISM, 2, armorKinds[i], false);
            add(items, "netherite_battlemage_" + piece + "_module", "Netherite Battlemage " + title(piece) + " Module",
                    "irons_spellbooks:netherite_mage_" + piece, CompatibleMod.IRONS_SPELLBOOKS, 4, armorKinds[i], false);
            piece = shimmerPieces[i];
            add(items, "shimmerweave_" + piece + "_module", "Shimmerweave " + title(piece) + " Module",
                    "auroral:shimmerweave_" + piece, CompatibleMod.AURORAL, 4, armorKinds[i]);
        }
    }

    private static String title(String word) { return Character.toUpperCase(word.charAt(0)) + word.substring(1); }

    private static void paxel(DeferredRegister.Items items, String namespace, String material, String tool, CompatibleMod mod, int tier) {
        String nativePath = namespace.equals("actuallyadditions") ? material + "_aiot" : material + "_paxel";
        String name = java.util.Arrays.stream(material.split("_")).map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(java.util.stream.Collectors.joining(" ")) + (tool.equals("aiot") ? " AIOT Module" : " Paxel Module");
        add(items, namespace + "_" + nativePath + "_module", name, namespace + ":" + nativePath, mod, tier, NativeModuleItem.Kind.PAXEL);
    }

    private static void add(DeferredRegister.Items items, String id, String name, String donor, CompatibleMod mod, int tier, NativeModuleItem.Kind kind) {
        boolean toggleable = switch (kind) {
            case TOOL -> java.util.Set.of("mekanism_configurator_module", "network_reader_module", "seismic_reader_module",
                    "gps_tool_module", "gps_area_tool_module").contains(id);
            case STATION -> id.equals("powered_diamond_anvil_module");
            default -> true;
        };
        add(items, id, name, donor, mod, tier, kind, toggleable);
    }

    private static void add(DeferredRegister.Items items, String id, String name, String donor, CompatibleMod mod, int tier, NativeModuleItem.Kind kind, boolean toggleable) {
        var key = ResourceLocation.fromNamespaceAndPath("mpsremodularized", id);
        DeferredHolder<Item, NativeModuleItem> item;
        if (kind == NativeModuleItem.Kind.GRENADE_LAUNCHER) {
            item = ModuleRegistry.INSTANCE.registerItem(items, key, () -> new NativeModuleItem(donor, mod, kind),
                    GascraftLauncherModule.class, GascraftLauncherModule::new, tier);
        } else if (kind == NativeModuleItem.Kind.STATION) {
            item = toggleable
                    ? ModuleRegistry.INSTANCE.registerItem(items, key, () -> new NativeModuleItem(donor, mod, kind),
                            PortableStationModule.Toggleable.class, PortableStationModule.Toggleable::new, tier)
                    : ModuleRegistry.INSTANCE.registerItem(items, key, () -> new NativeModuleItem(donor, mod, kind),
                            PortableStationModule.class, PortableStationModule::new, tier);
        } else if (kind == NativeModuleItem.Kind.TOOL || kind == NativeModuleItem.Kind.CONSTRUCTION_STICK) {
            item = toggleable
                    ? ModuleRegistry.INSTANCE.registerItem(items, key, () -> new NativeModuleItem(donor, mod, kind),
                            NativeToolModule.Toggleable.class, NativeToolModule.Toggleable::new, tier)
                    : ModuleRegistry.INSTANCE.registerItem(items, key, () -> new NativeModuleItem(donor, mod, kind),
                            NativeToolModule.class, NativeToolModule::new, tier);
        } else if (toggleable) {
            item = ModuleRegistry.INSTANCE.registerItem(items, key, () -> new NativeModuleItem(donor, mod, kind),
                    ToggleableNativeModule.class, stack -> new ToggleableNativeModule(stack, kind), tier);
        } else {
            item = ModuleRegistry.INSTANCE.registerItem(items, key, () -> new NativeModuleItem(donor, mod, kind),
                    PassiveNativeModule.class, stack -> new PassiveNativeModule(stack, kind), tier);
        }
        ENTRIES.add(new Entry(id, name, donor, mod, tier, kind, item));
    }

    public static class PassiveNativeModule extends PowerModule {
        public PassiveNativeModule(ItemStack stack, NativeModuleItem.Kind kind) {
            super(stack, kind == NativeModuleItem.Kind.PAXEL ? ModuleCategory.PAXEL : ModuleCategory.ENVIRONMENTAL,
                    switch (kind) {
                        case PAXEL -> ModuleTarget.TOOLONLY;
                        case ARMOR_CHEST -> ModuleTarget.TORSOONLY;
                        case ARMOR_LEGS -> ModuleTarget.LEGSONLY;
                        case ARMOR_FEET -> ModuleTarget.FEETONLY;
                        default -> ModuleTarget.HEADONLY;
                    });
        }
        @Override public boolean isAllowed() { return ((NativeModuleItem) getModule().getItem()).available(); }
    }

    public static final class ToggleableNativeModule extends PassiveNativeModule implements mpsremodularized.common.capabilities.module.toggleable.IToggleableModule {
        public ToggleableNativeModule(ItemStack stack, NativeModuleItem.Kind kind) {
            super(stack, kind);
        }
    }

    public static boolean paxelProviderPresent() {
        return ENTRIES.stream().anyMatch(e -> e.kind == NativeModuleItem.Kind.PAXEL && e.item.get().available());
    }
}
