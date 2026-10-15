package mpsremodularized.common.capabilities.module.toggleable;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import net.minecraft.world.item.ItemStack;

public class ToggleableModule extends PowerModule implements IToggleableModule {
    boolean online;

    public ToggleableModule(ItemStack module, ModuleCategory category, ModuleTarget target) {
        super(module, category, target);
    }

    public ToggleableModule(ItemStack module, ModuleCategory category, ModuleTarget target, boolean isAllowed) {
        super(module, category, target, isAllowed);
    }
}
