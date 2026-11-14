package com.dreamsdimensions.mod.entity.behavior;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Shared, pure eligibility policy for nonviolent mob interactions.
 * Species retain their own timers, rewards and cooldowns.
 */
public final class CalmPlayerInteraction {
    private CalmPlayerInteraction() {}

    public static boolean isAttentive(Player player, PathfinderMob mob,
            double maxDistance, double maxHorizontalSpeedSquared, double minLookDot) {
        if (player == null || !player.isAlive() || player.isSpectator()
                || player.isSprinting() || !player.onGround()
                || player.getDeltaMovement().horizontalDistanceSqr() > maxHorizontalSpeedSquared
                || player.distanceToSqr(mob) > maxDistance * maxDistance
                || !player.hasLineOfSight(mob)) return false;

        Vec3 towards = new Vec3(mob.getX(),
                mob.getY() + mob.getBbHeight() * 0.65, mob.getZ())
                .subtract(player.getEyePosition());
        return towards.lengthSqr() > 0.001
                && player.getLookAngle().dot(towards.normalize()) >= minLookDot;
    }
}
