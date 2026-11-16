package com.dreamsdimensions.mod.dimension;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class WeightedDimensionPickerTest {
    @Test
    void emptyListHasNoDestination() {
        assertTrue(WeightedDimensionPicker.order(List.of(), new Random(8)).isEmpty());
    }

    @Test
    void eachLoadedAndEnabledDestinationAppearsExactlyOnce() {
        var options = List.of(
                new WeightedDimensionPicker.Candidate<>("dreamscape", 1),
                new WeightedDimensionPicker.Candidate<>("campo_onirico_azul", 1));
        for (int seed = 0; seed < 32; seed++) {
            List<String> order = WeightedDimensionPicker.order(options, new Random(seed));
            assertEquals(2, order.size());
            assertTrue(order.contains("dreamscape"));
            assertTrue(order.contains("campo_onirico_azul"));
            assertNotEquals(order.get(0), order.get(1));
        }
    }

    @Test
    void unavailableDestinationIsExcludedBeforeDraw() {
        // The server filters unavailable levels before calling the pure picker.
        var loaded = List.of(new WeightedDimensionPicker.Candidate<>("dreamscape", 1));
        assertEquals(List.of("dreamscape"), WeightedDimensionPicker.order(loaded, new Random(7)));
    }

    @Test
    void aFailedFirstTargetStillLeavesTheSecondForFallback() {
        var options = List.of(
                new WeightedDimensionPicker.Candidate<>("a", 1),
                new WeightedDimensionPicker.Candidate<>("b", 1));
        List<String> order = WeightedDimensionPicker.order(options, new Random(12));
        assertEquals(2, order.size());
        assertNotEquals(order.get(0), order.get(1));
    }

    @Test
    void weightsActuallyInfluenceFirstDestination() {
        var options = List.of(
                new WeightedDimensionPicker.Candidate<>("heavy", 3),
                new WeightedDimensionPicker.Candidate<>("light", 1));
        Random random = new Random(1234);
        int heavyFirst = 0;
        for (int i = 0; i < 4000; i++) {
            if (WeightedDimensionPicker.order(options, random).get(0).equals("heavy")) {
                heavyFirst++;
            }
        }
        assertTrue(heavyFirst > 2700 && heavyFirst < 3300, "3:1 first-choice weight");
    }

    @Test
    void neverAcceptZeroOrNegativeWeightAndNeverMutateInput() {
        assertThrows(IllegalArgumentException.class,
                () -> new WeightedDimensionPicker.Candidate<>("bad", 0));
        assertThrows(IllegalArgumentException.class,
                () -> new WeightedDimensionPicker.Candidate<>("bad", -1));
        var immutable = List.of(new WeightedDimensionPicker.Candidate<>("ok", 1));
        assertEquals(List.of("ok"), WeightedDimensionPicker.order(immutable, new Random()));
        assertEquals(1, immutable.size());
    }
}
