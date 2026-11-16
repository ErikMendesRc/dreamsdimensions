package com.dreamsdimensions.mod.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EchoMossEligibilityTest {
    @Test
    void onlyAliveSurvivalPlayersInDreamscapeWithEmptyMainHandQualify() {
        assertTrue(EchoMossEligibility.accepts(true, true, true, true, true));
        assertFalse(EchoMossEligibility.accepts(false, true, true, true, true));
        assertFalse(EchoMossEligibility.accepts(true, false, true, true, true));
        assertFalse(EchoMossEligibility.accepts(true, true, false, true, true));
        assertFalse(EchoMossEligibility.accepts(true, true, true, false, true));
        assertFalse(EchoMossEligibility.accepts(true, true, true, true, false));
    }
}
