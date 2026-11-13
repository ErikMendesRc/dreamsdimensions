package com.dreamsdimensions.mod.service;

/** Pure task 9.3 constraints tested without launching the game. */
public final class DreamRecoveryPolicy {
    public static final int CRACKED_USE_TICKS = 200;
    public static final int HANGOVER_NIGHTS = 3;
    public static final int GROUP_BED_RADIUS = 16;
    public static final int GROUP_WINDOW_TICKS = 100;

    private DreamRecoveryPolicy() {}

    public static boolean canGroup(int dx, int dy, int dz, long elapsedTicks) {
        long square = (long) dx * dx + (long) dy * dy + (long) dz * dz;
        return elapsedTicks >= 0 && elapsedTicks <= GROUP_WINDOW_TICKS
                && square <= (long) GROUP_BED_RADIUS * GROUP_BED_RADIUS;
    }

    public static boolean hangoverBlocks(int nightsRemaining, long lastBlockedNight, long nightIndex) {
        return nightIndex == lastBlockedNight || nightsRemaining > 0;
    }

    public static int hangoverNightsAfterSleep(int nightsRemaining,
            long lastBlockedNight, long nightIndex) {
        return lastBlockedNight == nightIndex ? nightsRemaining : Math.max(0, nightsRemaining - 1);
    }

    public static boolean returnConfirmed(boolean teleported, boolean samePlayer, boolean overworld) {
        return teleported && samePlayer && overworld;
    }
}
