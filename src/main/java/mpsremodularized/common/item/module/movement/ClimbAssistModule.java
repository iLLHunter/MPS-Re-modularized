package mpsremodularized.common.item.module.movement;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.toggleable.ToggleableModule;
import mpsremodularized.common.config.module.MovementModuleConfig;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ClimbAssistModule extends AbstractPowerModule {
    public static class ClimbAssistCap extends ToggleableModule {
        public ClimbAssistCap(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MOVEMENT, ModuleTarget.LEGSONLY);
        }

        @Override
        public boolean isAllowed() {
            return MovementModuleConfig.climbAssistModuleIsAllowed;
        }
    }
}
