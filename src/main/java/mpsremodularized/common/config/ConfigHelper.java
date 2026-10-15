package mpsremodularized.common.config;

import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHelper {
    public static File getConfigFolder(String modId) {
        return FMLPaths.CONFIGDIR.get().resolve(modId).toFile();
    }

    public static File setupConfigFile(String fileName, String modId) {
        return setupConfigFile(Path.of(modId, fileName).toString());
    }

    public static File setupConfigFile(String fileName) {
        Path configFile = FMLPaths.CONFIGDIR.get().resolve(fileName);
        File cfgFile = configFile.toFile();
        try {
            Files.createDirectories(configFile.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create config directory: " + configFile.getParent(), e);
        }
        return cfgFile;
    }
}
