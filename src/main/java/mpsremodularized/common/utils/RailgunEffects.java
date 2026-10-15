package mpsremodularized.common.utils;

import mpsremodularized.common.registration.MPSParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Sends visuals only after a server-authorized shot, using the same hit as damage. */
public final class RailgunEffects {
    private static final double OUTGOING_SPEED = 2.0;
    // Match the existing particle's drag and longest spark lifetime; stop short shots at the target.
    private static final double OUTGOING_TRAVEL = (1 - Math.pow(0.82F, 8)) / (1 - 0.82F);
    private RailgunEffects() {}

    public static void play(ServerLevel level, Player shooter, HitResult hit) {
        Vec3 look = shooter.getViewVector(1.0F);
        double yaw = Math.toRadians(shooter.getViewYRot(1.0F));
        Vec3 right = new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
        Vec3 up = right.cross(look);
        double handSide = shooter.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        Vec3 muzzle = shooter.getEyePosition().add(look.scale(0.65))
                .add(right.scale(0.24 * handSide)).subtract(up.scale(0.2));
        var muzzleHit = level.clip(new ClipContext(shooter.getEyePosition(), muzzle,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        if (muzzleHit.getType() != HitResult.Type.MISS) {
            Vec3 offset = muzzle.subtract(shooter.getEyePosition()).normalize().scale(0.05);
            muzzle = muzzleHit.getLocation().subtract(offset);
        }
        Vec3 toTarget = hit.getLocation().subtract(muzzle);
        Vec3 direction = toTarget.normalize();
        double speed = Math.min(OUTGOING_SPEED, toTarget.length() / OUTGOING_TRAVEL);
        burst(level, shooter, muzzle, 8, 0.025, speed, direction);

        if (hit.getType() != HitResult.Type.MISS) {
            // Offset from a block face so the flash isn't hidden inside the solid surface.
            Vec3 outward = hit instanceof BlockHitResult blockHit
                    ? Vec3.atLowerCornerOf(blockHit.getDirection().getNormal()) : look.scale(-1);
            Vec3 impact = hit.getLocation().add(outward.scale(0.04));
            burst(level, shooter, impact, 14, 0.04, 0.13, null);
        }
    }

    private static void burst(ServerLevel level, Player shooter, Vec3 position, int sparks, double spread, double speed,
                              Vec3 direction) {
        // The shooter sees impacts across the full 200-block range; nearby observers use vanilla's 32-block radius.
        if (shooter instanceof ServerPlayer serverPlayer) {
            sendBurst(level, serverPlayer, true, position, sparks, spread, speed, direction);
        }
        for (ServerPlayer viewer : level.players()) {
            if (viewer != shooter) {
                sendBurst(level, viewer, false, position, sparks, spread, speed, direction);
            }
        }
    }

    private static void sendBurst(ServerLevel level, ServerPlayer viewer, boolean longDistance, Vec3 position,
                                  int sparks, double spread, double speed, Vec3 direction) {
        if (direction == null) {
            level.sendParticles(viewer, MPSParticles.RAILGUN_FLASH.get(), longDistance,
                    position.x, position.y, position.z, 1, 0, 0, 0, 0);
            level.sendParticles(viewer, MPSParticles.RAILGUN_SPARK.get(), longDistance,
                    position.x, position.y, position.z, sparks, spread, spread, spread, speed);
        } else {
            // The visible shot, including its bright core, leaves the muzzle on one straight rail.
            // Count zero supplies exact forward velocity instead of a random spherical burst.
            level.sendParticles(viewer, MPSParticles.RAILGUN_FLASH.get(), longDistance,
                    position.x, position.y, position.z, 0, direction.x, direction.y, direction.z, speed);
            for (int i = 0; i < sparks; i++) {
                Vec3 start = position.subtract(direction.scale(i * spread));
                level.sendParticles(viewer, MPSParticles.RAILGUN_SPARK.get(), longDistance,
                        start.x, start.y, start.z, 0, direction.x, direction.y, direction.z, speed);
            }
        }
    }
}
