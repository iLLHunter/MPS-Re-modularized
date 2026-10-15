package mpsremodularized.common.item.module.weapon;


import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.HeatUtils;
import mpsremodularized.common.config.module.WeaponModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.AbstractPowerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Created by User: Andrew2448
 * 5:56 PM 6/14/13
 */
public class LightningModule extends AbstractPowerModule {
    public static class RightClickie extends RightClickModule {

        public RightClickie(ItemStack module) {
            super(module, ModuleCategory.WEAPON, ModuleTarget.TOOLONLY);
            addBaseProperty(MPSConstants.ENERGY_CONSUMPTION, WeaponModuleConfig.lightningSummonerEnergyConsumption, "FE");
            addBaseProperty(MPSConstants.HEAT_EMISSION, WeaponModuleConfig.lightningSummonerHeatEmission, "");
        }

        @Override
        public InteractionResultHolder<ItemStack> use(@NonNull ItemStack itemStackIn, Level level, Player playerIn, InteractionHand hand) {
            if (hand == InteractionHand.MAIN_HAND) {
                int energyConsumption = getEnergyUsage();
                if (energyConsumption <= ElectricItemUtils.getPlayerEnergy(playerIn)) {
                    if (!level.isClientSide()) {
                        double range = 64;

                        HitResult raytraceResult = rayTrace(level, playerIn, ClipContext.Fluid.SOURCE_ONLY, range);
                        if (raytraceResult != null && raytraceResult.getType() != HitResult.Type.MISS) {
                            BlockPos targetPos = ((net.minecraft.world.phys.BlockHitResult) raytraceResult).getBlockPos().above();
                            if (level.canSeeSky(targetPos)) {
                                LightningBolt sparkie = EntityType.LIGHTNING_BOLT.create(level);

                                if (sparkie != null) {
                                    sparkie.moveTo(Vec3.atBottomCenterOf(targetPos));
                                    sparkie.setCause(playerIn instanceof ServerPlayer ? (ServerPlayer)playerIn : null);
                                    if (level.addFreshEntity(sparkie)) {
                                        ElectricItemUtils.drainPlayerEnergy(playerIn, energyConsumption, false);
                                        HeatUtils.heatPlayer(playerIn, applyPropertyModifiers(MPSConstants.HEAT_EMISSION));
                                    }
                                }
                            }
                        }
                    }
                    return InteractionResultHolder.success(itemStackIn);
                }
            }
            return InteractionResultHolder.pass(itemStackIn);
        }

        @Override
        public boolean isAllowed() {
            return WeaponModuleConfig.lightningSummonerIsAllowed;
        }

        @Override
        public int getEnergyUsage() {
            return (int) Math.round(applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION));
        }

    }
}
