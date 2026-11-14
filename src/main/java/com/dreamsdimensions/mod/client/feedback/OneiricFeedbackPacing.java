package com.dreamsdimensions.mod.client.feedback;

/**
 * Pure timing/budget contract for the local Awakener visual feedback.
 * Never influences return eligibility, duration, inventory or teleport.
 */
public final class OneiricFeedbackPacing {
    public static final int DURATION = 60;

    private OneiricFeedbackPacing() {}

    public static int particleCount(long useTicks, boolean minimal, boolean decreased) {
        if (minimal || useTicks < 1 || useTicks > DURATION) return 0;
        if (decreased) return useTicks % 12 == 0 ? 1 : 0;
        if (useTicks % 6 != 0) return 0;
        return useTicks >= 42 ? 2 : 1;
    }
}
