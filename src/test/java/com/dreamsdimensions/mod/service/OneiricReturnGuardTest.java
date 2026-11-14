package com.dreamsdimensions.mod.service;

import static com.dreamsdimensions.mod.service.OneiricReturnGuard.Hand.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OneiricReturnGuardTest {
    @Test void mainHandCanClaimExactlyOnce() {
        var guard = new OneiricReturnGuard();
        UUID user = UUID.randomUUID();
        Object stack = new Object();
        assertTrue(guard.begin(user,MAIN_HAND,stack));
        assertTrue(guard.claim(user,MAIN_HAND,stack));
        assertFalse(guard.claim(user,MAIN_HAND,stack));
        guard.finish(user);
        assertFalse(guard.claim(user,MAIN_HAND,stack));
    }

    @Test void offHandCanClaimExactlyOnce() {
        var guard = new OneiricReturnGuard();
        UUID user = UUID.randomUUID();
        Object stack = new Object();
        assertTrue(guard.begin(user,OFF_HAND,stack));
        assertTrue(guard.claim(user,OFF_HAND,stack));
        guard.finish(user);
        assertFalse(guard.claim(user,OFF_HAND,stack));
    }

    @Test void wrongHandCannotCommit() {
        var guard = new OneiricReturnGuard();
        UUID user = UUID.randomUUID();
        Object stack = new Object();
        guard.begin(user,MAIN_HAND,stack);
        assertFalse(guard.claim(user,OFF_HAND,stack));
        assertTrue(guard.claim(user,MAIN_HAND,stack));
    }

    @Test void otherIdenticalLookingStackDoesNotQualify() {
        var guard = new OneiricReturnGuard();
        UUID user = UUID.randomUUID();
        String stack = new String("same-item");
        String otherItem = new String("same-item");
        assertEquals(stack, otherItem);
        assertNotSame(stack, otherItem);
        guard.begin(user,MAIN_HAND,stack);
        assertFalse(guard.claim(user,MAIN_HAND,otherItem));
        assertTrue(guard.claim(user,MAIN_HAND,stack));
    }

    @Test void anotherInventorySlotCannotBeSearchedForCommit() {
        var guard = new OneiricReturnGuard();
        UUID user = UUID.randomUUID();
        Object heldStack = new Object(), spareStack = new Object();
        guard.begin(user,OFF_HAND,heldStack);
        assertFalse(guard.claim(user,OFF_HAND,spareStack));
        assertTrue(guard.claim(user,OFF_HAND,heldStack));
    }

    @Test void independentPlayersCanFinishConcurrently() {
        var guard = new OneiricReturnGuard();
        UUID alice = UUID.randomUUID(), bob = UUID.randomUUID();
        Object a = new Object(), b = new Object();
        guard.begin(alice,MAIN_HAND,a);
        guard.begin(bob,MAIN_HAND,b);
        assertTrue(guard.claim(alice,MAIN_HAND,a));
        assertTrue(guard.claim(bob,MAIN_HAND,b));
        guard.finish(alice);
        assertTrue(guard.isResolving(bob));
        guard.finish(bob);
        assertFalse(guard.isResolving(alice));
    }

    @Test void otherPlayersActivationCannotBeClaimed() {
        var guard = new OneiricReturnGuard();
        UUID alice = UUID.randomUUID(), bob = UUID.randomUUID();
        Object a = new Object();
        guard.begin(alice,MAIN_HAND,a);
        assertFalse(guard.claim(bob,MAIN_HAND,a));
    }

    @Test void cancelAfterReleasePreventsTeleport() {
        var guard = new OneiricReturnGuard();
        UUID id = UUID.randomUUID();
        Object a = new Object();
        guard.begin(id,MAIN_HAND,a);
        guard.cancel(id);
        assertFalse(guard.claim(id,MAIN_HAND,a));
    }

    @Test void cancelledTickOneTwentyOrFiftyNineAreEquivalent() {
        for (int ticks : new int[]{1,20,59}) {
            var guard = new OneiricReturnGuard();
            UUID id = UUID.randomUUID();
            Object a = new Object();
            guard.begin(id,MAIN_HAND,a);
            guard.cancel(id);
            assertFalse(guard.claim(id,MAIN_HAND,a), "ticks=" + ticks);
        }
    }

    @Test void dimensionChangeDuringTeleportDoesNotCancelClaim() {
        var guard = new OneiricReturnGuard();
        UUID id = UUID.randomUUID();
        Object a = new Object();
        guard.begin(id,MAIN_HAND,a);
        assertTrue(guard.claim(id,MAIN_HAND,a));
        guard.cancel(id); // NeoForge ChangedDimensionEvent fires inside teleport
        assertTrue(guard.isResolving(id));
        assertFalse(guard.claim(id,MAIN_HAND,a));
        guard.finish(id);
        assertFalse(guard.isResolving(id));
    }

    @Test void secondStartCannotOverwriteInflightTicket() {
        var guard = new OneiricReturnGuard();
        UUID id = UUID.randomUUID();
        Object a = new Object();
        guard.begin(id,MAIN_HAND,a);
        assertTrue(guard.claim(id,MAIN_HAND,a));
        assertFalse(guard.begin(id,OFF_HAND,new Object()));
        assertFalse(guard.claim(id,OFF_HAND,new Object()));
        guard.finish(id);
    }

    @Test void newActivationMayReplaceAbandonedUnclaimedCycle() {
        var guard = new OneiricReturnGuard();
        UUID id = UUID.randomUUID();
        Object a = new Object(), b = new Object();
        guard.begin(id,MAIN_HAND,a);
        assertTrue(guard.begin(id,OFF_HAND,b));
        assertFalse(guard.claim(id,MAIN_HAND,a));
        assertTrue(guard.claim(id,OFF_HAND,b));
    }

    @Test void logoutDeathAndAdminChangeCancelIdleActivation() {
        for (int scenario=0; scenario<3; scenario++) {
            var guard = new OneiricReturnGuard();
            UUID id = UUID.randomUUID();
            Object stack = new Object();
            guard.begin(id,MAIN_HAND,stack);
            guard.cancel(id);
            assertFalse(guard.claim(id,MAIN_HAND,stack));
        }
    }

    @Test void serverStopClearsAllTickets() {
        var guard = new OneiricReturnGuard();
        UUID id = UUID.randomUUID();
        Object stack = new Object();
        guard.begin(id,MAIN_HAND,stack);
        guard.clear();
        assertFalse(guard.claim(id,MAIN_HAND,stack));
    }

    @Test void noUseOrNullStackCannotClaim() {
        var guard = new OneiricReturnGuard();
        UUID id = UUID.randomUUID();
        assertFalse(guard.begin(id,MAIN_HAND,null));
        assertFalse(guard.claim(id,MAIN_HAND,new Object()));
    }

    @Test void replayAfterCommittedActivationIsRejected() {
        var guard = new OneiricReturnGuard();
        UUID id = UUID.randomUUID();
        Object stack = new Object();
        guard.begin(id,MAIN_HAND,stack);
        assertTrue(guard.claim(id,MAIN_HAND,stack));
        guard.finish(id);
        assertFalse(guard.claim(id,MAIN_HAND,stack));
    }
}
