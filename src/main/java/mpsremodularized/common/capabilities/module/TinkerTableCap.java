package mpsremodularized.common.capabilities.module;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import mpsremodularized.common.config.module.ToolModuleConfig;
import mpsremodularized.common.container.InstallationAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TinkerTableCap extends RightClickModule {

    public TinkerTableCap(ItemStack module)  {
        super(module, ModuleCategory.TOOL, ModuleTarget.TOOLONLY);
    }

    @Override
    public boolean isAllowed() {
        return ToolModuleConfig.tinkerTableIsAllowed;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack itemStackIn, Level level, Player playerIn, InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(itemStackIn, level.isClientSide);
        } else {
            return InstallationAccess.openPortableTable(playerIn, hand)
                    ? InteractionResultHolder.consume(itemStackIn) : InteractionResultHolder.fail(itemStackIn);
        }
    }
}
