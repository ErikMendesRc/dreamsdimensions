package com.dreamsdimensions.mod.worldgen.terrablender;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.worldgen.WorldgenProfiles;
import com.dreamsdimensions.mod.worldgen.biomes.DreamsBiomeEntry;
import com.dreamsdimensions.mod.worldgen.biomes.DreamsBiomesOverworld;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

/**
 * Region única de Overworld do mod, consumindo o catálogo de biomas.
 */
public final class DreamsOverworldRegion extends Region {
    public static final Identifier REGION_ID = Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "overworld");

    public DreamsOverworldRegion(int weight) {
        super(REGION_ID, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        DreamsDimensions.LOGGER.info("[TerraBlender] addBiomes region={} profile={} entries={}",
                REGION_ID,
                WorldgenProfiles.ACTIVE,
                DreamsBiomesOverworld.ENTRIES.size());

        for (DreamsBiomeEntry entry : DreamsBiomesOverworld.ENTRIES) {
            if (entry.regionType() != RegionType.OVERWORLD) {
                continue;
            }

            for (Climate.ParameterPoint point : entry.parameterPoints(WorldgenProfiles.ACTIVE)) {
                for (int i = 0; i < entry.selectionWeight(); i++) {
                    mapper.accept(Pair.of(point, entry.biomeKey()));
                }
            }

            DreamsDimensions.LOGGER.debug("[TerraBlender] biome={} points={} weight={}",
                    entry.biomeKey().location(),
                    entry.parameterPoints(WorldgenProfiles.ACTIVE).size(),
                    entry.selectionWeight());
        }
    }
}
