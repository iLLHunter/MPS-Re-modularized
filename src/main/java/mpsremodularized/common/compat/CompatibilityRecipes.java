package mpsremodularized.common.compat;

import com.mojang.serialization.JsonOps;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import mpsremodularized.common.base.MPSLogger;
import mpsremodularized.common.config.MPSCommonConfig;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Server configs load after the initial recipes. Re-resolve them before login and after /reload. */
@EventBusSubscriber(modid = MPSConstants.MOD_ID)
public final class CompatibilityRecipes {
    private static volatile CompatibilityPolicy worldPolicy;
    private static final ThreadLocal<CompatibilityPolicy> DECODING_POLICY = new ThreadLocal<>();

    private CompatibilityRecipes() {}

    public static CompatibilityPolicy policy() {
        var scoped = DECODING_POLICY.get();
        // Initial data loading precedes the server config. Keep installed ingredients visible
        // until the final world policy is applied, rather than caching empty JEI ingredients.
        return scoped != null ? scoped : worldPolicy != null ? worldPolicy : CompatibilityPolicy.resolve(true,
                java.util.EnumSet.allOf(CompatibleMod.class), id -> ModList.get().isLoaded(id),
                id -> BuiltInRegistries.ITEM.getOptional(id).filter(item -> item != Items.AIR));
    }

    @SubscribeEvent
    public static void onServerStarting(ServerAboutToStartEvent event) {
        var settings = MPSCommonConfig.COMPATIBILITY_SETTINGS;
        worldPolicy = CompatibilityPolicy.resolve(settings.keepNativeRecipes(), settings.enabledMods(),
                modId -> ModList.get().isLoaded(modId), id -> BuiltInRegistries.ITEM.getOptional(id).filter(item -> item != Items.AIR));
        MachineCompatibilityRecipes.install(event.getServer().getRecipeManager(), event.getServer().registryAccess(),
                event.getServer().getResourceManager(), worldPolicy);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        worldPolicy = null;
        MachineCompatibilityRecipes.clear();
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        var manager = event.getServerResources().getRecipeManager();
        var registries = event.getRegistryAccess();
        event.addListener(new SimplePreparableReloadListener<Boolean>() {
            @Override
            protected Boolean prepare(ResourceManager resources, ProfilerFiller profiler) { return true; }

            @Override
            protected void apply(Boolean prepared, ResourceManager resources, ProfilerFiller profiler) {
                var policy = worldPolicy;
                if (policy != null) MachineCompatibilityRecipes.install(manager, registries, resources, policy);
            }
        });
    }

    public static void apply(RecipeManager manager, HolderLookup.Provider registries, CompatibilityPolicy policy) {
        var input = java.util.List.copyOf(manager.getRecipes());
        Set<ResourceLocation> existing = input.stream().map(RecipeHolder::id).collect(Collectors.toSet());
        var retained = new ArrayList<RecipeHolder<?>>();
        int removed = 0;
        for (RecipeHolder<?> holder : input) {
            if (holder.value() instanceof mpsremodularized.common.recipe.NativeModuleRecipe nativeRecipe
                    && nativeRecipe.getResultItem(registries).getItem() instanceof NativeModuleItem nativeItem
                    && !nativeItem.available()) {
                removed++;
                continue;
            }
            if (!isEnabled(holder.id(), policy, existing)) {
                removed++;
                continue;
            }
            if (holder.id().getNamespace().equals(MPSConstants.MOD_ID)) {
                var ops = registries.createSerializationContext(JsonOps.INSTANCE);
                var json = Recipe.CODEC.encodeStart(ops, holder.value()).getOrThrow();
                if (containsComponentIngredient(json)) {
                    Recipe<?> resolved = withPolicy(policy, () -> Recipe.CODEC.parse(ops, json).getOrThrow());
                    retained.add(new RecipeHolder<>(holder.id(), resolved));
                } else {
                    retained.add(holder);
                }
            } else {
                retained.add(holder);
            }
        }
        manager.replaceRecipes(retained);
        MPSLogger.logger.info("MPS compatibility resolved {} component groups; retained {} recipes and removed {} unavailable/native component recipes",
                policy.foreignItems().values().stream().filter(items -> !items.isEmpty()).count(), retained.size(), removed);
    }

    private static boolean containsComponentIngredient(JsonElement json) {
        if (json.isJsonObject()) {
            var object = json.getAsJsonObject();
            if (object.has("type") && object.get("type").isJsonPrimitive()
                    && (object.get("type").getAsString().equals(MPSConstants.MOD_ID + ":component")
                    || object.get("type").getAsString().equals(MPSConstants.MOD_ID + ":compatible_crafting"))) return true;
            return object.entrySet().stream().anyMatch(entry -> containsComponentIngredient(entry.getValue()));
        }
        return json.isJsonArray() && java.util.stream.StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                .anyMatch(CompatibilityRecipes::containsComponentIngredient);
    }

    public static boolean isEnabled(ResourceLocation recipeId, CompatibilityPolicy policy, Set<ResourceLocation> existingRecipes) {
        for (var recipe : CompatibleCraftingRecipe.values()) {
            if (recipeId.equals(recipe.id())) return policy.hasCraftingAlternative(recipe);
            if (recipeId.equals(recipe.outputId()) && !policy.keepNativeRecipes()
                    && policy.hasCraftingAlternative(recipe) && existingRecipes.contains(recipe.id())) return false;
        }
        if (!policy.keepNativeRecipes() && MachineCompatibilityRecipes.covers(recipeId)) return false;
        for (CompatibleComponent component : CompatibleComponent.values()) {
            if (recipeId.equals(component.conversionId())) return component.battery && policy.hasAlternative(component);
            if (recipeId.equals(component.nativeId())) {
                boolean replacementAvailable = policy.hasAlternative(component)
                        && (!component.battery || existingRecipes.contains(component.conversionId()));
                return policy.keepNativeRecipes() || !replacementAvailable;
            }
        }
        return true;
    }

    public static <T> T withPolicy(CompatibilityPolicy policy, Supplier<T> action) {
        var previous = DECODING_POLICY.get();
        DECODING_POLICY.set(policy);
        try { return action.get(); }
        finally {
            if (previous == null) DECODING_POLICY.remove();
            else DECODING_POLICY.set(previous);
        }
    }
}
