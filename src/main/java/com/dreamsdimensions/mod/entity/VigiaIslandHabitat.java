package com.dreamsdimensions.mod.entity;

import com.dreamsdimensions.mod.entity.habitat.DreamMobHabitat;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ServerLevelAccessor;

/** A standing, three-block-tall terrestrial mob on actual floating land. */
public final class VigiaIslandHabitat {
    private static final DreamMobHabitat HABITAT = (level, feet) ->
            FragmentoIslandHabitat.isIslandSurface(level, feet)
                    && level.hasChunkAt(feet.above(2))
                    && level.isEmptyBlock(feet.above(2));

    private VigiaIslandHabitat() {}

    public static DreamMobHabitat habitat() {
        return HABITAT;
    }

    public static boolean maySpawn(EntityType<VigiaAdormecidoEntity> type,
            ServerLevelAccessor level, EntitySpawnReason reason, BlockPos feet, RandomSource random) {
        return HABITAT.isValid(level, feet);
    }
}
