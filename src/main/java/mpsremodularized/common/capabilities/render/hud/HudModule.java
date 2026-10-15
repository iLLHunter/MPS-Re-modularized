package mpsremodularized.common.capabilities.render.hud;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.toggleable.ToggleableModule;
import net.minecraft.world.item.ItemStack;

public class HudModule extends ToggleableModule implements IHudModule {
    public HudModule(ItemStack module, ModuleCategory category, ModuleTarget target) {
        super(module, category, target);
    }

    public HudModule(ItemStack module, ModuleCategory category, ModuleTarget target, boolean isAllowed) {
        super(module, category, target, isAllowed);
    }
}
