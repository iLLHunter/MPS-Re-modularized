package mpsremodularized.common.capabilities.module;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import mpsremodularized.common.config.module.ToolModuleConfig;
import mpsremodularized.common.registration.MPSCapabilities;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PortableCraftingCapability {
    public static class RightClickie extends RightClickModule {
        public RightClickie(ItemStack module) {
            super(module, ModuleCategory.TOOL, ModuleTarget.TOOLONLY);
        }

        @Override
        public boolean isAllowed() {
            return ToolModuleConfig.craftingTableIsAllowed;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(ItemStack itemStackIn, Level level, Player playerIn, InteractionHand hand) {
            if (level.isClientSide) {
                return InteractionResultHolder.sidedSuccess(itemStackIn, level.isClientSide);
            } else {
                    SimpleMenuProvider container = new SimpleMenuProvider((id, inven, player) -> new CraftingMenu(id, inven, ContainerLevelAccess.create(level, player.blockPosition())) {

                    @Override
                    public boolean stillValid(Player player) {
                        if (player != playerIn || player.getItemInHand(hand) != itemStackIn) return false;
                        var host = MPSCapabilities.getModeChangingModularItem(itemStackIn);
                        return host != null && host.getActiveModule().is(net.minecraft.world.item.Items.CRAFTING_TABLE)
                            && host.isModuleOperational(host.getModuleCapability(host.getActiveModule()));
                    }
                }, Component.translatable("container.crafting"));
                playerIn.openMenu(container);
                playerIn.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
                return InteractionResultHolder.consume(itemStackIn);
            }
        }
    }
}
