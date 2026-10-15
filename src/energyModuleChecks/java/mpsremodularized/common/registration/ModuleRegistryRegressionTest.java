package mpsremodularized.common.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import mpsremodularized.common.capabilities.module.toggleable.IToggleableModule;
import mpsremodularized.common.capabilities.module.toggleable.ToggleableModule;

public final class ModuleRegistryRegressionTest {
    private static int checks;

    public static void main(String[] args) {
        ModuleRegistry registry = new ModuleRegistry();
        var id = ResourceLocation.parse("test:toggle");
        var toggle = registry.register(id, () -> { throw new AssertionError("Resolved item during discovery"); },
                ToggleableModule.class, stack -> { throw new AssertionError("Created capability during discovery"); }, 2);
        var ordinary = registry.register(ResourceLocation.parse("test:ordinary"), () -> null,
                PowerModule.class, stack -> new PowerModule(stack, ModuleCategory.SPECIAL, ModuleTarget.ALLITEMS), -1);
        check(toggle.isToggleable() && toggle.supports(IToggleableModule.class), "Derive toggle support from type");
        check(!ordinary.isToggleable(), "Ordinary modules have no toggle binding");
        check(ordinary.createCapability(null).getTier() == -1, "Unchanged natural/default tier");
        fails(IllegalArgumentException.class, () -> registry.register(id, () -> null, PowerModule.class,
                stack -> null, -1), "Duplicate IDs rejected");
        var sorted = registry.freeze();
        check(sorted.size() == 2 && sorted.getFirst() == ordinary, "Stable ID order");
        check(registry.freeze() == sorted, "Freeze is idempotent");
        fails(UnsupportedOperationException.class, () -> sorted.clear(), "Immutable definitions");
        fails(IllegalStateException.class, () -> registry.register(ResourceLocation.parse("test:late"), () -> null,
                PowerModule.class, stack -> null, -1), "Late definitions rejected");

        var items = DeferredRegister.createItems("test");
        ModuleRegistry mismatch = new ModuleRegistry();
        fails(IllegalArgumentException.class, () -> mismatch.registerItem(items, ResourceLocation.parse("other:wrong"),
                () -> new Item(new Item.Properties()), PowerModule.class, stack -> null, -1), "Wrong namespace rejected");
        fails(IllegalArgumentException.class, () -> mismatch.register(ResourceLocation.parse("test:invalid"), () -> null,
                PowerModule.class, stack -> null, 0), "Invalid tier rejected");
        var tiered = mismatch.register(ResourceLocation.parse("test:tiered"), () -> null,
                PowerModule.class, stack -> new PowerModule(stack, ModuleCategory.SPECIAL, ModuleTarget.ALLITEMS), 4);
        check(tiered.createCapability(null).getTier() == 4, "Minimum tier applied by factory");
        check(tiered.createCapability(null) != tiered.createCapability(null), "Capabilities are not cached across stacks");
        var wrongType = mismatch.register(ResourceLocation.parse("test:wrong_type"), () -> null,
                PowerModule.class, stack -> new ToggleableModule(stack, ModuleCategory.SPECIAL, ModuleTarget.ALLITEMS), -1);
        fails(IllegalStateException.class, () -> wrongType.createCapability(null), "Metadata/factory disagreement rejected");
        var nullFactory = mismatch.register(ResourceLocation.parse("test:null"), () -> null, PowerModule.class, stack -> null, -1);
        fails(NullPointerException.class, () -> nullFactory.createCapability(null), "Null factory output rejected");
        System.out.println("Module registry regression checks passed: " + checks);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void fails(Class<? extends Throwable> type, Runnable action, String message) {
        checks++;
        try {
            action.run();
        } catch (Throwable failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError(message, failure);
        }
        throw new AssertionError(message);
    }
}
