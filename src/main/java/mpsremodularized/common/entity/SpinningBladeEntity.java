package mpsremodularized.common.entity;

import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSEntities;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.IShearable;

public class SpinningBladeEntity extends ThrowableProjectile {
    public static final int SIZE = 24;
    public static final float DEGREES_FROM_RADIANS = (float) Math.toDegrees(1);




    public double damage;
    public ItemStack shootingItem = ItemStack.EMPTY;

    public SpinningBladeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
        setNoGravity(true);
    }

    public SpinningBladeEntity(Level worldIn, LivingEntity shootingEntity) {
        super(MPSEntities.SPINNING_BLADE_ENTITY_TYPE.get(), shootingEntity, worldIn);
        this.setOwner(shootingEntity);
        this.setNoGravity(true);
        if (shootingEntity instanceof Player) {


            this.shootingItem = ((Player) shootingEntity).getInventory().getSelected();
            IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(this.shootingItem);

            damage = 0;
            if(mci != null) {
                int index = mci.findInstalledModule(MPSConstants.BLADE_LAUNCHER_MODULE);
                if (index >= 0) {
                    IPowerModule pm = mci.getStackInSlot(index).getCapability(MPSCapabilities.Module.POWER_MODULE);
                    if(pm != null) {
                        damage = pm.applyPropertyModifiers(MPSConstants.BLADE_DAMAGE);
                    }
                }
            }
        }
        double speed = 1.0;
        double scale = 1;

        Vec3 direction = shootingEntity.getLookAngle().normalize();//
        this.setDeltaMovement(direction.multiply(speed, speed, speed));

        double r = 1;
        this.setPos(
            shootingEntity.getX(),
            shootingEntity.getY() + shootingEntity.getEyeHeight(),
            shootingEntity.getZ());


        this.setBoundingBox(new AABB(getX() - r, getY() - r, getZ() - r, getX() + r, getY() + r, getZ() + r));


        //        this.performShooting(level, player, hand, itemstack, getShootingPower(chargedprojectiles), 1.0F, null);
        //        shoot(shootingEntity, (float) speed, 1F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {

    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > this.getMaxLifetime()) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (this.level().isClientSide || this.isRemoved()) return;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            ServerLevel world = (ServerLevel) this.level();
            BlockHitResult result = (BlockHitResult) hitResult;
            var state = world.getBlockState(result.getBlockPos());
            if (state.getBlock() instanceof IShearable target && getOwner() instanceof Player player
                    && target.isShearable(player, shootingItem, world, result.getBlockPos())) {
                var drops = target.onSheared(player, shootingItem, world, result.getBlockPos());
                // Vanilla foliage uses loot tables; its default IShearable hook returns no drops.
                if (drops.isEmpty() && world.getBlockState(result.getBlockPos()).equals(state)) {
                    drops = Block.getDrops(state, world, result.getBlockPos(), world.getBlockEntity(result.getBlockPos()), player, new ItemStack(Items.SHEARS));
                }
                for (ItemStack drop : drops) target.spawnShearedDrop(world, result.getBlockPos(), drop);
                world.destroyBlock(result.getBlockPos(), false); // Shearing already supplied the drops.
            } else { // Block hit was not IShearable
                this.discard();
            }
        } else if (hitResult.getType() == HitResult.Type.ENTITY && ((EntityHitResult)hitResult).getEntity() != getOwner()) {
            EntityHitResult result = (EntityHitResult) hitResult;
            if (result.getEntity() instanceof IShearable) {
                IShearable target = (IShearable) result.getEntity();
                Entity entity = result.getEntity();
                if (getOwner() instanceof Player player && target.isShearable(player, shootingItem, level(), entity.blockPosition())) {
                    for (ItemStack drop : target.onSheared(player, shootingItem, level(), entity.blockPosition())) entity.spawnAtLocation(drop);
                }
            } else {
                result.getEntity().hurt(this.damageSources().thrown(this, getOwner()), (int) damage);
            }
        }
    }

    public int getMaxLifetime() {
        return 200;
    }
}
