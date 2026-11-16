package com.dreamsdimensions.mod.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DreamFallPolicyTest {
    @Test void fallReductionIsCappedAtTwoLiningLevels() {
        assertEquals(1.0f, DreamFallPolicy.damageMultiplier(0, false), 0.0001f);
        assertEquals(0.65f, DreamFallPolicy.damageMultiplier(1, false), 0.0001f);
        assertEquals(0.4225f, DreamFallPolicy.damageMultiplier(2, false), 0.0001f);
        assertEquals(DreamFallPolicy.damageMultiplier(2, false),
                     DreamFallPolicy.damageMultiplier(4, false), 0.0001f);
        assertEquals(0, DreamFallPolicy.effectiveLiningLevel(-3));
    }

    @Test void infusionCombinesWithArmorWithoutBecomingAnAbsoluteImmunity() {
        assertEquals(0.5f, DreamFallPolicy.damageMultiplier(0, true), 0.0001f);
        assertEquals(0.21125f, DreamFallPolicy.damageMultiplier(4, true), 0.0001f);
        assertTrue(DreamFallPolicy.damageMultiplier(4, true) > 0.0f);
    }

    @Test void tetherNeverRearmsAnActiveProtectionOrUsesOtherDreams() {
        assertTrue(DreamFallPolicy.mayArmTether(true, true, true, false));
        assertFalse(DreamFallPolicy.mayArmTether(true, true, true, true));
        assertFalse(DreamFallPolicy.mayArmTether(false, true, true, false));
        assertFalse(DreamFallPolicy.mayArmTether(true, false, true, false));
        assertFalse(DreamFallPolicy.mayArmTether(true, true, false, false));
    }
}
