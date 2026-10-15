package mpsremodularized.common.registration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Declare modules during mod construction, alongside deferred item registration.
 * The first lifecycle consumer freezes the catalog; later declarations fail instead of missing keybinds.
 */
public final class ModuleRegistry {
    public static final ModuleRegistry INSTANCE = new ModuleRegistry();

    private final Map<ResourceLocation, ModuleDefinition<?>> definitions = new LinkedHashMap<>();
    private List<ModuleDefinition<?>> frozenDefinitions;

    public synchronized <P extends PowerModule> ModuleDefinition<P> register(
            ResourceLocation id, Supplier<? extends Item> item, Class<P> capabilityType,
            Function<ItemStack, P> factory, int minimumTierOverride) {
        checkRegistration(id);
        var definition = new ModuleDefinition<>(id, item, capabilityType, factory, minimumTierOverride);
        definitions.put(id, definition);
        return definition;
    }

    public synchronized <I extends Item, P extends PowerModule> DeferredHolder<Item, I> registerItem(
            DeferredRegister.Items items, ResourceLocation id, Supplier<I> itemFactory, Class<P> capabilityType,
            Function<ItemStack, P> capabilityFactory, int minimumTierOverride) {
        checkRegistration(id);
        if (!items.getNamespace().equals(id.getNamespace())) {
            throw new IllegalArgumentException("Item registry namespace disagrees with module " + id);
        }
        // Validate metadata before modifying either registry. Do not resolve the deferred item here.
        var definition = new ModuleDefinition<>(id, itemFactory, capabilityType, capabilityFactory, minimumTierOverride);
        DeferredHolder<Item, I> item = items.register(id.getPath(), itemFactory);
        definitions.put(id, new ModuleDefinition<>(id, item, definition.capabilityType(), definition.factory(), minimumTierOverride));
        return item;
    }

    private void checkRegistration(ResourceLocation id) {
        if (frozenDefinitions != null) {
            throw new IllegalStateException("Declare module " + id + " during mod construction, before module discovery");
        }
        if (definitions.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate module definition: " + id);
        }
    }

    public synchronized List<ModuleDefinition<?>> freeze() {
        if (frozenDefinitions == null) {
            // Mod constructors can run in parallel; sort so keybind/config ordering is stable.
            frozenDefinitions = definitions.values().stream()
                    .sorted(Comparator.comparing(definition -> definition.id().toString())).toList();
        }
        return frozenDefinitions;
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (ModuleDefinition<?> definition : freeze()) {
            Item item = definition.item().get();
            if (!definition.id().equals(BuiltInRegistries.ITEM.getKey(item))) {
                throw new IllegalStateException("Module definition does not resolve to its registered item: " + definition.id());
            }
            event.registerItem(MPSCapabilities.Module.POWER_MODULE,
                    (stack, context) -> definition.createCapability(stack), item);
        }
    }
}
