package mpsremodularized.common.item.module.tool.misc;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import mpsremodularized.common.compat.GascraftIntegration;
import mpsremodularized.common.compat.NativeModuleItem;
import mpsremodularized.common.registration.MPSCapabilities;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class GascraftLauncherModule extends RightClickModule implements mpsremodularized.common.capabilities.module.toggleable.IToggleableModule {
    public GascraftLauncherModule(ItemStack module) { super(module, ModuleCategory.WEAPON, ModuleTarget.TOOLONLY); }
    @Override public boolean isAllowed() { return ((NativeModuleItem) getModule().getItem()).available(); }
    @Override public InteractionResultHolder<ItemStack> use(ItemStack fist, Level level, Player player, InteractionHand hand) {
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (hand != InteractionHand.MAIN_HAND || player.getMainHandItem() != fist || host == null
                || !host.hasActiveModule(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(getModule().getItem()))
                || !host.isModuleOperational(this)) {
            return InteractionResultHolder.fail(fist);
        }
        int index = host.getActiveMode();
        var module = host.getStackInSlot(index);
        ItemStack donor = ((NativeModuleItem) module.getItem()).source(module, level.registryAccess());
        if (donor.isEmpty() || !GascraftIntegration.launcherAdapterReady()) return InteractionResultHolder.fail(fist);
        if (level.isClientSide()) return InteractionResultHolder.success(fist);
        // Gascraft validates ammunition/projectile creation and pays its upgraded native buffer cost.
        try {
            var result = GascraftIntegration.fire(donor, level, player, hand);
            return new InteractionResultHolder<>(result.getResult(), fist);
        } finally {
            GascraftIntegration.save(fist, index, module, donor, level);
        }
    }
}
