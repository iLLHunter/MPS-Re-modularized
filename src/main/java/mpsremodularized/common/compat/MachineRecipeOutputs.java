package mpsremodularized.common.compat;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import mpsremodularized.common.base.MPSLogger;
import mpsremodularized.common.constants.MPSConstants;
import net.neoforged.fml.loading.FMLPaths;

/** MPS-owned output settings; read on server start and data reload. Zero disables a route. */
public final class MachineRecipeOutputs {
    private MachineRecipeOutputs() {}

    public static Map<String, Integer> load(String mod, Map<String, Integer> defaults, Map<String, Integer> limits) {
        var values = new java.util.LinkedHashMap<>(defaults);
        var file = FMLPaths.CONFIGDIR.get().resolve(MPSConstants.MOD_ID).resolve("recipes").resolve(mod + ".json");
        try {
            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());
                var root = new JsonObject();
                root.addProperty("schemaVersion", 1);
                var outputs = new JsonObject();
                defaults.forEach(outputs::addProperty);
                root.add("outputs", outputs);
                Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(root) + "\n", StandardCharsets.UTF_8);
                return Map.copyOf(values);
            }
            try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                var root = JsonParser.parseReader(reader).getAsJsonObject();
                if (!root.has("schemaVersion") || root.get("schemaVersion").getAsInt() != 1)
                    throw new IllegalArgumentException("Expected schemaVersion 1");
                var outputs = root.getAsJsonObject("outputs");
                if (outputs == null) throw new IllegalArgumentException("Missing outputs object");
                boolean missingDefaults = defaults.keySet().stream().anyMatch(id -> !outputs.has(id));
                defaults.forEach((id, fallback) -> {
                    if (!outputs.has(id)) return;
                    try {
                        var raw = outputs.get(id);
                        if (!raw.isJsonPrimitive() || !raw.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException();
                        int count = raw.getAsBigDecimal().intValueExact();
                        if (count < 0 || count > limits.get(id)) throw new IllegalArgumentException();
                        values.put(id, count);
                    } catch (RuntimeException exception) {
                        MPSLogger.logger.warn("Invalid output quantity for {} in {}; using {} (allowed 0..{})",
                                id, file, fallback, limits.get(id));
                    }
                });
                if (missingDefaults) {
                    defaults.forEach((id, count) -> { if (!outputs.has(id)) outputs.addProperty(id, count); });
                    var temporary = Files.createTempFile(file.getParent(), mod + "-", ".json.tmp");
                    try {
                        Files.writeString(temporary, new GsonBuilder().setPrettyPrinting().create().toJson(root) + "\n", StandardCharsets.UTF_8);
                        try {
                            Files.move(temporary, file, java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                            Files.move(temporary, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }
                    } finally { Files.deleteIfExists(temporary); }
                }
            }
        } catch (Exception exception) {
            MPSLogger.logger.warn("Cannot read/write MPS recipe output settings {}: {}; using defaults", file, exception.toString());
        }
        return Map.copyOf(values);
    }
}
