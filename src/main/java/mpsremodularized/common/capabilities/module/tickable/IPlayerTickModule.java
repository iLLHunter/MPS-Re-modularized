package mpsremodularized.common.capabilities.module.tickable;

import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IPlayerTickModule extends IPowerModule {
    boolean onPlayerTickActive(Player player, Level level, ItemStack host, int moduleIndex);

    boolean onPlayerTickInactive(Player player, Level level, ItemStack host, int moduleIndex);
}
