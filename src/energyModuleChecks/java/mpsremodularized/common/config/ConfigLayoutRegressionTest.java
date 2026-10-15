package mpsremodularized.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import mpsremodularized.client.config.MPSClientConfig;
import mpsremodularized.common.config.module.ArmorModuleConfig;
import mpsremodularized.common.config.module.AxeModuleConfig;
import mpsremodularized.common.config.module.CosmeticModuleConfig;
import mpsremodularized.common.config.module.EnergyGenerationModuleConfig;
import mpsremodularized.common.config.module.EnvironmentalModuleConfig;
import mpsremodularized.common.config.module.FluidStorageConfig;
import mpsremodularized.common.config.module.HoeModuleConfig;
import mpsremodularized.common.config.module.MiningEnchantmentModuleConfig;
import mpsremodularized.common.config.module.MiningEnhancementModuleConfig;
import mpsremodularized.common.config.module.MovementModuleConfig;
import mpsremodularized.common.config.module.PickaxeModuleConfig;
import mpsremodularized.common.config.module.ShovelModuleConfig;
import mpsremodularized.common.config.module.ToolModuleConfig;
import mpsremodularized.common.config.module.VisionModuleConfig;
import mpsremodularized.common.config.module.WeaponModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSConfigurations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public final class ConfigLayoutRegressionTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        verifyDeclaredDefaults();
        Path gameDirectory = Files.createTempDirectory(Path.of("build"), "config-layout-check-").toAbsolutePath();
        FMLPaths.loadAbsolutePaths(gameDirectory);
        Path configRoot = gameDirectory.resolve("config");
        var catalog = MPSConfigurations.getSpecs();
        for (int tier = 1; tier <= 4; tier++) {
            int declared = new int[] {14, 22, 30, 71}[tier - 1];
            check(MPSCommonConfig.getPowerFistInventorySlots(tier) == declared,
                    "Power Fist provider safely reads declared slots before server config loading: " + tier);
        }
        check(catalog.size() == 17, "Exactly 17 registered config filenames");
        var identities = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<ModConfigSpec, Boolean>());
        check(catalog.values().stream().allMatch(identities::add), "Every spec registered once");
        for (var entry : catalog.entrySet()) {
            String name = entry.getKey();
            ModConfig.Type type = MPSConfigurations.getConfigType(entry.getValue());
            check(type == (entry.getValue() == MPSClientConfig.CLIENT_SPEC ? ModConfig.Type.CLIENT : ModConfig.Type.SERVER),
                    "Gameplay files synchronize from server; client preferences stay local: " + name);
            String relativeName = MPSConfigurations.getConfigFileName(name);
            check(!Path.of(relativeName).isAbsolute(), "Relative registration enables world overrides: " + name);
            Path file = ConfigHelper.setupConfigFile(name, "mpsremodularized").toPath();
            check(file.equals(configRoot.resolve("mpsremodularized").resolve(name)), name);
            check(Files.isDirectory(file.getParent()), "Created parent directory for " + name);
            CommentedConfig defaults = TomlFormat.newConfig();
            entry.getValue().correct(defaults);
            Files.writeString(file, new TomlWriter().writeToString(defaults));
            CommentedConfig fresh = new TomlParser().parse(Files.readString(file));
            check(entry.getValue().isCorrect(fresh), "Fresh disk config validates: " + name);
            check(fresh.equals(defaults), "Every default survives disk round-trip: " + name);
        }
        Path keybinds = ConfigHelper.setupConfigFile("mpsremodularized-keybinds.json", "mpsremodularized").toPath();
        check(keybinds.equals(configRoot.resolve("mpsremodularized/mpsremodularized-keybinds.json")), "Keybind path shares MPS root");
        check(Files.isDirectory(configRoot.resolve("mpsremodularized/modules")), "Module directory exists");
        try (var paths = Files.list(configRoot)) {
            check(paths.filter(Files::isDirectory).map(path -> path.getFileName().toString()).allMatch("mpsremodularized"::equals), "No extra mod or author config directories");
        }

        CommentedConfig config = TomlFormat.newConfig();
        MPSCommonConfig.SERVER_SPEC.correct(config);
        check(keys(config).equals(Set.of("General", "Armor", "Power_Fist", "ChargingBase settings", "Batteries", "InterModCompatibility")), "Equipment and optional compatibility settings share the main server spec");
        for (var mod : mpsremodularized.common.compat.CompatibleMod.values()) {
            check(Boolean.TRUE.equals(config.get("InterModCompatibility." + mod.configKey)), "Integration defaults true: " + mod.configKey);
        }
        check(Boolean.TRUE.equals(config.get("InterModCompatibility.useMPSRecipesWithEnabledMods")), "Native recipe fallback defaults true");
        CommentedConfig armor = config.get("Armor");
        check(keys(armor).equals(Set.of("Helm", "ChestPlate", "Leggings", "Boots")), "Armor contains all four pieces");
        String[] expandedPaths = {"Power_Fist.Tier_1.inventorySlots", "Power_Fist.Tier_2.inventorySlots",
                "Armor.Helm.Tier_2.inventorySlots", "Armor.Helm.Tier_3.inventorySlots",
                "Armor.Leggings.Tier_2.inventorySlots", "Armor.ChestPlate.Tier_2.inventorySlots"};
        int[] expandedSlots = {14, 22, 7, 10, 5, 6};
        int[] oldSlots = {5, 12, 6, 8, 4, 5};
        CommentedConfig previous = new TomlParser().parse(new TomlWriter().writeToString(config));
        for (int i = 0; i < expandedPaths.length; i++) {
            previous.set(expandedPaths[i], oldSlots[i]);
        }
        previous.set("General.maximumFlyingSpeedmps", 30.0);
        check(!MPSCommonConfig.SERVER_SPEC.isCorrect(previous), "Old undersized capacities require normal config correction");
        MPSCommonConfig.SERVER_SPEC.correct(previous);
        for (int i = 0; i < expandedPaths.length; i++) {
            check(((Number) previous.get(expandedPaths[i])).intValue() == expandedSlots[i], "Old capacity upgrades: " + expandedPaths[i]);
        }
        check(((Number) previous.get("General.maximumFlyingSpeedmps")).doubleValue() == 30.0, "Capacity correction preserves unrelated custom settings");
        for (int i = 0; i < expandedPaths.length; i++) {
            previous.set(expandedPaths[i], expandedSlots[i] + 1);
        }
        MPSCommonConfig.SERVER_SPEC.correct(previous);
        for (int i = 0; i < expandedPaths.length; i++) {
            check(((Number) previous.get(expandedPaths[i])).intValue() == expandedSlots[i] + 1, "Higher valid custom capacity retained: " + expandedPaths[i]);
        }
        String[] capacityPieces = {"Helm", "ChestPlate", "Leggings", "Boots", "Power_Fist"};
        int[][] approvedCapacities = {{4, 7, 10, 26}, {6, 6, 8, 22}, {4, 5, 6, 18}, {2, 4, 5, 16}, {14, 22, 30, 71}};
        for (int piece = 0; piece < capacityPieces.length; piece++) {
            for (int tier = 1; tier <= 4; tier++) {
                String path = (piece == 4 ? "Power_Fist" : "Armor." + capacityPieces[piece]) + ".Tier_" + tier + ".inventorySlots";
                check(((Number) config.get(path)).intValue() == approvedCapacities[piece][tier - 1],
                        "Approved equipment capacity: " + path);
            }
        }
        CommentedConfig existing = new TomlParser().parse(new TomlWriter().writeToString(config));
        existing.set("Armor.Helm.Tier_2.inventorySlots", 11);
        existing.set("Armor.ChestPlate.Tier_3.inventorySlots", 16);
        existing.set("Power_Fist.Tier_4.inventorySlots", 60);
        MPSCommonConfig.SERVER_SPEC.correct(existing);
        check(((Number) existing.get("Armor.Helm.Tier_2.inventorySlots")).intValue() == 11
                        && ((Number) existing.get("Armor.ChestPlate.Tier_3.inventorySlots")).intValue() == 16
                        && ((Number) existing.get("Power_Fist.Tier_4.inventorySlots")).intValue() == 60,
                "Approved default changes add no migration of existing valid config values");
        CommentedConfig weapons = TomlFormat.newConfig();
        WeaponModuleConfig.MPS_WEAPON_MODULE_SPEC.correct(weapons);
        check(((Number) weapons.get("Weapon_Modules.Lightning_Summoner_Launcher.energyConBase")).doubleValue() == 100_000.0,
                "Lightning default matches its valid and previously effective energy cost");
        for (String piece : keys(armor)) {
            CommentedConfig tiers = armor.get(piece);
            check(keys(tiers).equals(Set.of("Tier_1", "Tier_2", "Tier_3", "Tier_4")), piece + " tiers preserved");
        }
        for (int tier = 1; tier <= 4; tier++) {
            double heat = ((Number) config.get("Power_Fist.Tier_" + tier + ".maxHeat")).doubleValue();
            check(heat == 50 * (1 << (tier - 1)), "Power Fist heat capacity tier " + tier);
        }
        CommentedConfig batteries = config.get("Batteries");
        long capacity = 1_000_000;
        for (String battery : java.util.List.of(MPSConstants.MODULE_BATTERY_1__REGNAME,
                MPSConstants.MODULE_BATTERY_2__REGNAME, MPSConstants.MODULE_BATTERY_3__REGNAME,
                MPSConstants.MODULE_BATTERY_4__REGNAME)) {
            check(((Number) batteries.get(battery + ".maxEnergy")).longValue() == capacity,
                    "Battery default capacity preserved for " + battery);
            capacity *= 10;
        }
        check(!config.contains("PlayerDeath"), "Death retention follows vanilla keepInventory without an MPS override");

        String toml = new TomlWriter().writeToString(config);
        CommentedConfig reloaded = new TomlParser().parse(toml);
        reloaded.set("General.maximumFlyingSpeedmps", 30.0);
        reloaded.set("Armor.Helm.Tier_1.maxHeat", 7.5);
        reloaded.set("Power_Fist.Tier_4.maxHeat", 450.0);
        reloaded.set("Power_Fist.Tier_4.inventorySlots", 40);
        reloaded.set("ChargingBase settings.chargingBaseMaxEnergy", 2_000_000);
        MPSCommonConfig.SERVER_SPEC.correct(reloaded);
        check(MPSCommonConfig.SERVER_SPEC.isCorrect(reloaded), "Round-trip config validates before loading");
        // FML seals ILoadedConfig; use its real implementation for this isolated load check.
        var constructor = Class.forName("net.neoforged.fml.config.LoadedConfig")
                .getDeclaredConstructor(CommentedConfig.class, Path.class, ModConfig.class);
        constructor.setAccessible(true);
        var loadedConfig = (IConfigSpec.ILoadedConfig) constructor.newInstance(reloaded, null, null);
        MPSCommonConfig.SERVER_SPEC.acceptConfig(loadedConfig);
        MPSCommonConfig.loadSettings();
        check(MPSCommonConfig.SERVER_SPEC.isLoaded(), "Unified spec reports loaded");
        check(MPSCommonConfig.maxFlyingSpeed == 30.0, "General custom settings loaded");
        check(ArmorConfig.helmMaxHeat1 == 7.5, "Armor custom settings loaded");
        check(PowerFistConfig.powerFistMaxHeat4 == 450.0, "Power Fist custom settings loaded");
        check(MPSCommonConfig.getPowerFistInventorySlots(4) == 40, "Provider uses loaded server slots");
        check(ArmorConfig.chestPlateMaxHeat4 == 40.0 && ArmorConfig.leggingsMaxHeat4 == 24.0 && ArmorConfig.bootsMaxHeat4 == 16.0, "All armor pieces loaded");
        check(MPSCommonConfig.chargingBaseMaxEnergy == 2_000_000, "Charging base custom settings loaded");
        check(MPSCommonConfig.batteryMaxEnergy4 == 1_000_000_000, "Battery settings loaded through the shared callback");
        reloaded.set("General.maximumFlyingSpeedmps", 25.0);
        reloaded.set("Power_Fist.Tier_4.inventorySlots", 50);
        MPSCommonConfig.SERVER_SPEC.acceptConfig((IConfigSpec.ILoadedConfig) constructor.newInstance(reloaded, null, null));
        MPSCommonConfig.loadSettings();
        check(MPSCommonConfig.maxFlyingSpeed == 25.0,
                "Reload updates synchronized gameplay caches");
        check(MPSCommonConfig.getPowerFistInventorySlots(4) == 50, "Provider reads updated server slots without cached registration values");
        System.out.println("Config layout regression checks passed: " + checks);
    }

    private static void verifyDeclaredDefaults() throws Exception {
        Class<?>[] owners = {
                MPSClientConfig.class, MPSCommonConfig.class, ArmorModuleConfig.class,
                AxeModuleConfig.class, CosmeticModuleConfig.class, EnergyGenerationModuleConfig.class,
                EnvironmentalModuleConfig.class, FluidStorageConfig.class, HoeModuleConfig.class,
                MiningEnchantmentModuleConfig.class, MiningEnhancementModuleConfig.class,
                MovementModuleConfig.class, PickaxeModuleConfig.class, ShovelModuleConfig.class,
                ToolModuleConfig.class, VisionModuleConfig.class, WeaponModuleConfig.class
        };
        int specs = 0;
        int settings = 0;
        for (Class<?> owner : owners) {
            for (var field : owner.getFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == ModConfigSpec.class) {
                    ModConfigSpec spec = (ModConfigSpec) field.get(null);
                    settings += verifyValues(spec.getSpec(), owner.getSimpleName());
                    CommentedConfig defaults = TomlFormat.newConfig();
                    spec.correct(defaults);
                    check(spec.isCorrect(defaults), "Fresh defaults need no further correction: " + owner.getSimpleName());
                    specs++;
                }
            }
        }
        check(specs == 17 && settings == 463, "Validated every declared default across all 17 specs and 463 settings, including twenty new integration switches");
    }

    private static int verifyValues(UnmodifiableConfig values, String path) {
        int count = 0;
        for (var entry : values.entrySet()) {
            String name = path + "." + entry.getKey();
            if (entry.getValue() instanceof UnmodifiableConfig nested) {
                count += verifyValues(nested, name);
            } else if (entry.getValue() instanceof ModConfigSpec.ValueSpec value) {
                check(value.test(value.getDefault()), "Declared default is valid: " + name);
                count++;
            }
        }
        return count;
    }

    private static Set<String> keys(UnmodifiableConfig config) {
        return config.entrySet().stream().map(UnmodifiableConfig.Entry::getKey).collect(java.util.stream.Collectors.toSet());
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
