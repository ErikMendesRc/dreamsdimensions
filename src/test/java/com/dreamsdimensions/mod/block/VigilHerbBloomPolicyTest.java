package com.dreamsdimensions.mod.block;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VigilHerbBloomPolicyTest {
    @Test
    void matureFlowerOpensAtNightAndClosesAtDawn() {
        for (long tick : new long[]{12000, 13000, 18000, 23999, 36000, -1}) {
            assertTrue(VigilHerbBloomPolicy.shouldOpen(tick, 3), "night " + tick);
        }
        for (long tick : new long[]{0, 1000, 6000, 11999, 24000, 25000, -12001}) {
            assertFalse(VigilHerbBloomPolicy.shouldOpen(tick, 3), "day " + tick);
        }
    }

    @Test
    void seedlingCannotFlowerAtNight() {
        for (int age = 0; age < 3; age++) {
            assertFalse(VigilHerbBloomPolicy.shouldOpen(18000, age));
        }
    }

    @Test
    void growingRemainsDaylightOnly() {
        assertTrue(VigilHerbBloomPolicy.canGrow(6000, true));
        assertFalse(VigilHerbBloomPolicy.canGrow(18000, true));
        assertFalse(VigilHerbBloomPolicy.canGrow(6000, false));
    }
}
