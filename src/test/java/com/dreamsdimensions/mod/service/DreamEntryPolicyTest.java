package com.dreamsdimensions.mod.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DreamEntryPolicyTest {
    private static DreamEntryPolicy.Input yes() {
        return new DreamEntryPolicy.Input(true, true, true, false, true, true, true);
    }

    @Test void allConditionsRequired() {
        assertTrue(DreamEntryPolicy.canRoll(yes()));
        assertFalse(DreamEntryPolicy.canRoll(new DreamEntryPolicy.Input(false, true, true, false, true, true, true)));
        assertFalse(DreamEntryPolicy.canRoll(new DreamEntryPolicy.Input(true, false, true, false, true, true, true)));
        assertFalse(DreamEntryPolicy.canRoll(new DreamEntryPolicy.Input(true, true, false, false, true, true, true)));
        assertFalse(DreamEntryPolicy.canRoll(new DreamEntryPolicy.Input(true, true, true, true, true, true, true)));
        assertFalse(DreamEntryPolicy.canRoll(new DreamEntryPolicy.Input(true, true, true, false, false, true, true)));
        assertFalse(DreamEntryPolicy.canRoll(new DreamEntryPolicy.Input(true, true, true, false, true, false, true)));
        assertFalse(DreamEntryPolicy.canRoll(new DreamEntryPolicy.Input(true, true, true, false, true, true, false)));
    }

    @Test void boundaryProbabilitiesNeverDrawRandom() {
        AtomicInteger draws = new AtomicInteger();
        assertFalse(DreamEntryPolicy.roll(0.0, () -> { draws.incrementAndGet(); return 0; }));
        assertTrue(DreamEntryPolicy.roll(1.0, () -> { draws.incrementAndGet(); return 0.99; }));
        assertEquals(0, draws.get());
    }

    @Test void deterministicIntermediateProbability() {
        assertTrue(DreamEntryPolicy.roll(0.30, () -> 0.299999));
        assertFalse(DreamEntryPolicy.roll(0.30, () -> 0.30));
        assertFalse(DreamEntryPolicy.roll(0.30, () -> 0.84));
    }

    @Test void rejectsOutOfRangeAndNonFiniteValues() {
        assertThrows(IllegalArgumentException.class, () -> DreamEntryPolicy.roll(-0.01, () -> 0.2));
        assertThrows(IllegalArgumentException.class, () -> DreamEntryPolicy.roll(1.01, () -> 0.2));
        assertThrows(IllegalArgumentException.class, () -> DreamEntryPolicy.roll(Double.NaN, () -> 0.2));
        assertThrows(IllegalArgumentException.class, () -> DreamEntryPolicy.roll(Double.POSITIVE_INFINITY, () -> 0.2));
    }

    @Test void doesNotDrawWhenIneligibleAndKeepsEntrySeparateFromWeights() {
        AtomicInteger teleports = new AtomicInteger();
        DreamEntryPolicy.Input offhandOnly = new DreamEntryPolicy.Input(true, true, true, false, true, false, true);
        if (DreamEntryPolicy.canRoll(offhandOnly) && DreamEntryPolicy.roll(1.0, () -> 0.0)) {
            teleports.incrementAndGet();
        }
        assertEquals(0, teleports.get());
        // Destination weights are resolved later by WeightedDimensionPicker.
        assertTrue(DreamEntryPolicy.canRoll(yes()));
    }
}
