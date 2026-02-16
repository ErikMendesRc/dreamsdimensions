package com.dreamsdimensions.mod.worldgen.biomes;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.worldgen.WorldgenProfiles;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils;
import terrablender.api.RegionType;

import java.util.List;

public final class LuminaHollowsEntry implements DreamsBiomeEntry {
    public static final ResourceKey<Biome> BIOME_KEY = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "lumina_hollows")
    );

    @Override
    public ResourceKey<Biome> biomeKey() {
        return BIOME_KEY;
    }

    @Override
    public RegionType regionType() {
        return RegionType.OVERWORLD;
    }

    @Override
    public int selectionWeight() {
        return 1;
    }

    @Override
    public List<Climate.ParameterPoint> parameterPoints(WorldgenProfiles.Profile profile) {
        return switch (profile) {
            case TEST -> List.of(Climate.parameters(
                    ParameterUtils.Temperature.span(ParameterUtils.Temperature.COOL, ParameterUtils.Temperature.WARM),
                    ParameterUtils.Humidity.span(ParameterUtils.Humidity.NEUTRAL, ParameterUtils.Humidity.HUMID),
                    ParameterUtils.Continentalness.span(ParameterUtils.Continentalness.COAST, ParameterUtils.Continentalness.FAR_INLAND),
                    ParameterUtils.Erosion.span(ParameterUtils.Erosion.EROSION_1, ParameterUtils.Erosion.EROSION_5),
                    ParameterUtils.Weirdness.FULL_RANGE.parameter(),
                    ParameterUtils.Depth.SURFACE.parameter(),
                    0.0F
            ));
            case PROD -> List.of(Climate.parameters(
                    ParameterUtils.Temperature.NEUTRAL.parameter(),
                    ParameterUtils.Humidity.HUMID.parameter(),
                    ParameterUtils.Continentalness.span(ParameterUtils.Continentalness.MID_INLAND, ParameterUtils.Continentalness.FAR_INLAND),
                    ParameterUtils.Erosion.EROSION_3.parameter(),
                    ParameterUtils.Weirdness.MID_SLICE_NORMAL_ASCENDING.parameter(),
                    ParameterUtils.Depth.SURFACE.parameter(),
                    0.0F
            ));
        };
    }
}
