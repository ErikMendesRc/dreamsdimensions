package com.dreamsdimensions.mod.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DreamSleepCycleTest {
    private static final int COMPLETE = 100;

    @Test void incompleteSleepDoesNotAttempt() {
        DreamSleepCycle cycle = new DreamSleepCycle();
        UUID id = UUID.randomUUID();
        assertFalse(cycle.onSleeping(id, 1, COMPLETE));
        assertFalse(cycle.onSleeping(id, 99, COMPLETE));
        assertFalse(cycle.onWake(id, 99, COMPLETE));
    }

    @Test void tickAndWakeUseOneAttempt() {
        DreamSleepCycle cycle = new DreamSleepCycle();
        UUID id = UUID.randomUUID();
        assertFalse(cycle.onSleeping(id, 99, COMPLETE));
        assertTrue(cycle.onSleeping(id, 100, COMPLETE));
        assertFalse(cycle.onSleeping(id, 101, COMPLETE));
        assertFalse(cycle.onWake(id, 100, COMPLETE));
        assertFalse(cycle.onWake(id, 100, COMPLETE));
    }

    @Test void wakeBeforeTickConsumesTheSameAttempt() {
        DreamSleepCycle cycle = new DreamSleepCycle();
        UUID id = UUID.randomUUID();
        assertFalse(cycle.onSleeping(id, 99, COMPLETE));
        assertTrue(cycle.onWake(id, 100, COMPLETE));
        assertFalse(cycle.onSleeping(id, 100, COMPLETE));
        assertFalse(cycle.onWake(id, 100, COMPLETE));
    }

    @Test void newCompletedSleepIsNewCycleButDuplicateWakeIsNot() {
        DreamSleepCycle cycle = new DreamSleepCycle();
        UUID id = UUID.randomUUID();
        cycle.onSleeping(id, 99, COMPLETE);
        assertTrue(cycle.onWake(id, 100, COMPLETE));
        cycle.onAwake(id);
        assertFalse(cycle.onWake(id, 100, COMPLETE));
        assertFalse(cycle.onSleeping(id, 1, COMPLETE));
        assertTrue(cycle.onSleeping(id, 100, COMPLETE));
    }

    @Test void interruptedSleepThenNewSleepCanAttempt() {
        DreamSleepCycle cycle = new DreamSleepCycle();
        UUID id = UUID.randomUUID();
        assertFalse(cycle.onSleeping(id, 40, COMPLETE));
        cycle.onAwake(id);
        assertFalse(cycle.onSleeping(id, 40, COMPLETE));
        assertTrue(cycle.onSleeping(id, 100, COMPLETE));
    }

    @Test void playersAreIndependent() {
        DreamSleepCycle cycle = new DreamSleepCycle();
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        assertTrue(cycle.onSleeping(a, 100, COMPLETE));
        assertTrue(cycle.onSleeping(b, 100, COMPLETE));
        assertFalse(cycle.onSleeping(a, 100, COMPLETE));
        assertFalse(cycle.onWake(b, 100, COMPLETE));
        cycle.onAwake(a);
        assertFalse(cycle.onWake(a, 100, COMPLETE));
        assertFalse(cycle.onSleeping(a, 10, COMPLETE));
        assertTrue(cycle.onSleeping(a, 100, COMPLETE));
        assertFalse(cycle.onSleeping(b, 110, COMPLETE));
    }

    @Test void logoutCloneOrDimensionChangeCanForgetCycle() {
        DreamSleepCycle cycle = new DreamSleepCycle();
        UUID id = UUID.randomUUID();
        assertTrue(cycle.onSleeping(id, 100, COMPLETE));
        cycle.forget(id);
        assertFalse(cycle.onWake(id, 100, COMPLETE));
        assertFalse(cycle.onSleeping(id, 1, COMPLETE));
        assertTrue(cycle.onSleeping(id, 100, COMPLETE));
    }
}
