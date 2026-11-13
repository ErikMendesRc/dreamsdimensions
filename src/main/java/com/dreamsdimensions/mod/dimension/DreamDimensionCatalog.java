package com.dreamsdimensions.mod.dimension;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.registry.ModBlocks;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Fixed, ordered catalog of logical profiles for datapack-registered worlds.
 * Do not use DeferredRegister or register dimension IDs here.
 */
public final class DreamDimensionCatalog {
    public static final ResourceKey<Level> DREAMSCAPE = key("dreamscape");
    public static final ResourceKey<Level> BLUE_FIELD = key("campo_onirico_azul");

    private static final List<DreamDimensionDefinition> DEFINITIONS = List.of(
            new DreamDimensionDefinition(
                    DREAMSCAPE, true, 1, ModBlocks.SERENE_STONE_BLOCK, 12,
                    DimensionEffectPolicy.NONE, Optional.of(id("dreamscape_ambience")),
                    Optional.of(new DreamDimensionDefinition.Transition(
                            Level.OVERWORLD, DreamDimensionDefinition.TransitionVisual.DREAM_MESSAGE)),
                    Set.of(DreamTrait.FALL)),
            new DreamDimensionDefinition(
                    BLUE_FIELD, true, 1, ModBlocks.BLUE_DREAM_STONE, 24,
                    DimensionEffectPolicy.SLOW_FALLING,
                    Optional.of(id("blue_field")), Optional.empty(), Set.of())
    );
    private static final Map<ResourceKey<Level>, DreamDimensionDefinition> BY_DIMENSION;

    static {
        Map<ResourceKey<Level>, DreamDimensionDefinition> entries = new LinkedHashMap<>();
        for (DreamDimensionDefinition definition : DEFINITIONS) {
            if (entries.putIfAbsent(definition.dimension(), definition) != null) {
                throw new IllegalStateException("Duplicate dream dimension: " + definition.dimension());
            }
        }
        BY_DIMENSION = Map.copyOf(entries);
    }

    private DreamDimensionCatalog() {}

    public static List<DreamDimensionDefinition> all() {
        return DEFINITIONS;
    }

    public static Optional<DreamDimensionDefinition> find(ResourceKey<Level> dimension) {
        return Optional.ofNullable(BY_DIMENSION.get(dimension));
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, path);
    }

    private static ResourceKey<Level> key(String path) {
        return ResourceKey.create(Registries.DIMENSION, id(path));
    }
}
