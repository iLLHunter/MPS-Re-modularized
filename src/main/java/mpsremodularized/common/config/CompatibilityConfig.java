package mpsremodularized.common.config;

import java.util.EnumMap;
import java.util.Set;
import java.util.stream.Collectors;
import mpsremodularized.common.compat.CompatibleMod;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-owned compatibility settings, captured when a world starts. */
public final class CompatibilityConfig {
    private final EnumMap<CompatibleMod, ModConfigSpec.BooleanValue> mods = new EnumMap<>(CompatibleMod.class);
    private final ModConfigSpec.BooleanValue keepNativeRecipes;

    CompatibilityConfig(ModConfigSpec.Builder builder) {
        builder.comment("Optional inter-mod compatibility. Changes take effect after restarting the server/world.")
                .push("InterModCompatibility");
        for (CompatibleMod mod : CompatibleMod.values()) {
            mods.put(mod, builder.comment("Allow " + mod.displayName + " components, MPS integration recipes, equipment modules and universal wrench behavior when installed.")
                    .translation("config.mpsremodularized." + mod.configKey)
                    .worldRestart().define(mod.configKey, true));
        }
        keepNativeRecipes = builder.comment(
                "Keep native MPS component, module and equipment recipes alongside every enabled integration.",
                "When false, hide only native recipes with an available replacement; all enabled integration routes remain available.",
                "Missing or disabled integrations never remove the fallback MPS recipe.")
                .translation("config.mpsremodularized.useMPSRecipesWithEnabledMods")
                .worldRestart().define("useMPSRecipesWithEnabledMods", true);
        builder.pop();
    }

    public Set<CompatibleMod> enabledMods() {
        return mods.entrySet().stream().filter(entry -> entry.getValue().getAsBoolean())
                .map(java.util.Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
    }

    public boolean keepNativeRecipes() {
        return keepNativeRecipes.getAsBoolean();
    }
}
