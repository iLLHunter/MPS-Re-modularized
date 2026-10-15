/*
 * Copyright (c) 2021. MachineMuse, Lehjr
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *      Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package mpsremodularized.client.control;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import mpsremodularized.common.base.MPSLogger;
import mpsremodularized.common.config.ConfigHelper;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyModifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

/** Persist preferences for the module key mappings registered during client initialization. */
public enum KeyMappingReaderWriter {
    INSTANCE;

    static final String formatVersionKey = "formatVersion";
    static final String registryNameKey = "registryName";
    static final String showOnHudKey = "showOnHud";
    static final String defaultKeyKey = "defaultKey";
    static final String inputKey = "key";
    static final String modifierKey = "modifier";

    File getKeyBindConfig() {
        return ConfigHelper.setupConfigFile("mpsremodularized-keybinds.json", MPSConstants.MOD_ID);
    }

    public void writeOutKeybindSetings() {
        try {
            File file = getKeyBindConfig();
            Files.createDirectories(file.toPath().getParent());
            String json = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
                    .toJson(serializeBindings(getMPSKeyBinds()));
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }
        } catch (IOException e) {
            MPSLogger.logException("Problem writing keybind settings", e);
        }
    }

    static JsonObject serializeBindings(Collection<MPSKeyMapping> bindings) {
        JsonObject settings = new JsonObject();
        settings.addProperty(formatVersionKey, 2);
        for (MPSKeyMapping binding : bindings) {
            JsonObject data = new JsonObject();
            data.addProperty(registryNameKey, binding.registryName.toString());
            data.addProperty(showOnHudKey, binding.showOnHud);
            data.addProperty(defaultKeyKey, binding.getKey().getValue());
            // The translation key preserves mouse/scancode bindings within the current JSON format.
            data.addProperty(inputKey, binding.getKey().getName());
            data.addProperty(modifierKey, binding.getKeyModifier().name());
            settings.add(binding.getName(), data);
        }
        return settings;
    }

    public Collection<MPSKeyMapping> getMPSKeyBinds() {
        return KeymappingKeyHandler.getMPSKeyMappings();
    }

    public void readInKeybinds() {
        File file = getKeyBindConfig();
        if (!file.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            applySettings(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (Exception e) {
            MPSLogger.logException("Problem reading keybind settings", e);
        }
    }

    void applySettings(JsonObject settings) {
        if (!settings.has(formatVersionKey) || settings.get(formatVersionKey).getAsInt() != 2) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : settings.entrySet()) {
            if (entry.getKey().equals(formatVersionKey)) {
                continue;
            }
            try {
                String name = entry.getKey();
                MPSKeyMapping binding = KeymappingKeyHandler.getModuleKeyMapping(name);
                if (binding == null) {
                    continue;
                }
                JsonObject data = entry.getValue().getAsJsonObject();
                ResourceLocation id = ResourceLocation.parse(data.get(registryNameKey).getAsString());
                if (!binding.registryName.equals(id)) {
                    throw new IllegalArgumentException("Saved module ID disagrees with key mapping " + name);
                }
                InputConstants.Key key = data.has(inputKey)
                        ? InputConstants.getKey(data.get(inputKey).getAsString())
                        : getInputByCode(data.get(defaultKeyKey).getAsInt());
                boolean showOnHud = data.get(showOnHudKey).getAsBoolean();
                KeyModifier modifier = data.has(modifierKey)
                        ? KeyModifier.valueOf(data.get(modifierKey).getAsString()) : KeyModifier.NONE;
                binding.setKeyInternal(key, modifier);
                binding.showOnHud = showOnHud;
            } catch (Exception e) {
                // One damaged or obsolete entry must not prevent restoring the remaining bindings.
                MPSLogger.logException("Invalid keybind setting: " + entry.getKey(), e);
            }
        }
        KeyMapping.resetMapping();
    }

    public InputConstants.Key getInputByCode(int keyCode) {
        return InputConstants.Type.KEYSYM.getOrCreate(keyCode);
    }
}
