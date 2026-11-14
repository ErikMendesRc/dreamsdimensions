package com.dreamsdimensions.mod.service;

/** Pure, independently testable restrictions for dream death/respawn. */
public final class DreamRespawnPolicy {
    public enum Selection {
        VANILLA, EXPEDITION_ANCHOR, NEAR_ANCHOR,
        NEAR_DEATH, WORLD_SPAWN, CONTROLLED_PLATFORM, TECHNICAL_FAILURE
    }

    private DreamRespawnPolicy() {}

    public static boolean interceptDeath(boolean isCatalogDream, boolean endReturn, boolean hardcore) {
        return isCatalogDream && !endReturn && !hardcore;
    }

    public static boolean useAnchor(boolean anchorPresent, boolean matchingDeathDimension, boolean validFloor) {
        return anchorPresent && matchingDeathDimension && validFloor;
    }

    public static Selection choose(boolean anchorMatches, boolean anchorSafe,
            boolean nearAnchorSafe, boolean nearDeathSafe, boolean worldSpawnSafe,
            boolean platformSafe) {
        if (useAnchor(anchorMatches, anchorMatches, anchorSafe)) return Selection.EXPEDITION_ANCHOR;
        if (anchorMatches && nearAnchorSafe) return Selection.NEAR_ANCHOR;
        if (nearDeathSafe) return Selection.NEAR_DEATH;
        if (worldSpawnSafe) return Selection.WORLD_SPAWN;
        if (platformSafe) return Selection.CONTROLLED_PLATFORM;
        return Selection.TECHNICAL_FAILURE;
    }
}
