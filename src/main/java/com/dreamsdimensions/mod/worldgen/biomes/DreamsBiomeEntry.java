package com.dreamsdimensions.mod.worldgen.biomes;

import com.dreamsdimensions.mod.worldgen.WorldgenProfiles;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.RegionType;

import java.util.List;

/**
 * Entrada declarativa de 1 bioma do mod para injeção via TerraBlender.
 */
public interface DreamsBiomeEntry {
    ResourceKey<Biome> biomeKey();

    RegionType regionType();

    /**
     * Multiplicador simples para ajustar frequência relativa da entrada na region.
     */
    int selectionWeight();

    List<Climate.ParameterPoint> parameterPoints(WorldgenProfiles.Profile profile);
}
