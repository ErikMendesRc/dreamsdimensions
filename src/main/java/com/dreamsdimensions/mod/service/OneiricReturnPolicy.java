package com.dreamsdimensions.mod.service;

/**
 * Pure decisions for the existing Awakener return path; does not perform
 * world changes, touch inventory or depend on Minecraft runtime objects.
 */
public final class OneiricReturnPolicy {
    public enum Result {
        SUCCESS, INVALID_PLAYER, INVALID_WORLD, INVALID_ITEM_STATE,
        SPECTATOR, COOLDOWN, INVALID_TARGET, TELEPORT_REJECTED,
        WRONG_DESTINATION, TELEPORT_EXCEPTION
    }

    private OneiricReturnPolicy() {}

    public static Result validate(boolean serverPlayer, boolean alive, boolean spectator,
            boolean allowedDream, boolean correctItem, boolean nonempty, boolean cooldown) {
        if (!serverPlayer || !alive) return Result.INVALID_PLAYER;
        if (spectator) return Result.SPECTATOR;
        if (!allowedDream) return Result.INVALID_WORLD;
        if (!correctItem || !nonempty) return Result.INVALID_ITEM_STATE;
        if (cooldown) return Result.COOLDOWN;
        return Result.SUCCESS;
    }

    public static Result confirm(boolean transitionPrepared, boolean resultPresent,
            boolean resultAlive, boolean sameUuid, boolean overworld) {
        if (!transitionPrepared) return Result.INVALID_TARGET;
        if (!resultPresent) return Result.TELEPORT_REJECTED;
        if (!sameUuid || !resultAlive) return Result.TELEPORT_REJECTED;
        if (!overworld) return Result.WRONG_DESTINATION;
        return Result.SUCCESS;
    }

    /** Creative (instabuild) retains its item just like vanilla. */
    public static int consumption(Result result, boolean instabuild) {
        return result == Result.SUCCESS && !instabuild ? 1 : 0;
    }

    public static boolean shouldApplyCooldown(Result result) {
        return result == Result.SUCCESS;
    }
}
