package com.dreamsdimensions.mod.entity;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.entity.spawn.MobPopulationProfile;
import com.dreamsdimensions.mod.entity.spawn.DreamMobPopulationService;
import com.dreamsdimensions.mod.registry.ModEntities;
import net.minecraft.world.level.levelgen.Heightmap;

/** Species-owned configuration; runtime scheduling belongs to the shared coordinator. */
public final class FragmentoIslandPopulation {
    static final int PERIOD_TICKS = 200;
    static final int CANDIDATE_LIMIT = 48;
    static final int LOCAL_CAP = 6;
    static final int MAX_NEW_MOBS_PER_PASS = 2;
    private static final int INNER_RADIUS = 16;
    private static final int OUTER_RADIUS = 64;
    private static final int MIN_PLAYER_Y = 145;
    private static final int MAX_PLAYER_Y = 244;
    private static final double MIN_SPACING_SQR = 14.0 * 14.0;

    private FragmentoIslandPopulation() {}

    public static MobPopulationProfile<FragmentoErranteEntity> profile() {
        return new MobPopulationProfile<>(
                "fragmento_errante", ModEntities.FRAGMENTO_ERRANTE,
                FragmentoErranteEntity.class, DreamDimensionCatalog.DREAMSCAPE,
                FragmentoIslandHabitat.habitat(),
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PERIOD_TICKS, CANDIDATE_LIMIT, LOCAL_CAP, MAX_NEW_MOBS_PER_PASS,
                INNER_RADIUS, OUTER_RADIUS, 72, MIN_PLAYER_Y, MAX_PLAYER_Y,
                FragmentoIslandHabitat.MIN_SPAWN_Y, FragmentoIslandHabitat.MAX_SPAWN_Y,
                65, Math.sqrt(MIN_SPACING_SQR), Math.sqrt(MIN_SPACING_SQR),
                DreamMobPopulationService::annulusHeightmapCandidate, true);
    }
}
