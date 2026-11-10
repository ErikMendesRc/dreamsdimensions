package com.dreamsdimensions.mod.entity.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Reusable read-only ground support checks for land mobs and safe landing.
 * Only probes already-loaded chunks. Never mutates terrain or computes paths.
 */
public final class SafeGroundNavigation {
    private SafeGroundNavigation() {}

    public static boolean canStand(ServerLevel level, BlockPos feet, Entity entity) {
        if (feet.getY() <= level.getMinY() || feet.getY() >= level.getMaxY() - 3
                || !level.getWorldBorder().isWithinBounds(feet)
                || !level.hasChunkAt(feet) || !level.hasChunkAt(feet.below())
                || !level.hasChunkAt(feet.above(3))) return false;
        BlockPos floor = feet.below();
        if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                || !level.getFluidState(floor).isEmpty()) return false;
        for (int dy = 0; dy <= Math.ceil(entity.getBbHeight()); dy++) {
            BlockPos probe = feet.above(dy);
            if (!level.hasChunkAt(probe) || !level.getBlockState(probe).isAir()
                    || !level.getFluidState(probe).isEmpty()) return false;
        }
        return level.noCollision(entity,
                entity.getDimensions(Pose.STANDING).makeBoundingBox(Vec3.atBottomCenterOf(feet)));
    }

    /** Reject discontinuous support or steep steps before starting a ground path. */
    public static boolean hasSupportedCorridor(ServerLevel level, Entity mob,
            BlockPos target, int maxSamples, int maxStepY) {
        Vec3 from = mob.position();
        Vec3 to = Vec3.atBottomCenterOf(target);
        double distance = Math.hypot(to.x - from.x, to.z - from.z);
        int count = Math.min(maxSamples, Math.max(1, (int) Math.ceil(distance * 1.4)));
        int previousY = mob.blockPosition().getY();
        for (int n = 1; n <= count; n++) {
            double t = n / (double) count;
            int x = (int) Math.floor(from.x + (to.x - from.x) * t);
            int z = (int) Math.floor(from.z + (to.z - from.z) * t);
            BlockPos probe = new BlockPos(x, target.getY(), z);
            if (!level.hasChunkAt(probe)) return false;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos feet = new BlockPos(x, y, z);
            if (Math.abs(y - previousY) > maxStepY || !canStand(level, feet, mob)) return false;
            previousY = y;
        }
        return true;
    }
}
