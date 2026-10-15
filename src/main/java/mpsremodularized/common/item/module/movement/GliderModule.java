package mpsremodularized.common.item.module.movement;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PlayerTickModule;
import mpsremodularized.common.utils.PlayerUtils;
import mpsremodularized.common.config.module.MovementModuleConfig;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class GliderModule extends AbstractPowerModule {
        public static class Ticker extends PlayerTickModule {
            public Ticker(@Nonnull ItemStack module) {
                super(module, ModuleCategory.MOVEMENT, ModuleTarget.TORSOONLY);
            }

            @Override
            public boolean onPlayerTickActive(Player player, Level level, ItemStack chestPlate, int moduleIndex) {
                Vec3 playerHorzFacing = player.getLookAngle();
                playerHorzFacing = new Vec3(playerHorzFacing.x, 0, playerHorzFacing.z);
                playerHorzFacing.normalize();

                PlayerUtils.resetFloatKickTicks(player);

                if (player.isCrouching() && player.getDeltaMovement().y < 0) {
                    Vec3 motion = player.getDeltaMovement();
                    if (motion.y < -0.1) {
                        double motionYchange = Math.min(0.08, -0.1 - motion.y);

                        player.setDeltaMovement(motion.add(
                                playerHorzFacing.x * motionYchange,
                                motionYchange,
                                playerHorzFacing.z * motionYchange
                        ));

                    }
                }
                return false;
            }

            @Override
            public boolean isAllowed() {
                return MovementModuleConfig.gliderModuleIsAllowed;
            }
        }


}
