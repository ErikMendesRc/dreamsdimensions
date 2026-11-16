package com.dreamsdimensions.mod.service;

/**
 * Pure rules shared by every dream carrying the FALL trait.
 * Independent of Minecraft registries for deterministic unit testing.
 */
public final class DreamFallPolicy {
    public static final int MAX_LINING_LEVEL = 2;
    public static final float LINING_MULTIPLIER = 0.65F;
    public static final float LIGHTNESS_MULTIPLIER = 0.5F;

    private DreamFallPolicy() {}

    public static int effectiveLiningLevel(int equippedPieces) {
        return Math.max(0, Math.min(MAX_LINING_LEVEL, equippedPieces));
    }

    public static float damageMultiplier(int equippedPieces, boolean infusionActive) {
        float result = 1.0F;
        for (int i = 0; i < effectiveLiningLevel(equippedPieces); i++) {
            result *= LINING_MULTIPLIER;
        }
        if (infusionActive) result *= LIGHTNESS_MULTIPLIER;
        return result;
    }

    public static boolean mayArmTether(boolean dreamHasFall, boolean grounded,
            boolean safeFloor, boolean alreadyActive) {
        return dreamHasFall && grounded && safeFloor && !alreadyActive;
    }
}
