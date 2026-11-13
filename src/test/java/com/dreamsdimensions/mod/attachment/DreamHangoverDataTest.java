package com.dreamsdimensions.mod.attachment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DreamHangoverDataTest {
    @Test void consumesOnlyOneLockPerDistinctNight() {
        DreamHangoverData initial = DreamHangoverData.fresh();
        assertEquals(3, initial.nightsRemaining());
        DreamHangoverData first = initial.blockNight(100);
        assertEquals(2, first.nightsRemaining());
        assertTrue(first.blocks(100));
        assertEquals(first, first.blockNight(100));
        DreamHangoverData second = first.blockNight(101);
        DreamHangoverData third = second.blockNight(102);
        assertEquals(0, third.nightsRemaining());
        assertTrue(third.blocks(102));
        assertFalse(third.blocks(103));
        assertEquals(third, third.blockNight(103));
    }
}
