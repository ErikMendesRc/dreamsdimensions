package com.dreamsdimensions.mod.client.atmosphere;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Client-only execution budget; visual fog/sky settings live in datapacks. */
public record AtmosphereProfile(ResourceKey<Level> dimension,
        int particleEveryTicks, int decreasedParticleMultiplier,
        int maxParticleSamples, int soundMinimumTicks, int soundJitterTicks,
        int sampleRadius) {
    public static final AtmosphereProfile BLUE_FIELD = new AtmosphereProfile(
            ResourceKey.create(Registries.DIMENSION,
                    Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "campo_onirico_azul")),
            12, 2, 8, 160, 160, 8
    );
}
