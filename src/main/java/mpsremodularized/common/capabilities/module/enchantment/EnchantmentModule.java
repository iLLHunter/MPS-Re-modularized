package mpsremodularized.common.capabilities.module.enchantment;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PlayerTickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.TagUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Uses player tick to apply/update enchantments or remove them when tuned off.
 * Energy is charged by the modular item when a block is mined.
 */
public abstract class EnchantmentModule extends PlayerTickModule implements IEnchantmentModule {
    boolean added;
    boolean removed;

    public EnchantmentModule(ItemStack module, ModuleCategory category, ModuleTarget target) {
        super(module, category, target);
//         setting to and loading these just allow values to be persistant when capability reloads
        added = TagUtils.getModuleBoolean(module, "added");
        removed = TagUtils.getModuleBoolean(module, "removed");
    }

    @Override
    public boolean onPlayerTickActive(Player player, Level level, @Nonnull ItemStack item, int moduleIndex) {
        if (level.isClientSide()) {
            return false;
        }

        double playerEnergy = ElectricItemUtils.getPlayerEnergy(player);
        int energyUsage = getEnergyUsage();

        if (playerEnergy >= energyUsage) {
            int currentLevel = mpsremodularized.common.compat.PaxelIntegration.find(item) == null
                    ? item.getEnchantmentLevel(getEnchantment(level))
                    : item.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS,
                            net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).getLevel(getEnchantment(level));
            if (!added || removed || currentLevel != getLevel()) {
                addEnchantment(item, level);
                setAdded(true);
                setRemoved(false);
                return true;
            }
            return false;
        } else {
            toggleModule(false);
            return false;
        }
    }

    @Override
    public boolean onPlayerTickInactive(Player player, Level level, @Nonnull ItemStack item, int moduleIndex) {
        if (added && !removed) {
            removeEnchantment(item, level);
            setAdded(false);
            setRemoved(true);
            return true;
        }
        return false;
    }

    @Override
    public void setAdded(boolean added) {
        TagUtils.setModuleBoolean(getModule(), "added", added);
        this.added = added;
    }

    @Override
    public void setRemoved(boolean removed) {
        TagUtils.setModuleBoolean(getModule(), "removed", removed);
        this.removed = removed;
    }

    @Override
    public int getLevel() {
        return 1;
    }
}
