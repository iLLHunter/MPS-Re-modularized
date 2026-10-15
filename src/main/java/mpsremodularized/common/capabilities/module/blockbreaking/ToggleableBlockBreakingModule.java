package mpsremodularized.common.capabilities.module.blockbreaking;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.toggleable.ToggleableModule;
import net.minecraft.world.item.ItemStack;

public abstract class ToggleableBlockBreakingModule extends ToggleableModule implements IBlockBreakingModule {
    public ToggleableBlockBreakingModule(ItemStack module, ModuleCategory category, ModuleTarget target) {
        super(module, category, target);
    }
}
