package com.dreamsdimensions.mod.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Per-player, transient cycle guard: wake and tick consume the SAME attempt.
 * A new observed sleeping interval, not a duplicate wake event, resets the gate.
 */
public final class DreamSleepCycle {
    private final Set<UUID> activeSleep = new HashSet<>();
    private final Set<UUID> attemptedSleep = new HashSet<>();
    private final Set<UUID> completedWake = new HashSet<>();

    public boolean onSleeping(UUID uuid, int sleepTicks, int requiredTicks) {
        if (activeSleep.add(uuid)) {
            attemptedSleep.remove(uuid);
            completedWake.remove(uuid);
        }
        return sleepTicks >= requiredTicks && attemptedSleep.add(uuid);
    }

    public boolean onWake(UUID uuid, int sleepTicks, int requiredTicks) {
        return activeSleep.contains(uuid)
                && sleepTicks >= requiredTicks
                && attemptedSleep.add(uuid);
    }

    /** A completed wake is distinct from a dream-entry attempt made during sleep. */
    public boolean onCompletedWake(UUID uuid, int sleepTicks, int requiredTicks) {
        return activeSleep.contains(uuid) && sleepTicks >= requiredTicks
                && completedWake.add(uuid);
    }

    /** Keep attempted consumed until the next genuinely observed sleeping state. */
    public void onAwake(UUID uuid) { activeSleep.remove(uuid); }

    public void forget(UUID uuid) {
        activeSleep.remove(uuid);
        attemptedSleep.remove(uuid);
        completedWake.remove(uuid);
    }
}
