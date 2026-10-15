package mpsremodularized.datagen.common.recipes;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import mpsremodularized.common.compat.CompatibleComponent;
import mpsremodularized.common.compat.CompatibleCraftingRecipe;
import mpsremodularized.common.compat.CompatibleMod;
import mpsremodularized.common.compat.CompatibilityPolicy;
import mpsremodularized.common.compat.CompatibilityRecipes;
import mpsremodularized.common.recipe.ingredients.ComponentIngredient;
import mpsremodularized.common.registration.MPSDataComponents;
import mpsremodularized.common.registration.MPSItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;

/** Bootstrapped policy, actual recipe codec, network ingredient and component-state checks. */
public final class CompatibilityValidation extends MPSRecipeGenerator {
    private static int checks;

    private CompatibilityValidation(HolderLookup.Provider registries) {
        super(new PackOutput(Path.of("build", "compatibility-validation")), CompletableFuture.completedFuture(registries));
    }

    public static void validate(HolderLookup.Provider registries) {
        checks = 0;
        Map<ResourceLocation, Item> fixture = new HashMap<>();
        var uniqueIds = java.util.stream.Stream.concat(java.util.Arrays.stream(CompatibleComponent.values())
                .flatMap(component -> component.alternatives.stream()), java.util.Arrays.stream(CompatibleCraftingRecipe.values())
                .flatMap(recipe -> recipe.inputs.stream())).map(CompatibleComponent.Alternative::itemId).distinct().toList();
        var fixtureItems = BuiltInRegistries.ITEM.stream().filter(item -> item != Items.AIR).limit(uniqueIds.size()).toList();
        for (int index = 0; index < uniqueIds.size(); index++) fixture.put(uniqueIds.get(index), fixtureItems.get(index));
        var allMods = EnumSet.allOf(CompatibleMod.class);
        var all = CompatibilityPolicy.resolve(false, allMods, ignored -> true, id -> Optional.ofNullable(fixture.get(id)));
        check(uniqueIds.size() == 59, "All 55 direct alternatives and four recipe materials are declared");
        check(CompatibleComponent.CIRCUIT_4.alternatives.stream().noneMatch(a -> a.mod() == CompatibleMod.AE2), "Base AE2 has no invented quantum processor");
        check(all.foreignItems().values().stream().mapToInt(List::size).sum() == 55, "Enabled integrations combine every alternative");

        // Native-tool/armor-only switches do not affect component policy. Enumerate every
        // material-provider combination and prove the irrelevant projection is invariant.
        var materialMods = java.util.stream.Stream.concat(java.util.Arrays.stream(CompatibleComponent.values())
                .flatMap(component -> component.alternatives.stream()), java.util.Arrays.stream(CompatibleCraftingRecipe.values())
                .flatMap(recipe -> recipe.inputs.stream())).map(CompatibleComponent.Alternative::mod).distinct().toList();
        var nativeOnlyMods = EnumSet.allOf(CompatibleMod.class); nativeOnlyMods.removeAll(materialMods);
        for (int mask = 0; mask < 1 << materialMods.size(); mask++) {
            var enabled = EnumSet.noneOf(CompatibleMod.class);
            for (int bit = 0; bit < materialMods.size(); bit++) if ((mask & 1 << bit) != 0) enabled.add(materialMods.get(bit));
            var policy = CompatibilityPolicy.resolve(false, enabled, ignored -> true, id -> Optional.ofNullable(fixture.get(id)));
            var withNative = EnumSet.copyOf(enabled); withNative.addAll(nativeOnlyMods);
            check(policy.equals(CompatibilityPolicy.resolve(false, withNative, ignored -> true, id -> Optional.ofNullable(fixture.get(id)))),
                    "Native-only switches leave every material combination unchanged");
            for (CompatibleComponent component : CompatibleComponent.values()) {
                var expected = component.alternatives.stream().filter(a -> enabled.contains(a.mod())).map(a -> fixture.get(a.itemId())).toList();
                check(policy.acceptedItems(component, true).equals(expected), "Exact alternatives for " + enabled + " / " + component);
                var ingredient = new ComponentIngredient(component, false, policy);
                check(ingredient.test(new ItemStack(component.nativeItem.get())), "Existing native components remain usable");
                for (var alternative : component.alternatives) {
                    check(ingredient.test(new ItemStack(fixture.get(alternative.itemId()))) == enabled.contains(alternative.mod()), "Individual integration switch gates the ingredient");
                }
            }
            for (var recipe : CompatibleCraftingRecipe.values()) {
                var expected = recipe.inputs.stream().filter(input -> enabled.contains(input.mod())).map(input -> fixture.get(input.itemId())).toList();
                check(policy.inputs(recipe).equals(expected), "All enabled materials remain available: " + recipe);
                check(CompatibilityRecipes.isEnabled(recipe.id(), policy, Set.of()) == !expected.isEmpty(), "Material recipe follows its provider switches");
            }
        }
        var absent = CompatibilityPolicy.resolve(false, allMods, ignored -> false, id -> Optional.ofNullable(fixture.get(id)));
        check(absent.foreignItems().values().stream().allMatch(List::isEmpty), "Installed check cannot be bypassed by an item lookup");
        var missing = CompatibilityPolicy.resolve(false, allMods, ignored -> true, ignored -> Optional.empty());
        for (CompatibleComponent component : CompatibleComponent.values()) {
            check(CompatibilityRecipes.isEnabled(component.nativeId(), missing, Set.of()), "Missing registry items retain native recipes");
            check(CompatibilityRecipes.isEnabled(component.nativeId(), absent, Set.of()), "Standalone MPS retains native recipes with the global switch false");
        }

        List<RecipeHolder<?>> recipes = new ArrayList<>();
        new CompatibilityValidation(registries).buildRecipes(new RecipeOutput() {
            @Override public Advancement.Builder advancement() { return Advancement.Builder.recipeAdvancement(); }
            @Override public void accept(ResourceLocation id, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
                recipes.add(new RecipeHolder<>(id, recipe));
            }
        });
        RecipeManager manager = new RecipeManager(registries);
        manager.replaceRecipes(recipes);
        CompatibilityRecipes.apply(manager, registries, all);
        for (var route : CompatibleCraftingRecipe.values()) {
            var recipe = (net.minecraft.world.item.crafting.CraftingRecipe) manager.byKey(route.id()).orElseThrow().value();
            var items = all.inputs(route);
            for (Item item : items) {
                var grid = route == CompatibleCraftingRecipe.RUBBER_HOSE
                        ? CraftingInput.of(3, 3, List.of(new ItemStack(Items.WHITE_WOOL), new ItemStack(Items.WHITE_WOOL), new ItemStack(Items.WHITE_WOOL),
                                new ItemStack(item), ItemStack.EMPTY, new ItemStack(items.getLast()), new ItemStack(Items.WHITE_WOOL), new ItemStack(Items.WHITE_WOOL), new ItemStack(Items.WHITE_WOOL)))
                        : CraftingInput.of(2, 2, List.of(new ItemStack(item), new ItemStack(item), new ItemStack(item), new ItemStack(item)));
                check(recipe.matches(grid, null), "Material recipe accepts each enabled material or mixed rubber sides");
                var result = recipe.assemble(grid, registries);
                check(result.is(route.output.get()) && result.getCount() == 1, "Requested material count creates one item/module");
                if (route != CompatibleCraftingRecipe.RUBBER_HOSE) {
                    var scattered = CraftingInput.of(3, 3, List.of(new ItemStack(item), ItemStack.EMPTY, new ItemStack(item), ItemStack.EMPTY,
                            ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(item), ItemStack.EMPTY, new ItemStack(item)));
                    check(!recipe.matches(scattered, null), "Both plate recipes require a 2x2 shape");
                    check(!recipe.matches(CraftingInput.of(2, 2, List.of(new ItemStack(item), new ItemStack(item), new ItemStack(item), ItemStack.EMPTY)), null), "Three large plates cannot create a component");
                }
            }
            var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY),
                    net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE);
            try {
                var custom = recipe.getIngredients().stream().filter(Ingredient::isCustom).findFirst().orElseThrow();
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, custom);
                var decoded = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                for (var item : items) check(decoded.test(new ItemStack(item)), "Material choices survive ingredient network sync");
            } finally { buffer.release(); }
        }
        for (var oldAndNew : List.of(Map.entry("component_wiring_copper", MPSItems.WIRING_STANDARD.get()),
                Map.entry("component_wiring_gold", MPSItems.WIRING_HIGH_CONDUCTIVITY.get()))) {
            var json = new com.google.gson.JsonObject();
            json.addProperty("id", "mpsremodularized:" + oldAndNew.getKey());
            json.addProperty("count", 1);
            var stack = ItemStack.CODEC.parse(registries.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE), json).getOrThrow();
            check(stack.is(oldAndNew.getValue()), "Legacy saved item ID resolves to its renamed item");
            check(!BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals(oldAndNew.getKey()), "Saved item uses the new canonical ID");
        }
        for (CompatibleComponent component : CompatibleComponent.values()) {
            check(manager.byKey(component.nativeId()).isPresent() == (component == CompatibleComponent.DIAMOND_PLATING),
                    "Large plates cover the plating module, preserving the separate diamond component fallback: " + component);
            if (!component.battery) continue;
            var recipe = (ShapelessRecipe) manager.byKey(component.conversionId()).orElseThrow().value();
            for (Item item : all.acceptedItems(component, true)) {
                for (int energy : new int[] {0, 1, 12345, Integer.MAX_VALUE}) {
                    ItemStack input = new ItemStack(item);
                    input.set(MPSDataComponents.ENERGY, energy);
                    input.set(DataComponents.CUSTOM_NAME, Component.literal("Charged foreign ingredient"));
                    var grid = CraftingInput.of(1, 1, List.of(input));
                    check(recipe.matches(grid, null), "Converter accepts any component/charge state");
                    ItemStack output = recipe.assemble(grid, registries);
                    check(output.is(component.nativeItem.get()) && output.getCount() == 1, "One foreign item creates one matching MPS battery");
                    check(output.getOrDefault(MPSDataComponents.ENERGY, -1) == 0, "Converted battery explicitly contains zero FE");
                    check(output.get(DataComponents.CUSTOM_NAME) == null, "Foreign data components are not copied");
                    check(input.getOrDefault(MPSDataComponents.ENERGY, -1) == energy, "Crafting preview does not mutate the source");
                }
            }
            var ingredient = recipe.getIngredients().getFirst();
            var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY),
                    net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE);
            try {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
                Ingredient decoded = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                check(!decoded.isCustom(), "Client receives a plain server-resolved item list");
                for (Item item : all.acceptedItems(component, true)) check(decoded.test(new ItemStack(item)), "All enabled foreign items survive ingredient network sync");
                check(!decoded.test(new ItemStack(component.nativeItem.get())), "One-way conversion never accepts an MPS battery");
            } finally { buffer.release(); }
        }
        var aeOnly = CompatibilityPolicy.resolve(false, Set.of(CompatibleMod.AE2), ignored -> true, id -> Optional.ofNullable(fixture.get(id)));
        manager.replaceRecipes(recipes);
        CompatibilityRecipes.apply(manager, registries, aeOnly);
        check(manager.byKey(CompatibleComponent.CIRCUIT_1.nativeId()).isEmpty(), "AE2 covers the first circuit recipe");
        check(manager.byKey(CompatibleComponent.CIRCUIT_4.nativeId()).isPresent(), "AE2 alone keeps the final circuit fallback");
        check(manager.byKey(CompatibleComponent.BATTERY_1.nativeId()).isPresent(), "AE2 alone keeps native battery crafting");
        check(manager.byKey(CompatibleComponent.BATTERY_1.conversionId()).isEmpty(), "Unavailable conversion recipes stay out of the recipe list");
        for (boolean keep : new boolean[] {true, false}) {
            var policy = CompatibilityPolicy.resolve(keep, allMods, ignored -> true, id -> Optional.ofNullable(fixture.get(id)));
            manager.replaceRecipes(recipes);
            CompatibilityRecipes.apply(manager, registries, policy);
            for (CompatibleComponent component : CompatibleComponent.values()) {
                check(manager.byKey(component.nativeId()).isPresent() == (keep || component == CompatibleComponent.DIAMOND_PLATING), "Global flag controls only covered native recipes");
                if (component.battery) check(manager.byKey(component.conversionId()).isPresent(), "Every enabled conversion remains available in either global mode");
            }
        }
        check(CompatibilityRecipes.isEnabled(CompatibleComponent.BATTERY_1.nativeId(), all, Set.of()), "Removing a conversion via datapack restores the native battery fallback");
        var miOnly = CompatibilityPolicy.resolve(false, Set.of(CompatibleMod.MODERN_INDUSTRIALIZATION), ignored -> true, id -> Optional.ofNullable(fixture.get(id)));
        for (var recipe : CompatibleCraftingRecipe.values()) {
            check(CompatibilityRecipes.isEnabled(recipe.outputId(), miOnly, Set.of()), "Removing material conversion restores its native fallback");
            check(!CompatibilityRecipes.isEnabled(recipe.outputId(), miOnly, Set.of(recipe.id())), "Available MI conversion replaces covered native recipe");
        }
        var missingGenerators = CompatibilityPolicy.resolve(false, Set.of(CompatibleMod.MEKANISM), namespace -> namespace.equals("mekanism"), id -> Optional.ofNullable(fixture.get(id)));
        check(!missingGenerators.hasAlternative(CompatibleComponent.SOLAR_PANEL), "Base Mekanism alone cannot supply the Generators solar item");
        System.out.println("Stage 1 compatibility policy/recipe/state checks passed: " + checks);
    }

    private static void check(boolean success, String message) {
        checks++;
        if (!success) throw new AssertionError(message);
    }
}
