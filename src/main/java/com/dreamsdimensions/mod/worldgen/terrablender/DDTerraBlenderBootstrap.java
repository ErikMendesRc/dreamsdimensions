package com.dreamsdimensions.mod.worldgen.terrablender;

import com.dreamsdimensions.mod.DreamsDimensions;
import terrablender.api.Regions;

public final class DDTerraBlenderBootstrap {
    private static final int OVERWORLD_REGION_WEIGHT = 8;

    private DDTerraBlenderBootstrap() {
    }

    public static void register() {
        DreamsDimensions.LOGGER.info("[TerraBlender] Registrando regions do Dreams Dimensions...");
        Regions.register(new DreamsOverworldRegion(OVERWORLD_REGION_WEIGHT));
        DreamsDimensions.LOGGER.info("[TerraBlender] Region registrada: {} (weight={})",
                DreamsOverworldRegion.REGION_ID,
                OVERWORLD_REGION_WEIGHT);
    }
}
