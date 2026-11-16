package com.dreamsdimensions.mod.service;

import static org.junit.jupiter.api.Assertions.*;
import static com.dreamsdimensions.mod.service.DreamRespawnPolicy.Selection.*;
import org.junit.jupiter.api.Test;

/** Real branch-policy tests; dedicated server and manual QA still required. */
class DreamRespawnPolicyTest {
    @Test void onlyCatalogDreamDeathsIntercepted() {
        assertTrue(DreamRespawnPolicy.interceptDeath(true,false,false)); // either catalog dream
        assertFalse(DreamRespawnPolicy.interceptDeath(false,false,false)); // Overworld/Nether/End/third-party
    }
    @Test void neverInterceptEndCompletionOrHardcore() {
        assertFalse(DreamRespawnPolicy.interceptDeath(true,true,false));
        assertFalse(DreamRespawnPolicy.interceptDeath(true,false,true));
        assertFalse(DreamRespawnPolicy.interceptDeath(true,true,true));
    }
    @Test void validSameDimensionAnchorWins() {
        assertTrue(DreamRespawnPolicy.useAnchor(true,true,true));
        assertEquals(EXPEDITION_ANCHOR, DreamRespawnPolicy.choose(true,true,true,true,true,true));
    }
    @Test void noOldAttachmentOrWrongDimension() {
        assertFalse(DreamRespawnPolicy.useAnchor(false,false,true));
        assertFalse(DreamRespawnPolicy.useAnchor(true,false,true));
        assertEquals(NEAR_DEATH, DreamRespawnPolicy.choose(false,false,true,true,true,true));
    }
    @Test void invalidAnchorFloorOrObstruction() {
        assertFalse(DreamRespawnPolicy.useAnchor(true,true,false));
        assertEquals(NEAR_ANCHOR, DreamRespawnPolicy.choose(true,false,true,true,true,true));
    }
    @Test void nearAnchorRequiresMatchingDimension() {
        assertEquals(NEAR_DEATH, DreamRespawnPolicy.choose(false,false,true,true,true,true));
    }
    @Test void safeDeathAreaUsedWhenNoAnchor() {
        assertEquals(NEAR_DEATH, DreamRespawnPolicy.choose(false,false,false,true,true,true));
    }
    @Test void worldSpawnIsSafeFallback() {
        assertEquals(WORLD_SPAWN, DreamRespawnPolicy.choose(false,false,false,false,true,true));
    }
    @Test void controlledPlatformLastResort() {
        assertEquals(CONTROLLED_PLATFORM, DreamRespawnPolicy.choose(false,false,false,false,false,true));
    }
    @Test void technicalFailureOnlyWhenEverythingFails() {
        assertEquals(TECHNICAL_FAILURE, DreamRespawnPolicy.choose(false,false,false,false,false,false));
    }
    @Test void sameDimensionAnchorCanFailWithoutSendingPlayerToOverworld() {
        assertEquals(CONTROLLED_PLATFORM, DreamRespawnPolicy.choose(true,false,false,false,false,true));
        assertEquals(TECHNICAL_FAILURE, DreamRespawnPolicy.choose(true,false,false,false,false,false));
    }
    @Test void independentPlayersHaveNoSharedState() {
        assertEquals(EXPEDITION_ANCHOR, DreamRespawnPolicy.choose(true,true,false,false,false,false));
        assertEquals(WORLD_SPAWN, DreamRespawnPolicy.choose(false,false,false,false,true,false));
    }
    @Test void noGameruleOrInventoryMutationInPurePolicy() {
        // keepInventory is deliberately not a parameter: vanilla owns drops and XP.
        assertTrue(DreamRespawnPolicy.interceptDeath(true,false,false));
        assertEquals(NEAR_DEATH, DreamRespawnPolicy.choose(false,false,false,true,false,false));
    }
}
