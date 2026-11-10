package com.dreamsdimensions.mod.entity.behavior;

/**
 * Stateless, deterministic thresholds for one-use dream anchors.
 * Different future guardians can use a different policy without rewriting
 * the recall service. No Minecraft runtime or entity references.
 */
public record DreamAnchorPolicy(int durationTicks, int fallBelowAnchorBlocks,
        int maxHorizontalBlocks, double verticalVelocityTrigger) {
    public DreamAnchorPolicy {
        if (durationTicks <= 0 || fallBelowAnchorBlocks <= 0 || maxHorizontalBlocks <= 0
                || verticalVelocityTrigger >= 0) {
            throw new IllegalArgumentException("Invalid dream anchor policy");
        }
    }

    public boolean isActive(long now, long expiresAt) {
        return now < expiresAt;
    }

    public boolean insideHorizontalArea(double dx, double dz) {
        return dx * dx + dz * dz <= (double) maxHorizontalBlocks * maxHorizontalBlocks;
    }

    public boolean shouldRecall(double playerY, double verticalVelocity, int anchorY) {
        return playerY < anchorY - fallBelowAnchorBlocks
                && verticalVelocity < verticalVelocityTrigger;
    }
}
