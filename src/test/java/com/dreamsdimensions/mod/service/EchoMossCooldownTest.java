package com.dreamsdimensions.mod.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class EchoMossCooldownTest {
    @Test
    void blocksRepeatedSearchIncludingNegativeResults() {
        EchoMossCooldown cooldown = new EchoMossCooldown(100);
        UUID a = UUID.randomUUID();
        assertTrue(cooldown.tryAcquire(a, 300));
        assertFalse(cooldown.tryAcquire(a, 300));
        assertFalse(cooldown.tryAcquire(a, 399));
        assertTrue(cooldown.tryAcquire(a, 400));
    }

    @Test
    void playersAreIndependentAndCleanupWorks() {
        EchoMossCooldown cooldown = new EchoMossCooldown(100);
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        assertTrue(cooldown.tryAcquire(a, 1));
        assertTrue(cooldown.tryAcquire(b, 1));
        assertFalse(cooldown.tryAcquire(a, 10));
        assertFalse(cooldown.tryAcquire(b, 10));
        cooldown.clear(a);
        assertTrue(cooldown.tryAcquire(a, 11));
        assertFalse(cooldown.tryAcquire(b, 11));
        cooldown.clearAll();
        assertTrue(cooldown.tryAcquire(b, 12));
    }

    @Test
    void backwardsClockResetsInsteadOfDeadlocking() {
        EchoMossCooldown cooldown = new EchoMossCooldown(100);
        UUID a = UUID.randomUUID();
        assertTrue(cooldown.tryAcquire(a, 1000));
        assertTrue(cooldown.tryAcquire(a, 2));
        assertFalse(cooldown.tryAcquire(a, 3));
    }

    @Test
    void zeroOrNegativeDurationsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EchoMossCooldown(0));
        assertThrows(IllegalArgumentException.class, () -> new EchoMossCooldown(-1));
    }
}
