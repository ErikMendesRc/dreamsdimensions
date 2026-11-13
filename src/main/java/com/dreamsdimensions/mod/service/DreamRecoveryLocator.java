package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Optional;

/**
 * Structure-index lookup, once per intentional moss interaction at most.
 * No chunk scan, entity tick loop or automatic terrain modification.
 */
public final class DreamRecoveryLocator {
    public static final TagKey<Structure> RECOVERY_RUINS = TagKey.create(
            Registries.STRUCTURE,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "recovery_ruins"));

    // Search radius is in structure-search chunks, not blocks. Do not use a
    // dimension-wide unbounded locate to prevent stalls in shared servers.
    public static final int SEARCH_RADIUS = 36;

    private DreamRecoveryLocator() {}

    public static Optional<BlockPos> find(ServerLevel level, BlockPos origin) {
        BlockPos found = level.findNearestMapStructure(RECOVERY_RUINS,
                origin, SEARCH_RADIUS, false);
        return found == null ? Optional.empty() : Optional.of(found);
    }

    public static String direction(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        if (Math.abs(dx) > Math.abs(dz) * 2) return dx > 0 ? "Leste" : "Oeste";
        if (Math.abs(dz) > Math.abs(dx) * 2) return dz > 0 ? "Sul" : "Norte";
        if (dx >= 0 && dz >= 0) return "Sudeste";
        if (dx >= 0) return "Nordeste";
        if (dz >= 0) return "Sudoeste";
        return "Noroeste";
    }
}
