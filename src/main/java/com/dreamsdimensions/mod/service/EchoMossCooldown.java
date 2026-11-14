package com.dreamsdimensions.mod.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-thread-only, transient per-player tick cooldown.
 * Stores no Player, Level, chunks or persistent attachments.
 */
public final class EchoMossCooldown {
    private final int durationTicks;
    private final Map<UUID, Long> lastUse = new HashMap<>();

    public EchoMossCooldown(int durationTicks) {
        if (durationTicks <= 0) throw new IllegalArgumentException("Duration must be positive");
        this.durationTicks = durationTicks;
    }

    /** Acquires the cooldown before scanning, even if no target is found. */
    public boolean tryAcquire(UUID playerId, long gameTime) {
        Long previous = lastUse.get(playerId);
        if (previous != null && gameTime >= previous
                && gameTime - previous < durationTicks) return false;
        // Backwards time resets this entry instead of locking it indefinitely.
        lastUse.put(playerId, gameTime);
        return true;
    }

    public void clear(UUID playerId) {
        lastUse.remove(playerId);
    }

    public void clearAll() {
        lastUse.clear();
    }
}
