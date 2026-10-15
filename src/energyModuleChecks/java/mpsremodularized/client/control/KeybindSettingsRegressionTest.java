package mpsremodularized.client.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import mpsremodularized.common.capabilities.module.tickable.PassivePlayerTickModule;
import mpsremodularized.common.capabilities.module.toggleable.ToggleableModule;
import mpsremodularized.common.registration.ModuleRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class KeybindSettingsRegressionTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        Path gameDirectory = Files.createTempDirectory(Path.of("build"), "keybind-check-").toAbsolutePath();
        FMLPaths.loadAbsolutePaths(gameDirectory);
        for (String path : new String[]{"night_vision", "active_camouflage", "railgun"}) {
            ModuleRegistry.INSTANCE.register(ResourceLocation.parse("mpsremodularized:" + path),
                    () -> { throw new AssertionError("Resolved item during key registration"); },
                    ToggleableModule.class, stack -> { throw new AssertionError("Created capability during key registration"); }, -1);
        }
        ModuleRegistry.INSTANCE.register(ResourceLocation.parse("test:ordinary"), () -> null, PowerModule.class, stack -> null, -1);
        ModuleRegistry.INSTANCE.register(ResourceLocation.parse("test:automatic"),
                () -> { throw new AssertionError("Resolved automatic module during key discovery"); },
                PassivePlayerTickModule.class, stack -> { throw new AssertionError("Created automatic capability during discovery"); }, -1);
        KeymappingKeyHandler.initializeModuleKeyBindings();
        check(KeymappingKeyHandler.getMPSKeyMappings().size() == 3, "Register only declared toggle modules before capabilities exist");
        MPSKeyMapping vision = KeymappingKeyHandler.getModuleKeyMapping("keybinding.mpsremodularized.night_vision");
        MPSKeyMapping camo = KeymappingKeyHandler.getModuleKeyMapping("keybinding.mpsremodularized.active_camouflage");
        check(vision != null && camo != null, "Previously missing bindings are discovered");
        check(vision.isUnbound() && vision.showOnHud, "Fresh bindings opt into HUD; unbound keys are filtered from rendering");
        KeyMappingReaderWriter.INSTANCE.readInKeybinds();
        Path file = gameDirectory.resolve("config/mpsremodularized/mpsremodularized-keybinds.json");
        check(!Files.exists(file), "A missing file does not cause an empty/premature write");
        KeyMappingReaderWriter.INSTANCE.writeOutKeybindSetings();
        check(JsonParser.parseString(Files.readString(file)).getAsJsonObject().size() == 4, "Fresh file includes every binding");
        JsonObject saved = JsonParser.parseString("""
                {
                  "formatVersion": 2,
                  "keybinding.mpsremodularized.night_vision": {"registryName":"mpsremodularized:night_vision", "showOnHud":true, "defaultKey":78},
                  "keybinding.mpsremodularized.active_camouflage": {"registryName":"mpsremodularized:active_camouflage", "showOnHud":false, "defaultKey":67},
                  "keybinding.removed.module": {"registryName":"removed:module", "showOnHud":true, "defaultKey":82}
                }
                """).getAsJsonObject();
        KeyMappingReaderWriter.INSTANCE.applySettings(saved);
        check(vision.getKey().getValue() == 78 && vision.showOnHud, "Restore existing numeric key/HUD setting");
        check(camo.getKey().getValue() == 67 && !camo.showOnHud, "Restore second key");
        check(camo.getKeyModifier() == KeyModifier.NONE, "Older current-format files without a modifier still load");
        check(KeymappingKeyHandler.getModuleKeyMapping(vision.getName()) == vision, "Restore does not replace registered mapping");
        KeymappingKeyHandler.initializeModuleKeyBindings();
        check(KeymappingKeyHandler.getModuleKeyMapping(vision.getName()) == vision && vision.showOnHud, "Repeated discovery retains object and preferences");
        check(KeymappingKeyHandler.getMPSKeyMappings().size() == 3, "Obsolete settings cannot create ghost bindings");

        camo.setKeyInternal(InputConstants.Type.MOUSE.getOrCreate(3));
        JsonObject roundTrip = KeyMappingReaderWriter.serializeBindings(KeymappingKeyHandler.getMPSKeyMappings());
        camo.setKeyInternal(InputConstants.UNKNOWN);
        vision.setKeyInternal(InputConstants.UNKNOWN);
        KeyMappingReaderWriter.INSTANCE.applySettings(roundTrip);
        check(camo.getKey().getType() == InputConstants.Type.MOUSE && camo.getKey().getValue() == 3, "Mouse binding survives round-trip");
        check(vision.getKey().getValue() == 78 && vision.showOnHud, "Keyboard binding survives round-trip");
        check(roundTrip.size() == 4, "Every binding is serialized once");
        KeyMapping.resetMapping();
        KeyMappingReaderWriter.INSTANCE.writeOutKeybindSetings();
        camo.setKeyInternal(InputConstants.UNKNOWN);
        vision.setKeyInternal(InputConstants.UNKNOWN);
        vision.showOnHud = false;
        KeyMappingReaderWriter.INSTANCE.readInKeybinds();
        check(vision.getKey().getValue() == 78 && vision.showOnHud, "Disk reload restores keyboard/HUD settings");
        check(camo.getKey().getType() == InputConstants.Type.MOUSE, "Disk reload preserves mouse input type");

        camo.setKeyInternal(InputConstants.Type.SCANCODE.getOrCreate(42));
        JsonObject scancode = KeyMappingReaderWriter.serializeBindings(KeymappingKeyHandler.getMPSKeyMappings());
        camo.setKeyInternal(InputConstants.UNKNOWN);
        KeyMappingReaderWriter.INSTANCE.applySettings(scancode);
        check(camo.getKey().getType() == InputConstants.Type.SCANCODE && camo.getKey().getValue() == 42,
                "Scancode binding survives round-trip");

        JsonObject damaged = saved.deepCopy();
        damaged.getAsJsonObject(vision.getName()).addProperty("registryName", "mpsremodularized:active_camouflage");
        damaged.getAsJsonObject(camo.getName()).addProperty("defaultKey", 66);
        KeyMappingReaderWriter.INSTANCE.applySettings(damaged);
        check(vision.getKey().getValue() == 78, "Mismatched ID cannot change a mapping");
        check(camo.getKey().getValue() == 66, "Invalid entry does not block other settings");
        JsonObject legacy = new JsonObject();
        legacy.addProperty(vision.getName(), false);
        KeyMappingReaderWriter.INSTANCE.applySettings(legacy);
        check(vision.showOnHud && vision.getKey().getValue() == 78, "Legacy HUD-only JSON is ignored without migration");
        JsonObject unsupported = saved.deepCopy();
        unsupported.addProperty("formatVersion", 1);
        KeyMappingReaderWriter.INSTANCE.applySettings(unsupported);
        check(camo.getKey().getValue() == 66, "Unsupported JSON format cannot change current bindings");
        Files.delete(file);
        Files.writeString(file.resolveSibling("mpsremodularized-keybinds.cfg"), "78:0:false:false:false\nnight_vision~\n");
        KeyMappingReaderWriter.INSTANCE.readInKeybinds();
        check(vision.showOnHud && vision.getKey().getValue() == 78, "Legacy CFG is not read when current JSON is absent");

        InputConstants.Key oldKey = vision.getKey();
        InputConstants.Key newKey = InputConstants.Type.KEYSYM.getOrCreate(86);
        vision.setKey(newKey);
        check(lookupContains(vision, newKey, KeyModifier.NONE), "Direct Controls remap updates native input lookup immediately");
        check(!lookupContains(vision, oldKey, KeyModifier.NONE), "Old Controls key cannot still dispatch this module");
        InputConstants.Key modifiedKey = InputConstants.Type.KEYSYM.getOrCreate(77);
        camo.setKeyModifierAndCode(KeyModifier.SHIFT, modifiedKey);
        check(lookupContains(camo, modifiedKey, KeyModifier.SHIFT), "Tinker modifier remap enters the correct lookup bucket");
        check(!lookupContains(camo, InputConstants.Type.KEYSYM.getOrCreate(66), KeyModifier.NONE), "Tinker remap removes the former key");
        JsonObject modifiers = KeyMappingReaderWriter.serializeBindings(KeymappingKeyHandler.getMPSKeyMappings());
        camo.setKeyInternal(InputConstants.UNKNOWN);
        KeyMappingReaderWriter.INSTANCE.applySettings(modifiers);
        check(camo.getKey().equals(modifiedKey) && camo.getKeyModifier() == KeyModifier.SHIFT, "Modifier survives current JSON round-trip");
        check(!camo.showOnHud, "Remapping preserves an explicitly hidden HUD preference");
        camo.setKeyModifierAndCode(KeyModifier.CONTROL, InputConstants.Type.MOUSE.getOrCreate(4));
        check(lookupContains(camo, camo.getKey(), KeyModifier.CONTROL), "Mouse/modifier remap uses the native lookup");
        KeyMappingReaderWriter.INSTANCE.writeOutKeybindSetings();
        camo.setKeyInternal(InputConstants.UNKNOWN);
        KeyMappingReaderWriter.INSTANCE.readInKeybinds();
        check(camo.getKey().getType() == InputConstants.Type.MOUSE && camo.getKeyModifier() == KeyModifier.CONTROL,
                "Mouse plus modifier survives disk reload");

        // Native queued clicks, without pretending to synthesize physical GLFW input in a headless JVM.
        var clicks = KeyMapping.class.getDeclaredField("clickCount");
        clicks.setAccessible(true);
        var dispatched = new ArrayList<MPSKeyMapping>();
        clicks.setInt(vision, 2);
        clicks.setInt(camo, 1);
        KeymappingKeyHandler.consumeModuleClicks(true, dispatched::add);
        check(dispatched.size() == 3 && dispatched.contains(camo), "One dispatch per queued keyboard or mouse click");
        KeymappingKeyHandler.consumeModuleClicks(true, dispatched::add);
        check(dispatched.size() == 3, "Already consumed clicks cannot toggle twice");
        clicks.setInt(camo, 1);
        KeymappingKeyHandler.consumeModuleClicks(false, dispatched::add);
        KeymappingKeyHandler.consumeModuleClicks(true, dispatched::add);
        check(dispatched.size() == 3, "Menu/unfocused clicks are discarded rather than replayed on return");
        var directions = new ArrayList<Integer>();
        clicks.setInt(KeymappingKeyHandler.cycleToolForward, 2);
        clicks.setInt(KeymappingKeyHandler.cycleToolBackward, 1);
        KeymappingKeyHandler.consumeModeClicks(true, directions::add);
        check(directions.equals(java.util.List.of(1, -1, -1)), "Mode keys dispatch each queued keyboard/mouse click once");
        KeymappingKeyHandler.consumeModeClicks(true, directions::add);
        check(directions.size() == 3, "Consumed mode clicks cannot replay");
        clicks.setInt(KeymappingKeyHandler.cycleToolForward, 1);
        KeymappingKeyHandler.consumeModeClicks(false, directions::add);
        KeymappingKeyHandler.consumeModeClicks(true, directions::add);
        check(directions.size() == 3, "Menu/unfocused mode clicks are discarded");
        var movement = new mpsremodularized.common.capabilities.player.keystates.PlayerKeyStateStorage();
        var inputs = new KeyMapping[6];
        for (int i = 0; i < inputs.length; i++) inputs[i] = new KeyMapping("test.movement." + i, 65 + i, "test");
        inputs[4].setKey(InputConstants.Type.MOUSE.getOrCreate(2));
        check(KeymappingKeyHandler.pollMovementKeys(movement, true, inputs) == null, "Idle input does not send a packet every tick");
        for (int i = 0; i < 6; i++) {
            inputs[i].setDown(true);
            Byte packed = KeymappingKeyHandler.pollMovementKeys(movement, true, inputs);
            check(packed != null && (packed & 255) == (1 << i), "Polling catches held mapping " + i + " without a keyboard event");
            inputs[i].setDown(false);
            check(KeymappingKeyHandler.pollMovementKeys(movement, true, inputs) == (byte) 0, "Polling catches released mapping " + i);
        }
        inputs[0].setDown(true); inputs[5].setDown(true);
        KeymappingKeyHandler.pollMovementKeys(movement, true, inputs);
        check(KeymappingKeyHandler.pollMovementKeys(movement, false, inputs) == (byte) 0,
                "Menus, lost focus and death immediately publish all released keys");
        check(!movement.getForwardKeyState() && !movement.getJumpKeyState(), "Blocked gameplay clears local thrust input");
        check(KeymappingKeyHandler.pollMovementKeys(movement, false, inputs) == null, "Blocked idle input sends no redundant packets");
        for (var input : inputs) input.setDown(false);
        for (int packed = 0; packed < 256; packed++) {
            boolean[] unpacked = mpsremodularized.common.utils.MathUtils.byteToBooleanArray((byte) packed);
            check((mpsremodularized.common.utils.MathUtils.boolArrayToByte(unpacked) & 255) == packed,
                    "Every input byte round-trips independent bits: " + packed);
        }
        System.out.println("Keybind settings regression checks passed: " + checks);
    }

    @SuppressWarnings("unchecked")
    private static boolean lookupContains(MPSKeyMapping mapping, InputConstants.Key key, KeyModifier modifier) throws Exception {
        var field = KeyMappingLookup.class.getDeclaredField("map");
        field.setAccessible(true);
        var buckets = (Map<KeyModifier, Map<InputConstants.Key, Collection<KeyMapping>>>) field.get(null);
        return buckets.get(modifier).getOrDefault(key, java.util.List.of()).contains(mapping);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
