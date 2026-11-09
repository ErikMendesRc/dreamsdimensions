package com.dreamsdimensions.mod.client.atmosphere;

import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

/** Bounded loaded-chunk samples only; never forces neighbor chunk loads. */
final class AtmosphereSampling {
    private AtmosphereSampling() {}

    static BlockPos sampledFloor(ClientLevel level, BlockPos center, RandomSource random, int radius) {
        int x = center.getX() + random.nextInt(2 * radius + 1) - radius;
        int z = center.getZ() + random.nextInt(2 * radius + 1) - radius;
        BlockPos probe = new BlockPos(x, center.getY(), z);
        if (!level.hasChunkAt(probe)) return null;
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (Math.abs(surfaceY - center.getY()) > 13 || surfaceY <= level.getMinY() + 1
                || surfaceY >= level.getMaxY() - 2) return null;
        BlockPos floor = new BlockPos(x, surfaceY - 1, z);
        if (!level.getBlockState(floor.above()).isAir()
                || !level.getFluidState(floor.above()).isEmpty()) return null;
        return floor;
    }

    static boolean isLuminous(ClientLevel level, BlockPos floor) {
        return level.getBlockState(floor).is(ModBlocks.DREAM_GLOW_MOSS.get())
                || level.getBlockState(floor).is(ModBlocks.DREAM_SHIMMER_BLOCK.get());
    }

    static boolean belowLeaves(ClientLevel level, BlockPos position) {
        BlockPos canopy = position.above(3);
        return level.hasChunkAt(canopy)
                && level.getBlockState(canopy).is(ModBlocks.DREAM_LEAVES_BLOCK.get());
    }

    static boolean isValley(ClientLevel level, BlockPos position) {
        // Two distant height probes only; a hint for particles, not fog rewriting.
        int y = position.getY();
        if (y > 125) return false;
        BlockPos east = position.offset(12, 0, 0);
        BlockPos north = position.offset(0, 0, 12);
        if (!level.hasChunkAt(east) || !level.hasChunkAt(north)) return false;
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                east.getX(), east.getZ()) >= y + 7
                || level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                north.getX(), north.getZ()) >= y + 7;
    }
}
