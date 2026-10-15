package mpsremodularized.common.item.module.movement;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.toggleable.ToggleableModule;
import mpsremodularized.common.config.module.MovementModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class FlightControlModule extends AbstractPowerModule {
    public static class Toggler extends ToggleableModule {
        public Toggler(@Nonnull ItemStack module) {
            super(module, ModuleCategory.MOVEMENT, ModuleTarget.HEADONLY);
            addTradeoffProperty(MPSConstants.VERTICALITY, MPSConstants.FLIGHT_VERTICALITY, 1.0F, "%");
        }

        @Override
        public boolean isAllowed() {
            return MovementModuleConfig.flightControlModuleIsAllowed;
        }
    }
}
