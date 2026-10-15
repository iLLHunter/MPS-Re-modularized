package mpsremodularized.common.item.module.environmental;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import mpsremodularized.common.config.module.EnvironmentalModuleConfig;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.item.ItemStack;

public class PiglinPacificationModule extends AbstractPowerModule {
    public static class PiglinPacificationCap extends PowerModule {
        public PiglinPacificationCap(ItemStack module) {
            super(module, ModuleCategory.ENVIRONMENTAL, ModuleTarget.TORSOONLY);
        }

        @Override
        public boolean isAllowed() {
            return EnvironmentalModuleConfig.piglinPacificationIsAllowed;
        }
    }
}
