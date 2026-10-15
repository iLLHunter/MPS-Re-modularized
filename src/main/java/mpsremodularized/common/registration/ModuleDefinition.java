package mpsremodularized.common.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import mpsremodularized.common.capabilities.module.toggleable.IToggleableModule;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** Capability metadata is available without resolving the item or constructing its capability. */
public record ModuleDefinition<P extends PowerModule>(
        ResourceLocation id,
        Supplier<? extends Item> item,
        Class<P> capabilityType,
        Function<ItemStack, P> factory,
        int minimumTierOverride) {

    public ModuleDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(capabilityType, "capabilityType");
        Objects.requireNonNull(factory, "factory");
        if (minimumTierOverride != -1 && minimumTierOverride < 1) {
            throw new IllegalArgumentException("Invalid minimum tier for " + id);
        }
    }

    public boolean supports(Class<?> capabilityInterface) {
        return capabilityInterface.isAssignableFrom(capabilityType);
    }

    public boolean isToggleable() {
        return supports(IToggleableModule.class);
    }

    public P createCapability(ItemStack stack) {
        P capability = Objects.requireNonNull(factory.apply(stack), "Module factory returned null for " + id);
        if (capability.getClass() != capabilityType) {
            throw new IllegalStateException("Module factory type disagrees with metadata for " + id);
        }
        if (minimumTierOverride != -1) {
            capability.withMinimumTier(minimumTierOverride);
        }
        return capability;
    }
}
