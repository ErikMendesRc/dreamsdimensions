package com.dreamsdimensions.mod.entity.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.phys.Vec3;

/**
 * Server-authoritative velocity servo using vanilla FlyingPathNavigation.
 * No position teleportation, bone transforms, speculative chunk loads or
 * dependency on a particular mob's Goal/GeckoLib implementation.
 */
public final class DreamFlyingMoveControl extends FlyingMoveControl {
    private final PathfinderMob flyer;
    private final FlightProfile profile;
    private Vec3 hoverAnchor;

    public DreamFlyingMoveControl(PathfinderMob flyer, FlightProfile profile) {
        super(flyer, 12, true);
        this.flyer = flyer;
        this.profile = profile;
    }

    /** Stop waypoint steering and reset only transient hover state. */
    public void brake() {
        this.operation = Operation.WAIT;
        resetHover();
    }

    public void resetHover() {
        this.hoverAnchor = null;
    }

    /**
     * Apply a bounded, collision-aware spring toward a fixed resting anchor.
     * Called by the entity only on the logical server and only when idle.
     */
    public void applyIdleHover() {
        double phase = (flyer.tickCount + flyer.getId() * 13.0)
                * (Math.PI * 2.0 / profile.hoverPeriodTicks());
        Vec3 offset = new Vec3(
                Math.sin(phase * 0.72) * profile.hoverXZAmplitude(),
                Math.sin(phase) * profile.hoverYAmplitude(),
                Math.cos(phase * 0.83) * profile.hoverXZAmplitude());
        if (hoverAnchor == null) {
            hoverAnchor = flyer.position().subtract(offset);
        }
        Vec3 target = hoverAnchor.add(offset);
        Vec3 correction = new Vec3(
                Mth.clamp((target.x - flyer.getX()) * 0.16, -0.014, 0.014),
                Mth.clamp((target.y - flyer.getY()) * 0.24, -0.028, 0.028),
                Mth.clamp((target.z - flyer.getZ()) * 0.16, -0.014, 0.014));
        Vec3 projected = flyer.position().add(correction.scale(2.0));
        if (flyer.level().hasChunkAt(BlockPos.containing(projected))
                && flyer.level().noCollision(flyer, flyer.getBoundingBox().move(correction.scale(2.0)))) {
            flyer.setDeltaMovement(correction);
        } else {
            resetHover();
            flyer.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public void tick() {
        flyer.setSpeed(0);
        Vec3 velocity = flyer.getDeltaMovement();
        if (operation != Operation.MOVE_TO) {
            flyer.setDeltaMovement(velocity.scale(profile.waitDamping()));
            return;
        }

        Vec3 difference = new Vec3(wantedX - flyer.getX(),
                wantedY - flyer.getY(), wantedZ - flyer.getZ());
        if (difference.lengthSqr() < profile.arrivalDistanceSquared()) {
            brake();
            flyer.setDeltaMovement(velocity.scale(profile.arrivalDamping()));
            return;
        }

        double maxSpeed = Mth.clamp(this.speedModifier * profile.speedFactor(),
                profile.minimumSpeed(), profile.maximumSpeed());
        Vec3 heading = difference.normalize();
        double phase = (flyer.tickCount + flyer.getId() * 13.0) * profile.wavePhaseSpeed();
        Vec3 sideways = new Vec3(-heading.z, 0, heading.x);
        Vec3 desired = difference.normalize().scale(maxSpeed)
                .add(sideways.scale(Math.sin(phase) * profile.horizontalWave()))
                .add(0, Math.cos(phase * profile.verticalWaveRatio()) * profile.verticalWave(), 0);
        Vec3 correction = desired.subtract(velocity);
        if (correction.lengthSqr() > profile.maximumAcceleration() * profile.maximumAcceleration()) {
            correction = correction.normalize().scale(profile.maximumAcceleration());
        }
        Vec3 result = velocity.add(correction);
        if (result.lengthSqr() > maxSpeed * maxSpeed) {
            result = result.normalize().scale(maxSpeed);
        }

        Vec3 next = flyer.position().add(result.scale(2));
        if (!flyer.level().hasChunkAt(BlockPos.containing(next))
                || !flyer.level().noCollision(flyer, flyer.getBoundingBox().move(result.scale(2)))) {
            flyer.getNavigation().stop();
            brake();
            flyer.setDeltaMovement(velocity.scale(profile.obstacleDamping()));
            return;
        }

        flyer.setDeltaMovement(result);
        if (Math.abs(difference.x) + Math.abs(difference.z) > 0.1) {
            float desiredYaw = (float) (Mth.atan2(difference.z, difference.x) * (180.0 / Math.PI)) - 90.0f;
            flyer.setYRot(flyer.getYRot() + Mth.wrapDegrees(desiredYaw - flyer.getYRot())
                    * (float) profile.yawSmoothing());
            flyer.setYBodyRot(flyer.getYRot());
        }
    }
}
