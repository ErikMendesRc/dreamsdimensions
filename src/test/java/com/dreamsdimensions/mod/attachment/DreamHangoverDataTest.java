package com.dreamsdimensions.mod.attachment;

import com.dreamsdimensions.mod.service.DreamRecoveryPolicy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Unit test of pure night rules; codec integration is covered by server boot. */
class DreamHangoverDataTest {
    @Test void consumesOnlyOneLockPerDistinctNight() {
        int left = DreamRecoveryPolicy.HANGOVER_NIGHTS;
        long last = Long.MIN_VALUE;
        for (int day = 100; day <= 102; day++) {
            assertTrue(DreamRecoveryPolicy.hangoverBlocks(left, last, day));
            left = DreamRecoveryPolicy.hangoverNightsAfterSleep(left, last, day);
            last = day;
            assertEquals(102 - day, left);
            assertTrue(DreamRecoveryPolicy.hangoverBlocks(left, last, day));
            assertEquals(left, DreamRecoveryPolicy.hangoverNightsAfterSleep(left, last, day));
        }
        assertFalse(DreamRecoveryPolicy.hangoverBlocks(left, last, 103));
    }
}
