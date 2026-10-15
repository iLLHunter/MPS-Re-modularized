package mpsremodularized.common.capabilities.module.tickable;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Automatic modules still respect server availability and host tiers, without a user toggle. */
public class PassivePlayerTickModule extends PowerModule implements IPlayerTickModule {
    public PassivePlayerTickModule(ItemStack module, ModuleCategory category, ModuleTarget target) {
        super(module, category, target);
    }

    @Override
    public boolean onPlayerTickActive(Player player, Level level, ItemStack host, int moduleIndex) {
        return false;
    }

    @Override
    public boolean onPlayerTickInactive(Player player, Level level, ItemStack host, int moduleIndex) {
        return false;
    }
}
