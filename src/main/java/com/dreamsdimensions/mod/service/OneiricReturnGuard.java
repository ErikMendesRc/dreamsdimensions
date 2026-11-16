package com.dreamsdimensions.mod.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Transient server-side one-activation guard. Captures the exact held stack
 * object, not an item type or inventory-wide search. A successful teleport
 * may fire ChangedDimension synchronously; an in-flight activation cannot be
 * canceled or restarted during that event.
 *
 * No Player/Level references and no new NBT or saved-data schema.
 */
public final class OneiricReturnGuard {
    public enum Hand { MAIN_HAND, OFF_HAND }
    private record Activation(Hand hand, Object stack, boolean resolving) {}

    private final Map<UUID, Activation> active = new HashMap<>();

    public boolean begin(UUID player, Hand hand, Object stack) {
        if (stack == null) return false;
        Activation existing = active.get(player);
        if (existing != null && existing.resolving()) return false;
        active.put(player, new Activation(hand, stack, false));
        return true;
    }

    /** Claims once only, and only for the original hand + exact stack instance. */
    public boolean claim(UUID player, Hand hand, Object stack) {
        Activation existing = active.get(player);
        if (existing == null || existing.resolving()
                || existing.hand() != hand || existing.stack() != stack) return false;
        active.put(player, new Activation(hand, stack, true));
        return true;
    }

    /** Interrupted usage, relog, death, or administrative dimension change. */
    public void cancel(UUID player) {
        Activation existing = active.get(player);
        if (existing != null && !existing.resolving()) active.remove(player);
    }

    /** Always invoked in finally by the server after claiming the attempt. */
    public void finish(UUID player) {
        active.remove(player);
    }

    public void clear() {
        active.clear();
    }

    public boolean isResolving(UUID player) {
        Activation existing = active.get(player);
        return existing != null && existing.resolving();
    }
}
