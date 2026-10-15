package mpsremodularized.datagen.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredRegister;
import mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.capabilities.module.rightclick.IRightClickModule;
import mpsremodularized.common.capabilities.module.tickable.IPlayerTickModule;
import mpsremodularized.common.capabilities.module.toggleable.IToggleableModule;
import mpsremodularized.common.capabilities.render.hud.IHudModule;
import mpsremodularized.common.registration.ModuleRegistry;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

/** Opt-in consolidation inventory. It is excluded from the runtime JAR. */
final class ConsolidationSnapshot {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final List<String> REGISTRY_OWNERS = List.of(
            "mpsremodularized.common.registration.MPSBlockEntities",
            "mpsremodularized.common.registration.MPSDataComponents",
            "mpsremodularized.common.registration.MPSIngredientTypes",
            "mpsremodularized.common.registration.RecipeSerializersRegistry",
            "mpsremodularized.common.registration.MPSItems",
            "mpsremodularized.common.registration.MPSBlocks",
            "mpsremodularized.common.registration.MPSEntities",
            "mpsremodularized.common.registration.MPSParticles",
            "mpsremodularized.common.registration.MPSMenuTypes",
            "mpsremodularized.common.registration.MPSArmorMaterial",
            "mpsremodularized.client.sound.MPSSoundDictionary");
    private static final List<String> CONFIG_OWNERS = List.of(
            "mpsremodularized.client.config.MPSClientConfig",
            "mpsremodularized.common.config.MPSCommonConfig",
            "mpsremodularized.common.config.module.ArmorModuleConfig",
            "mpsremodularized.common.config.module.AxeModuleConfig",
            "mpsremodularized.common.config.module.CosmeticModuleConfig",
            "mpsremodularized.common.config.module.EnergyGenerationModuleConfig",
            "mpsremodularized.common.config.module.EnvironmentalModuleConfig",
            "mpsremodularized.common.config.module.FluidStorageConfig",
            "mpsremodularized.common.config.module.HoeModuleConfig",
            "mpsremodularized.common.config.module.MiningEnchantmentModuleConfig",
            "mpsremodularized.common.config.module.MiningEnhancementModuleConfig",
            "mpsremodularized.common.config.module.MovementModuleConfig",
            "mpsremodularized.common.config.module.PickaxeModuleConfig",
            "mpsremodularized.common.config.module.ShovelModuleConfig",
            "mpsremodularized.common.config.module.ToolModuleConfig",
            "mpsremodularized.common.config.module.VisionModuleConfig",
            "mpsremodularized.common.config.module.WeaponModuleConfig");

    static void loadDefaultsForValidation() {
        try {
            configDefaults();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot initialize declared gameplay defaults for datagen validation", exception);
        }
    }

    static void writeIfRequested() {
        String destination = System.getProperty("mps.consolidationSnapshot");
        if (destination == null) {
            return;
        }
        try {
            JsonObject result = new JsonObject();
            result.addProperty("schemaVersion", 1);
            result.addProperty("baselineCommit", "1e5b5581649d538e2b2d87a40dd141f87f6cd418");
            result.addProperty("modVersion", ModList.get().getModContainerById("mpsremodularized").orElseThrow().getModInfo().getVersion().toString());
            String stage = System.getProperty("mps.consolidationStage", "pre-pass-4");
            result.addProperty("consolidationStage", stage);
            result.addProperty("configuration", "Declared spec defaults explicitly loaded through config callbacks in the opt-in datagen harness");
            result.add("registrations", registrations());
            result.add("configDefaults", configDefaults());
            ModuleRegistryValidation.validate();
            result.add("modules", modules());
            JsonObject configFiles = new JsonObject();
            mpsremodularized.common.registration.MPSConfigurations.getSpecs().forEach((name, spec) ->
                    configFiles.addProperty("config/mpsremodularized/" + name,
                            mpsremodularized.common.registration.MPSConfigurations.getConfigType(spec).name()));
            result.add("configFiles", configFiles);
            result.add("configTranslations", GSON.toJsonTree(
                    mpsremodularized.datagen.client.lang.MPSLanguageProvider_EN_US.configTranslations()));
            result.add("network", NetworkProtocolValidation.report());

            if (result.getAsJsonArray("registrations").size() != 168
                    || result.getAsJsonArray("modules").size() != 76
                    || result.getAsJsonObject("configDefaults").size() != 17) {
                throw new IllegalStateException("Unexpected consolidation counts; review the inventory before capturing");
            }
            Path path = Path.of(destination);
            if (!stage.equals("release-4.0.3") || !path.getFileName().toString().equals("runtime-" + stage + ".json")) {
                throw new IllegalArgumentException("Consolidation capture cannot overwrite the historical baseline");
            }
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(result) + "\n");
            System.out.println("Consolidation snapshot written: " + path);
        } catch (IOException | ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot capture consolidation baseline", exception);
        }
    }

    /** Optional current item inventory, independent of the frozen consolidation snapshots. */
    static void writeItemNamesIfRequested() {
        String destination = System.getProperty("mps.itemNamingAudit");
        if (destination == null) return;
        var rows = new JsonArray();
        net.minecraft.core.registries.BuiltInRegistries.ITEM.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals("mpsremodularized"))
                .sorted(java.util.Comparator.comparing(entry -> entry.getKey().location().toString()))
                .forEach(entry -> {
                    var row = new JsonObject();
                    row.addProperty("id", entry.getKey().location().toString());
                    row.addProperty("translationKey", entry.getValue().getDescriptionId());
                    rows.add(row);
                });
        try {
            var path = Path.of(destination);
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(rows) + "\n");
            System.out.println("Item naming inventory written: " + path + " (" + rows.size() + " items)");
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot write item naming inventory", exception);
        }
    }

    private static JsonArray registrations() throws ReflectiveOperationException {
        TreeMap<String, JsonObject> sorted = new TreeMap<>();
        for (String owner : REGISTRY_OWNERS) {
            for (var field : Class.forName(owner).getFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || !DeferredRegister.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                var register = (DeferredRegister<?>) field.get(null);
                String registry = register.getRegistryKey().location().toString();
                for (var entry : register.getEntries()) {
                    String id = entry.getId().toString();
                    JsonObject row = new JsonObject();
                    row.addProperty("registry", registry);
                    row.addProperty("id", id);
                    row.addProperty("targetId", id);
                    row.addProperty("owner", owner + "." + field.getName());
                    if (sorted.put(registry + "|" + id, row) != null) {
                        throw new IllegalStateException("Duplicate baseline registration: " + registry + " " + id);
                    }
                }
            }
        }
        JsonArray result = new JsonArray();
        sorted.values().forEach(result::add);
        return result;
    }

    private static JsonArray modules() {
        JsonArray result = new JsonArray();
        for (var definition : ModuleRegistry.INSTANCE.freeze()) {
            ItemStack stack = new ItemStack(definition.item().get());
            var capability = definition.createCapability(stack);
            JsonObject row = new JsonObject();
            row.addProperty("id", definition.id().toString());
            row.addProperty("targetId", definition.id().toString());
            row.addProperty("capabilityClass", definition.capabilityType().getName());
            row.addProperty("minimumTierOverride", definition.minimumTierOverride());
            row.addProperty("effectiveMinimumTier", capability.getTier());
            row.addProperty("target", capability.getTarget().toString());
            row.addProperty("category", capability.getCategory().toString());
            row.addProperty("allowed", capability.isAllowed());
            row.addProperty("onlineDefault", capability.isModuleOnline());
            JsonArray supported = new JsonArray();
            for (Class<?> type : List.of(IToggleableModule.class, IPlayerTickModule.class,
                    IRightClickModule.class, IBlockBreakingModule.class, IHudModule.class)) {
                if (definition.supports(type)) {
                    supported.add(type.getSimpleName());
                }
            }
            row.add("interfaces", supported);
            JsonObject properties = new JsonObject();
            for (var entry : new TreeMap<>(capability.getPropertyModifiers()).entrySet()) {
                JsonObject property = new JsonObject();
                CompoundTag allTradeoffsAtOne = new CompoundTag();
                JsonArray modifiers = new JsonArray();
                for (var modifier : entry.getValue()) {
                    modifiers.add(modifier.toString());
                    if (modifier instanceof IPowerModule.PropertyModifierLinearAdditive linear) {
                        allTradeoffsAtOne.putDouble(linear.tradeoffName, 1.0);
                    }
                }
                property.addProperty("atZero", capability.applyPropertyModifiers(entry.getKey(), new CompoundTag()));
                property.addProperty("atOne", capability.applyPropertyModifiers(entry.getKey(), allTradeoffsAtOne));
                property.add("modifiers", modifiers);
                properties.add(entry.getKey(), property);
            }
            row.add("properties", properties);
            result.add(row);
        }
        return result;
    }

    private static JsonObject configDefaults() throws ReflectiveOperationException {
        JsonObject result = new JsonObject();
        // Datagen skips normal config loading. Use the real FML config objects,
        // as in ConfigLayoutRegressionTest, then invoke each existing callback.
        var configConstructor = ModConfig.class.getDeclaredConstructor(ModConfig.Type.class,
                IConfigSpec.class, ModContainer.class, String.class, ReentrantLock.class);
        configConstructor.setAccessible(true);
        var loadedConstructor = Class.forName("net.neoforged.fml.config.LoadedConfig")
                .getDeclaredConstructor(CommentedConfig.class, Path.class, ModConfig.class);
        loadedConstructor.setAccessible(true);
        for (String owner : CONFIG_OWNERS) {
            Class<?> ownerClass = Class.forName(owner);
            var fields = ownerClass.getFields();
            Arrays.sort(fields, java.util.Comparator.comparing(java.lang.reflect.Field::getName));
            for (var field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == ModConfigSpec.class) {
                    CommentedConfig defaults = CommentedConfig.inMemory();
                    ModConfigSpec spec = (ModConfigSpec) field.get(null);
                    spec.correct(defaults);
                    result.add(owner + "." + field.getName(), configJson(defaults));
                    String modId = owner.substring(0, owner.indexOf('.'));
                    var container = ModList.get().getModContainerById(modId).orElseThrow();
                    var type = owner.contains(".client.") ? ModConfig.Type.CLIENT : ModConfig.Type.SERVER;
                    var config = configConstructor.newInstance(type, spec, container, "baseline.toml", new ReentrantLock());
                    var loaded = (IConfigSpec.ILoadedConfig) loadedConstructor.newInstance(defaults, null, config);
                    spec.acceptConfig(loaded);
                    var callback = ownerClass.getDeclaredMethod("onLoad", ModConfigEvent.class);
                    callback.setAccessible(true);
                    callback.invoke(null, new ModConfigEvent.Loading(config));
                }
            }
        }
        return result;
    }

    private static JsonElement configJson(Object value) {
        if (value instanceof UnmodifiableConfig config) {
            JsonObject result = new JsonObject();
            TreeMap<String, Object> values = new TreeMap<>();
            for (var entry : config.entrySet()) {
                values.put(entry.getKey(), entry.getValue());
            }
            values.forEach((key, item) -> result.add(key, configJson(item)));
            return result;
        }
        return GSON.toJsonTree(value);
    }

}
