package com.dreamsdimensions.mod.worldgen.biome;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Chaves de biomas do mod.
 *
 * <p>Os dados completos de biome continuam em datapack JSON
 * (`data/dreamsdimensions/worldgen/biome/*.json`).</p>
 */
public final class DDBiomes {
    public static final ResourceKey<Biome> LUMINA_HOLLOWS = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "lumina_hollows")
    );

    private DDBiomes() {
    }
}
