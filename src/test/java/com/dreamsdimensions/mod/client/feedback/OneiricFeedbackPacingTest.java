package com.dreamsdimensions.mod.client.feedback;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/** Task 8.5 purely local rendering cadence; does not claim a live client test. */
class OneiricFeedbackPacingTest {
    @Test void allParticlesAreBoundedAcrossOneUse() {
        int sum = 0;
        for (int t = 1; t <= 60; t++) {
            int n = OneiricFeedbackPacing.particleCount(t, false, false);
            assertTrue(n >= 0 && n <= 2, "tick=" + t);
            sum += n;
        }
        assertEquals(14, sum);
    }

    @Test void decreasedParticlesBudgetStaysLow() {
        int sum = 0;
        for (int t = 1; t <= 60; t++) {
            int n = OneiricFeedbackPacing.particleCount(t, false, true);
            assertTrue(n >= 0 && n <= 1, "tick=" + t);
            sum += n;
        }
        assertEquals(5, sum);
    }

    @Test void minimalNeverDrawsEvenAtCompletion() {
        for (int t = 0; t <= 65; t++) {
            assertEquals(0, OneiricFeedbackPacing.particleCount(t, true, false));
        }
    }

    @Test void feedbackCannotPersistPastUseWindow() {
        assertEquals(0, OneiricFeedbackPacing.particleCount(0, false, false));
        assertEquals(0, OneiricFeedbackPacing.particleCount(61, false, false));
        assertEquals(0, OneiricFeedbackPacing.particleCount(600, false, true));
    }

    @Test void finishPhaseIsDistinctButNotARewardTrigger() {
        assertEquals(1, OneiricFeedbackPacing.particleCount(36, false, false));
        assertEquals(2, OneiricFeedbackPacing.particleCount(42, false, false));
        assertEquals(2, OneiricFeedbackPacing.particleCount(60, false, false));
    }
}
