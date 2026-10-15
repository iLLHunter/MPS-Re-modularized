package mpsremodularized.common.item.module.environmental;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.tickable.PlayerTickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.config.module.EnvironmentalModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class MagnetModule extends AbstractPowerModule {
    public static class Ticker extends PlayerTickModule {
        public Ticker(ItemStack module) {
            super(module, ModuleCategory.ENVIRONMENTAL, ModuleTarget.TORSOONLY);
                addBaseProperty(MPSConstants.RADIUS, EnvironmentalModuleConfig.magnetModuleRadiusBase, "m");
                addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, EnvironmentalModuleConfig.magnetModuleEnergyConsumptionBase, "FE");
                addTradeoffProperty(MPSConstants.RADIUS, MPSConstants.ENERGY_CONSUMPTION, EnvironmentalModuleConfig.magnetModuleEnergyConsumptionRadiusMultiplier);
                addIntTradeoffProperty(MPSConstants.RADIUS, MPSConstants.RADIUS, EnvironmentalModuleConfig.magnetModuleRadiusMultiplier, "m", 1, 0);
        }

        @Override
        public boolean isAllowed() {
            return EnvironmentalModuleConfig.magnetModuleIsAllowed;
        }

        @Override
        public boolean onPlayerTickActive(Player player, Level level, ItemStack stack, int moduleIndex) {
            int energyUsage = getEnergyUsage();
            if (ElectricItemUtils.getPlayerEnergy(player) >= energyUsage) {
                boolean isServerSide = !level.isClientSide;

                if ((level.getGameTime() % 20) == 0 && isServerSide) {
                    ElectricItemUtils.drainPlayerEnergy(player, energyUsage, false);
                }
                int range = (int) applyPropertyModifiers(MPSConstants.RADIUS);
                AABB bounds = player.getBoundingBox().inflate(range);

                if (isServerSide) {
                    bounds.expandTowards(0.2000000029802322D, 0.2000000029802322D, 0.2000000029802322D);
                    if (stack.getDamageValue() >> 1 >= 7) {
                        List<Arrow> arrows = level.getEntitiesOfClass(Arrow.class, bounds);
                        for (Arrow arrow : arrows) {
                            if ((arrow.pickup == AbstractArrow.Pickup.ALLOWED) && (level.random.nextInt(6) == 0)) {
                                ItemEntity replacement = new ItemEntity(level, arrow.getX(), arrow.getY(), arrow.getZ(), new ItemStack(Items.ARROW));
                                level.addFreshEntity(replacement);
                            }
                            arrow.remove(Entity.RemovalReason.DISCARDED);
                        }
                    }
                }

                for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, bounds)) {
                    if (e.isAlive() && !e.getItem().isEmpty() && !e.hasPickUpDelay()) {
                        if (isServerSide) {
                            double x = player.getX() - e.getX();
                            double y = player.getY() - e.getY();
                            double z = player.getZ() - e.getZ();

                            double distanceSquared = x * x + y * y + z * z;
                            // Stop at the pickup point instead of normalizing a zero-length vector.
                            if (distanceSquared <= 1.0E-12D) {
                                e.setDeltaMovement(0, 0, 0);
                                e.hasImpulse = true;
                                continue;
                            }
                            double length = Math.sqrt(distanceSquared) * 0.75D;

                            x = x / length + player.getDeltaMovement().x * 22.0D;
                            y = y / length + player.getDeltaMovement().y / 22.0D;
                            z = z / length + player.getDeltaMovement().z * 22.0D;

                            e.setDeltaMovement(x, y, z);

                            e.hasImpulse = true;
                            if (e.horizontalCollision) {
                                e.setDeltaMovement(e.getDeltaMovement().add(0, 1, 0));
                            }
                        } else if (level.random.nextInt(20) == 0) {
                            float pitch = 0.85F - level.random.nextFloat() * 3.0F / 10.0F;
                            level.playLocalSound(e.getX(), e.getY(), e.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, pitch, true);
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public int getEnergyUsage() {
            return (int)applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
        }
    }
}
