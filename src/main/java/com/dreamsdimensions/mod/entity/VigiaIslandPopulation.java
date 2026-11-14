package com.dreamsdimensions.mod.entity;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.entity.spawn.DreamMobPopulationService;
import com.dreamsdimensions.mod.entity.spawn.MobPopulationProfile;
import com.dreamsdimensions.mod.registry.ModEntities;
import net.minecraft.world.level.levelgen.Heightmap;

/** Sparser than Fragmento: local groups of at most two stationary guardians. */
public final class VigiaIslandPopulation {
    private VigiaIslandPopulation() {}

    public static MobPopulationProfile<VigiaAdormecidoEntity> profile() {
        return new MobPopulationProfile<>(
                "vigia_adormecido", ModEntities.VIGIA_ADORMECIDO,
                VigiaAdormecidoEntity.class, DreamDimensionCatalog.DREAMSCAPE,
                VigiaIslandHabitat.habitat(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                360, 32, 2, 1,
                24, 64, 80, 150, 240,
                FragmentoIslandHabitat.MIN_SPAWN_Y, FragmentoIslandHabitat.MAX_SPAWN_Y,
                50, 22.0, 30.0,
                DreamMobPopulationService::annulusHeightmapCandidate, true);
    }
}
