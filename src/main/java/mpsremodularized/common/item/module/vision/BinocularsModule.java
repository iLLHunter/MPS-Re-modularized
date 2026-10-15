package mpsremodularized.common.item.module.vision;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.toggleable.ToggleableModule;
import mpsremodularized.common.config.module.VisionModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MachineMuse (Claire Semple)
 * Created: 1:08 AM, 4/24/13
 * <p>
 */
public class BinocularsModule extends AbstractPowerModule {
    public static class BinocularCap extends ToggleableModule {

        public BinocularCap(ItemStack module) {
            super(module, ModuleCategory.VISION, ModuleTarget.HEADONLY);
            addBaseProperty(MPSConstants.FOV, VisionModuleConfig.binocularsModuleFOVBase);
            addTradeoffProperty(MPSConstants.FIELD_OF_VIEW, MPSConstants.FOV, VisionModuleConfig.binocularsModuleFOVFieldOfViewMultiplier, "%");
        }

        @Override
        public boolean isAllowed() {
            return VisionModuleConfig.binocularsModuleIsAllowed;
        }

        @Override
        public int getTier() {
            return 2;
        }
    }
}
