package com.dreamsdimensions.mod.entity;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.entity.habitat.DreamMobHabitat;
import com.dreamsdimensions.mod.entity.habitat.FloatingIslandHabitat;
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

/** Fragmento-specific island profile; public signatures retained for spawn/AI compatibility. */
public final class FragmentoIslandHabitat {
    public static final int MIN_SPAWN_Y = 158;
    public static final int MAX_SPAWN_Y = 231;
    private static final int[] GAP_DEPTHS = {12, 20, 28, 36, 44, 52, 60, 68, 76, 84, 92, 100, 108};
    private static final TagKey<Block> GROUND = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "dreamscape_natural_ground"));
    private static final ResourceKey<Biome> DREAMSCAPE_BIOME = ResourceKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "dreamscape_biome"));
    private static final FloatingIslandHabitat ISLAND = new FloatingIslandHabitat(
            DreamDimensionCatalog.DREAMSCAPE, DREAMSCAPE_BIOME, GROUND,
            MIN_SPAWN_Y, MAX_SPAWN_Y, GAP_DEPTHS);

    private FragmentoIslandHabitat() {}

    public static DreamMobHabitat habitat() {
        return ISLAND;
    }

    public static boolean maySpawn(EntityType<FragmentoErranteEntity> type,
            ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return isIslandSurface(level, pos);
    }

    public static boolean isIslandSurface(ServerLevelAccessor level, BlockPos feet) {
        return ISLAND.isValid(level, feet);
    }

    public static boolean hasNearbyIslandPerch(ServerLevelAccessor level, BlockPos feet) {
        return ISLAND.hasNearbyPerch(level, feet, 9);
    }
}
