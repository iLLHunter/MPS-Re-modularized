package mpsremodularized.common.item.module.cosmetic;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.toggleable.ToggleableModule;
import mpsremodularized.common.config.module.CosmeticModuleConfig;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.item.ItemStack;

public class TransparentArmorModule extends AbstractPowerModule {

    public static class TransParentArmorCap extends ToggleableModule {
        public TransParentArmorCap(ItemStack module) {
            super(module, ModuleCategory.COSMETIC, ModuleTarget.ARMORONLY);
        }

        @Override
        public int getTier() {
            return 2;
        }

        @Override
        public boolean isAllowed() {
            return CosmeticModuleConfig.isTransparentArmorAllowed;
        }
    }
}
