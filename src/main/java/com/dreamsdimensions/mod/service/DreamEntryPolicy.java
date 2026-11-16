package com.dreamsdimensions.mod.service;

import java.util.Objects;
import java.util.function.DoubleSupplier;

/** Pure gameplay policy; entry chance and destination weighting are distinct draws. */
public final class DreamEntryPolicy {
    private DreamEntryPolicy() {}

    public record Input(boolean enabled, boolean alive, boolean overworld, boolean spectator,
                        boolean fullSleep, boolean necklaceEquipped, boolean loadedDestination) {}

    public static boolean canRoll(Input input) {
        Objects.requireNonNull(input, "input");
        return input.enabled() && input.alive() && input.overworld() && !input.spectator()
                && input.fullSleep() && input.necklaceEquipped() && input.loadedDestination();
    }

    public static boolean roll(double chance, DoubleSupplier random) {
        if (!Double.isFinite(chance) || chance < 0.0D || chance > 1.0D) {
            throw new IllegalArgumentException("dream_entry_chance must be in [0, 1]");
        }
        Objects.requireNonNull(random, "random");
        if (chance == 0.0D) return false;
        if (chance == 1.0D) return true;
        return random.getAsDouble() < chance;
    }
}
