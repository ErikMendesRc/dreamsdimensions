package com.dreamsdimensions.mod.dimension;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Immutable logical settings for an EXISTING dimension. This does not register
 * the dimension in Minecraft's world/dimension registries.
 */
public record DreamDimensionDefinition(
        ResourceKey<Level> dimension,
        boolean sleepDestination,
        int selectionWeight,
        Supplier<? extends Block> fallbackFloor,
        int naturalSpawnRadius,
        DimensionEffectPolicy effectPolicy,
        Optional<Identifier> atmosphereId,
        Optional<Transition> transition,
        Set<DreamTrait> traits
) {
    public DreamDimensionDefinition {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(fallbackFloor, "fallbackFloor");
        Objects.requireNonNull(effectPolicy, "effectPolicy");
        Objects.requireNonNull(atmosphereId, "atmosphereId");
        Objects.requireNonNull(transition, "transition");
        traits = Set.copyOf(Objects.requireNonNull(traits, "traits"));
        if (traits.size() > 3) throw new IllegalArgumentException("At most three traits per dream");
        if (dimension == Level.OVERWORLD) {
            throw new IllegalArgumentException("Overworld is not a dream dimension");
        }
        if (selectionWeight < 0 || (sleepDestination && selectionWeight == 0)) {
            throw new IllegalArgumentException("Invalid sleep selection weight: " + selectionWeight);
        }
        if (naturalSpawnRadius < 0 || naturalSpawnRadius > 32) {
            throw new IllegalArgumentException("Unsafe natural spawn search radius: " + naturalSpawnRadius);
        }
    }

    /** Existing client loading screen policy for a given incoming transition. */
    public record Transition(ResourceKey<Level> fromDimension, TransitionVisual visual) {
        public Transition {
            Objects.requireNonNull(fromDimension, "fromDimension");
            Objects.requireNonNull(visual, "visual");
        }
    }

    public enum TransitionVisual { DREAM_MESSAGE }
}
