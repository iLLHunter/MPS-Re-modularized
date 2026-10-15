package mpsremodularized.datagen.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlWriter;
import mpsremodularized.client.config.MPSClientConfig;
import mpsremodularized.common.config.MPSCommonConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSConfigurations;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

/** Exercises FML's real synced-TOML parser and event callbacks; does not open a network connection. */
final class ServerConfigSyncValidation {
    static void validate() {
        int checks = 0;
        boolean localSounds = MPSClientConfig.useSounds;
        try {
            var constructor = ModConfig.class.getDeclaredConstructor(ModConfig.Type.class,
                    IConfigSpec.class, ModContainer.class, String.class, ReentrantLock.class);
            constructor.setAccessible(true);
            var container = ModList.get().getModContainerById(MPSConstants.MOD_ID).orElseThrow();
            for (var entry : MPSConfigurations.getSpecs().entrySet()) {
                var spec = entry.getValue();
                if (spec == MPSClientConfig.CLIENT_SPEC) continue;
                CommentedConfig values = CommentedConfig.inMemory();
                spec.correct(values);
                if (spec == MPSCommonConfig.SERVER_SPEC) {
                    values.set("General.maximumFlyingSpeedmps", 37.0);
                    values.set("Batteries." + MPSConstants.MODULE_BATTERY_2__REGNAME + "." + MPSConstants.CONFIG_MAX_TRANSFER, 3);
                    values.set("InterModCompatibility.useMekanism", false);
                    values.set("InterModCompatibility.useMPSRecipesWithEnabledMods", false);
                }
                check(spec.isCorrect(values), "Server fixture contains only valid declared settings");
                checks++;
                // Each object models a client receiving this server file for the first time.
                var config = constructor.newInstance(ModConfig.Type.SERVER, spec, container,
                        MPSConfigurations.getConfigFileName(entry.getKey()), new ReentrantLock());
                ConfigTracker.acceptSyncedConfig(config,
                        new TomlWriter().writeToString(values).getBytes(StandardCharsets.UTF_8));
                check(spec.isLoaded(), "Synced server config loads: " + entry.getKey());
                checks++;
            }
            check(MPSCommonConfig.maxFlyingSpeed == 37.0, "Synced gameplay value refreshes the runtime cache");
            check(MPSCommonConfig.batteryMaxTransfer2 == 3, "Synced battery value refreshes its runtime cache");
            check(MPSClientConfig.useSounds == localSounds, "Local client preferences are unaffected");
            check(!MPSCommonConfig.COMPATIBILITY_SETTINGS.enabledMods().contains(mpsremodularized.common.compat.CompatibleMod.MEKANISM),
                    "Synced integration switch takes the server value");
            check(!MPSCommonConfig.COMPATIBILITY_SETTINGS.keepNativeRecipes(), "Synced native recipe switch takes the server value");
            checks += 5;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot initialize synced config fixture", exception);
        } finally {
            ConsolidationSnapshot.loadDefaultsForValidation();
        }
        System.out.println("Server config sync parser and callback checks passed: " + checks);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
