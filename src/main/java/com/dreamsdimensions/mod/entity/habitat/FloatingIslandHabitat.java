package com.dreamsdimensions.mod.entity.habitat;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * Read-only island detection. Air gaps are sampled below a natural perch,
 * not inferred from Y alone. All probes are bounded and use loaded chunks.
 */
public final class FloatingIslandHabitat implements DreamMobHabitat {
    private final ResourceKey<Level> dimension;
    private final ResourceKey<Biome> biome;
    private final TagKey<Block> ground;
    private final int minSpawnY;
    private final int maxSpawnY;
    private final int[] gapDepths;

    public FloatingIslandHabitat(ResourceKey<Level> dimension, ResourceKey<Biome> biome,
            TagKey<Block> ground, int minSpawnY, int maxSpawnY, int[] gapDepths) {
        this.dimension = Objects.requireNonNull(dimension);
        this.biome = Objects.requireNonNull(biome);
        this.ground = Objects.requireNonNull(ground);
        if (minSpawnY > maxSpawnY || gapDepths.length == 0) {
            throw new IllegalArgumentException("Invalid island detection bounds");
        }
        this.minSpawnY = minSpawnY;
        this.maxSpawnY = maxSpawnY;
        this.gapDepths = gapDepths.clone();
        for (int depth : this.gapDepths) {
            if (depth <= 0) throw new IllegalArgumentException("Gap depths must be positive");
        }
    }

    @Override
    public boolean isValid(ServerLevelAccessor level, BlockPos feet) {
        if (feet.getY() < minSpawnY || feet.getY() > maxSpawnY
                || !level.getLevel().dimension().equals(dimension)
                || !level.hasChunkAt(feet) || !level.getBiome(feet).is(biome)) {
            return false;
        }
        if (!level.getBlockState(feet.below()).is(ground)
                || !level.isEmptyBlock(feet)
                || !level.isEmptyBlock(feet.above())
                || !level.getFluidState(feet).isEmpty()) return false;

        // Preserve the Fragmento's two-point, 5-block gap detection exactly.
        for (int depth : gapDepths) {
            BlockPos airA = feet.below(depth);
            BlockPos airB = airA.below(5);
            if (airB.getY() <= level.getMinY() || !level.hasChunkAt(airA)
                    || !level.hasChunkAt(airB)) continue;
            if (level.isEmptyBlock(airA) && level.isEmptyBlock(airB)) return true;
        }
        return false;
    }

    /** Optional tether validation for flyers whose home is a floating island. */
    public boolean hasNearbyPerch(ServerLevelAccessor level, BlockPos feet, int maxDown) {
        if (!level.hasChunkAt(feet)) return false;
        for (int down = 0; down <= maxDown; down++) {
            if (isValid(level, feet.below(down))) return true;
        }
        return false;
    }
}
