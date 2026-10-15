/*
 * Copyright (c) 2021. MachineMuse, Lehjr
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *      Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package mpsremodularized.common.item.module.weapon;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.IRightClickModule;
import mpsremodularized.common.capabilities.module.tickable.PassivePlayerTickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.HeatUtils;
import mpsremodularized.common.utils.RailgunEffects;
import mpsremodularized.common.utils.TagUtils;
import mpsremodularized.common.config.module.WeaponModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class RailgunModule extends AbstractPowerModule {

    public static class Ticker extends PassivePlayerTickModule implements IRightClickModule {
        private static final double RANGE = 200;
        private static final int COOLDOWN_TICKS = 10;
        private static final double IMPULSE_PER_DAMAGE = 50.0;
        private static final double IMPULSE_PER_KNOCKBACK = 2000.0;

        public Ticker(ItemStack module) {
            super(module, ModuleCategory.WEAPON, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.TOTAL_IMPULSE, WeaponModuleConfig.railgunTotalImpulseBase, "Ns");
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, WeaponModuleConfig.railgunEnergyConsumptionBase, "FE");
            addBaseProperty(MPSConstants.HEAT_EMISSION, WeaponModuleConfig.railgunHeatEmissionBase, "");
            addTradeoffProperty(MPSConstants.VOLTAGE, MPSConstants.TOTAL_IMPULSE, WeaponModuleConfig.railgunTotalImpulseVoltageMultiplier);
            addTradeoffProperty(MPSConstants.VOLTAGE, MPSConstants.ENERGY_CONSUMPTION, WeaponModuleConfig.railgunEnergyConsumptionVoltageMultiplier);
            addTradeoffProperty(MPSConstants.VOLTAGE, MPSConstants.HEAT_EMISSION, WeaponModuleConfig.railgunHeatEmissionVoltageMultiplier);
        }

        @Override
        public boolean onPlayerTickActive(Player player, Level level, @Nonnull ItemStack itemStackIn, int moduleIndex) {
            return onPlayerTickInactive(player, level, itemStackIn,  moduleIndex);
        }

        @Override
        public boolean onPlayerTickInactive(Player player, Level level, @NotNull ItemStack itemStackIn, int moduleIndex) {
            int timer = TagUtils.getModularItemInt(itemStackIn, MPSConstants.RAILGUN_COOLDOWN_TIMER);
            if (timer > 0) {
                TagUtils.setModularItemInt(itemStackIn, MPSConstants.RAILGUN_COOLDOWN_TIMER, timer - 1);
                return true;
            }
            return false;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(ItemStack itemStack, Level level, Player player, InteractionHand hand) {
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResultHolder.pass(itemStack);
            }

            int timer = TagUtils.getModularItemInt(itemStack, MPSConstants.RAILGUN_COOLDOWN_TIMER);
            int energyConsumption = getEnergyUsage();
            if (timer > 0 || ElectricItemUtils.getPlayerEnergy(player) < energyConsumption) {
                return InteractionResultHolder.fail(itemStack);
            }

            if (!level.isClientSide) {
                ElectricItemUtils.drainPlayerEnergy(player, energyConsumption, false);
                TagUtils.setModularItemInt(itemStack, MPSConstants.RAILGUN_COOLDOWN_TIMER, COOLDOWN_TICKS);
                HeatUtils.heatPlayer(player, applyPropertyModifiers(MPSConstants.HEAT_EMISSION));

                double totalImpulse = applyPropertyModifiers(MPSConstants.TOTAL_IMPULSE);
                float damage = (float) (totalImpulse / IMPULSE_PER_DAMAGE);
                double knockback = totalImpulse / IMPULSE_PER_KNOCKBACK;
                Vec3 lookVec = player.getLookAngle();
                HitResult hitResult = rayTrace(player, RANGE);

                if (level instanceof ServerLevel serverLevel) {
                    RailgunEffects.play(serverLevel, player, hitResult);
                }

                if (hitResult instanceof EntityHitResult entityHitResult) {
                    Entity entity = entityHitResult.getEntity();
                    if (entity instanceof LivingEntity livingEntity) {
                        DamageSource damageSource = player.damageSources().playerAttack(player);
                        if (livingEntity.hurt(damageSource, damage)) {
                            pushWithResistance(livingEntity, lookVec.x * knockback, Math.abs(lookVec.y + 0.2F) * knockback, lookVec.z * knockback);
                        }
                    }
                }

                pushWithResistance(player, -lookVec.x * knockback, Math.abs(-lookVec.y + 0.2F) * knockback, -lookVec.z * knockback);
                level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5F,
                    0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
            }

            return InteractionResultHolder.consume(itemStack);
        }

        private static void pushWithResistance(LivingEntity entity, double x, double y, double z) {
            // Match vanilla's resistance factor while preserving the Railgun's three-dimensional impulse.
            double scale = 1.0 - Math.clamp(entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            if (scale > 0.0) {
                entity.push(x * scale, y * scale, z * scale);
                entity.hurtMarked = true;
            }
        }

        @Override
        public boolean isAllowed() {
            return WeaponModuleConfig.railgunIsAllowed;
        }

        @Override
        public int getEnergyUsage() {
            return (int) Math.round(applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION));
        }

        public HitResult rayTrace(Player player, double attackRange) {
            float pPartialTicks = 1.0F;

            Vec3 eyePositionVec = player.getEyePosition(pPartialTicks);
            double pickRange = Math.max(player.getPickRadius(), attackRange);
            HitResult hitResult = player.pick(pickRange, pPartialTicks, false);
            double range = pickRange * pickRange;
            if (hitResult.getType() != HitResult.Type.MISS) { // Add != MISS to ensure Attack Range is not clamped at the value of Reach Distance.
                range = hitResult.getLocation().distanceToSqr(eyePositionVec);
            }

            Vec3 viewVector = player.getViewVector(1.0F);
            Vec3 vec32 = eyePositionVec.add(viewVector.x * pickRange, viewVector.y * pickRange, viewVector.z * pickRange);
            AABB aabb = player.getBoundingBox().expandTowards(viewVector.scale(pickRange)).inflate(1.0D, 1.0D, 1.0D);
            EntityHitResult entityhitresult = ProjectileUtil.
                    getEntityHitResult(player, eyePositionVec, vec32, aabb,
                            (entity) -> !entity.isSpectator() && entity.isPickable(), range);

            if (entityhitresult != null && entityhitresult.getEntity() instanceof LivingEntity) {
                Vec3 entityHitLocation = entityhitresult.getLocation();
                double d2 = eyePositionVec.distanceToSqr(entityHitLocation);
                if (d2 < range) {
                    hitResult = entityhitresult;
                }
            }
            return hitResult;
        }

    }
}
