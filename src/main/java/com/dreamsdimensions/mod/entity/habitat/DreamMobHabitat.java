package com.dreamsdimensions.mod.entity.habitat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;

/** Server-side, read-only habitat predicate shared by mob spawn policies. */
@FunctionalInterface
public interface DreamMobHabitat {
    boolean isValid(ServerLevelAccessor level, BlockPos feet);
}
