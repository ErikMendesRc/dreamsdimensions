package com.dreamsdimensions.mod.entity.spawn;

import com.dreamsdimensions.mod.entity.habitat.DreamMobHabitat;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/** Immutable spawn-population policy for one species, independent of its AI. */
public record MobPopulationProfile<T extends Mob>(
        String id, Supplier<EntityType<T>> entityType, Class<T> mobClass,
        ResourceKey<Level> dimension, DreamMobHabitat habitat,
        Heightmap.Types heightmap,
        int periodTicks, int candidateLimit, int localCap, int maxPerPass,
        int innerRadius, int outerRadius, int countRadius,
        int minPlayerY, int maxPlayerY, int minSpawnY, int maxSpawnY,
        int maxVerticalDistance, double minimumPlayerDistance, double minimumMobSpacing,
        CandidateSelection candidateSelection, boolean stopAfterFirstSuccess) {

    /** A bounded, loaded-chunk-only candidate policy. Null means skip this attempt. */
    @FunctionalInterface
    public interface CandidateSelection {
        BlockPos select(ServerLevel level, ServerPlayer player, RandomSource random,
                MobPopulationProfile<?> profile);
    }

    public MobPopulationProfile {
        Objects.requireNonNull(id);
        Objects.requireNonNull(entityType);
        Objects.requireNonNull(mobClass);
        Objects.requireNonNull(dimension);
        Objects.requireNonNull(habitat);
        Objects.requireNonNull(heightmap);
        Objects.requireNonNull(candidateSelection);
        if (id.isBlank() || periodTicks <= 0 || candidateLimit <= 0 || localCap <= 0
                || maxPerPass <= 0 || innerRadius < 0 || outerRadius < innerRadius
                || countRadius <= 0 || minPlayerY > maxPlayerY || minSpawnY > maxSpawnY
                || maxVerticalDistance < 0 || minimumPlayerDistance < 0
                || minimumMobSpacing < 0) {
            throw new IllegalArgumentException("Invalid mob population profile: " + id);
        }
    }
}
