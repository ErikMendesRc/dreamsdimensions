package com.dreamsdimensions.mod.block;

/**
 * Day/night rules shared by wild herbs, planted seeds and the renderer state.
 * Pure clock policy: deterministic under /time set, day rollover and negative
 * game time. Maturity and flowering are deliberately separate from harvesting.
 */
public final class VigilHerbBloomPolicy {
    public static final int MATURE_AGE = 3;
    private static final long DAY_LENGTH = 24000L;
    private static final long NIGHT_START = 12000L;

    private VigilHerbBloomPolicy() { }

    public static boolean isNight(long overworldClockTime) {
        return Math.floorMod(overworldClockTime, DAY_LENGTH) >= NIGHT_START;
    }

    public static boolean shouldOpen(long overworldClockTime, int age) {
        return age >= MATURE_AGE && isNight(overworldClockTime);
    }

    public static boolean canGrow(long overworldClockTime, boolean skyVisible) {
        return !isNight(overworldClockTime) && skyVisible;
    }
}
