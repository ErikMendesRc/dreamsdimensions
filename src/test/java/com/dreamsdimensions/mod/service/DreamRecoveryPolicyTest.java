package com.dreamsdimensions.mod.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DreamRecoveryPolicyTest {
    @Test void crackedReturnIsSlowerAndNeverAFreeTeleport() {
        assertEquals(200, DreamRecoveryPolicy.CRACKED_USE_TICKS);
        assertEquals(3, DreamRecoveryPolicy.HANGOVER_NIGHTS);
        assertTrue(DreamRecoveryPolicy.returnConfirmed(true, true, true));
        assertFalse(DreamRecoveryPolicy.returnConfirmed(false, true, true));
        assertFalse(DreamRecoveryPolicy.returnConfirmed(true, false, true));
        assertFalse(DreamRecoveryPolicy.returnConfirmed(true, true, false));
    }

    @Test void nearbyBedsHaveBoundedDeterministicGroupWindow() {
        assertTrue(DreamRecoveryPolicy.canGroup(12, 0, 10, 15));
        assertTrue(DreamRecoveryPolicy.canGroup(16, 0, 0, 100));
        assertFalse(DreamRecoveryPolicy.canGroup(16, 0, 1, 100));
        assertFalse(DreamRecoveryPolicy.canGroup(1, 0, 1, -1));
        assertFalse(DreamRecoveryPolicy.canGroup(1, 0, 1, 101));
        assertFalse(DreamRecoveryPolicy.canGroup(200000, 0, 0, 1));
    }
}
