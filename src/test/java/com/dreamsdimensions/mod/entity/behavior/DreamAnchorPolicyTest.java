package com.dreamsdimensions.mod.entity.behavior;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/** Real deterministic unit tests: threshold and radius edge cases. */
class DreamAnchorPolicyTest {
    private final DreamAnchorPolicy policy = new DreamAnchorPolicy(2400, 12, 48, -0.10);

    @Test
    void onlyFallsDeepBelowPerchQualify() {
        assertFalse(policy.shouldRecall(189, -0.6, 200));
        assertFalse(policy.shouldRecall(188, -0.6, 200));
        assertFalse(policy.shouldRecall(185, -0.09, 200));
        assertTrue(policy.shouldRecall(185, -0.6, 200));
    }

    @Test
    void proximityAndExpiryAreBounded() {
        assertTrue(policy.insideHorizontalArea(48, 0));
        assertFalse(policy.insideHorizontalArea(48.1, 0));
        assertFalse(policy.insideHorizontalArea(34, 35));
        assertTrue(policy.isActive(2399, 2400));
        assertFalse(policy.isActive(2400, 2400));
    }

    @Test
    void invalidPoliciesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new DreamAnchorPolicy(0, 12, 48, -0.1));
        assertThrows(IllegalArgumentException.class,
                () -> new DreamAnchorPolicy(2400, 12, 48, 0));
    }
}
