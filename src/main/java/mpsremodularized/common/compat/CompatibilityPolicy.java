package mpsremodularized.common.compat;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** Immutable world policy: every installed, enabled, existing alternative is accepted. */
public record CompatibilityPolicy(boolean keepNativeRecipes, Map<CompatibleComponent, List<Item>> foreignItems,
        Map<CompatibleCraftingRecipe, List<Item>> craftingInputs) {
    public CompatibilityPolicy {
        var copy = new EnumMap<CompatibleComponent, List<Item>>(CompatibleComponent.class);
        foreignItems.forEach((component, items) -> copy.put(component, List.copyOf(items)));
        foreignItems = Map.copyOf(copy);
        var inputCopy = new EnumMap<CompatibleCraftingRecipe, List<Item>>(CompatibleCraftingRecipe.class);
        craftingInputs.forEach((recipe, items) -> inputCopy.put(recipe, List.copyOf(items)));
        craftingInputs = Map.copyOf(inputCopy);
    }

    public static CompatibilityPolicy nativeOnly() {
        return new CompatibilityPolicy(true, Map.of(), Map.of());
    }

    public static CompatibilityPolicy resolve(boolean keepNative, Set<CompatibleMod> enabled,
            Predicate<String> installed, Function<ResourceLocation, Optional<Item>> lookup) {
        var accepted = new EnumMap<CompatibleComponent, List<Item>>(CompatibleComponent.class);
        for (CompatibleComponent component : CompatibleComponent.values()) {
            var items = new LinkedHashSet<Item>();
            for (var alternative : component.alternatives) {
                if (enabled.contains(alternative.mod()) && installed.test(alternative.itemId().getNamespace())) {
                    lookup.apply(alternative.itemId()).ifPresent(items::add);
                }
            }
            accepted.put(component, List.copyOf(items));
        }
        var inputs = new EnumMap<CompatibleCraftingRecipe, List<Item>>(CompatibleCraftingRecipe.class);
        for (var recipe : CompatibleCraftingRecipe.values()) {
            var items = new LinkedHashSet<Item>();
            for (var input : recipe.inputs) {
                if (enabled.contains(input.mod()) && installed.test(input.itemId().getNamespace())) lookup.apply(input.itemId()).ifPresent(items::add);
            }
            inputs.put(recipe, List.copyOf(items));
        }
        return new CompatibilityPolicy(keepNative, accepted, inputs);
    }

    public List<Item> acceptedItems(CompatibleComponent component, boolean foreignOnly) {
        var foreign = foreignItems.getOrDefault(component, List.of());
        if (foreignOnly) return foreign;
        var all = new LinkedHashSet<Item>();
        all.add(component.nativeItem.get());
        all.addAll(foreign);
        return List.copyOf(all);
    }

    public boolean hasAlternative(CompatibleComponent component) {
        return !foreignItems.getOrDefault(component, List.of()).isEmpty();
    }

    public List<Item> inputs(CompatibleCraftingRecipe recipe) {
        return craftingInputs.getOrDefault(recipe, List.of());
    }

    public boolean hasCraftingAlternative(CompatibleCraftingRecipe recipe) {
        return !inputs(recipe).isEmpty();
    }
}
