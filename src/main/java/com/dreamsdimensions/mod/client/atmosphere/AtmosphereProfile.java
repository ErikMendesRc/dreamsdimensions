package com.dreamsdimensions.mod.client.atmosphere;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Client-only execution budget. One registry entry per supported ambience;
 * visual fog/sky settings stay in the existing dimension datapacks.
 */
public record AtmosphereProfile(ResourceKey<Level> dimension,
        int particleEveryTicks, int decreasedParticleMultiplier,
        int maxParticleSamples, int soundMinimumTicks, int soundJitterTicks,
        int sampleRadius) {
    public static final Identifier BLUE_ID = DreamDimensionCatalog.id("blue_field");
    public static final AtmosphereProfile BLUE_FIELD = new AtmosphereProfile(
            DreamDimensionCatalog.BLUE_FIELD,
            12, 2, 8, 160, 160, 8
    );
    private static final Map<Identifier, AtmosphereProfile> PROFILES = Map.of(
            BLUE_ID, BLUE_FIELD
    );

    public static Optional<AtmosphereProfile> find(Identifier id) {
        return Optional.ofNullable(PROFILES.get(id));
    }
}
