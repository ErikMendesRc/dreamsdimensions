package com.dreamsdimensions.mod.entity;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * A physical test for an island-top spawn. Dreamscape has only one biome for
 * mainland AND sky islands; an elevation check alone would be insufficient.
 * Uses loaded blocks only and never forces chunk generation.
 */
public final class FragmentoIslandHabitat {
    public static final int MIN_SPAWN_Y = 158;
    public static final int MAX_SPAWN_Y = 231;
    private static final int[] GAP_DEPTHS = {24, 40, 56, 68};
    private static final TagKey<Block> GROUND = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "dreamscape_natural_ground"));
    private static final ResourceKey<Biome> DREAMSCAPE_BIOME = ResourceKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "dreamscape_biome"));

    private FragmentoIslandHabitat() {}

    public static boolean maySpawn(EntityType<FragmentoErranteEntity> type,
            ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return isIslandSurface(level, pos);
    }

    public static boolean isIslandSurface(ServerLevelAccessor level, BlockPos feet) {
        if (feet.getY() < MIN_SPAWN_Y || feet.getY() > MAX_SPAWN_Y
                || !level.getLevel().dimension().equals(DreamDimensionCatalog.DREAMSCAPE)
                || !level.hasChunkAt(feet) || !level.getBiome(feet).is(DREAMSCAPE_BIOME)) {
            return false;
        }
        if (!level.getBlockState(feet.below()).is(GROUND)
                || !level.isEmptyBlock(feet)
                || !level.isEmptyBlock(feet.above())
                || !level.getFluidState(feet).isEmpty()) return false;

        // Islands can be tens of blocks thick. Probe several depths for a
        // substantial air gap rather than testing a single hard-coded Y.
        for (int depth : GAP_DEPTHS) {
            BlockPos airA = feet.below(depth);
            BlockPos airB = airA.below(5);
            if (airB.getY() <= level.getMinY() || !level.hasChunkAt(airA)
                    || !level.hasChunkAt(airB)) continue;
            if (level.isEmptyBlock(airA) && level.isEmptyBlock(airB)) return true;
        }
        return false;
    }

    public static boolean hasNearbyIslandPerch(ServerLevelAccessor level, BlockPos feet) {
        if (!level.hasChunkAt(feet)) return false;
        for (int down = 0; down <= 9; down++) {
            if (isIslandSurface(level, feet.below(down))) return true;
        }
        return false;
    }
}
