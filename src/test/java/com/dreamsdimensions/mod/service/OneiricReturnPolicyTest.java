package com.dreamsdimensions.mod.service;

import static com.dreamsdimensions.mod.service.OneiricReturnPolicy.Result.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/** Real pure-policy JUnit checks; these do not impersonate a live Minecraft server. */
class OneiricReturnPolicyTest {
    private OneiricReturnPolicy.Result valid(boolean server, boolean alive,
            boolean spectator, boolean dream, boolean item, boolean nonempty, boolean cd) {
        return OneiricReturnPolicy.validate(server, alive, spectator, dream, item, nonempty, cd);
    }

    @Test void eligibleDreamPlayer() {
        assertEquals(SUCCESS, valid(true,true,false,true,true,true,false));
    }
    @Test void overworldCannotActivate() {
        assertEquals(INVALID_WORLD, valid(true,true,false,false,true,true,false));
    }
    @Test void invalidItem() {
        assertEquals(INVALID_ITEM_STATE, valid(true,true,false,true,false,true,false));
    }
    @Test void emptyItem() {
        assertEquals(INVALID_ITEM_STATE, valid(true,true,false,true,true,false,false));
    }
    @Test void invalidPlayer() {
        assertEquals(INVALID_PLAYER, valid(false,true,false,true,true,true,false));
    }
    @Test void deadPlayer() {
        assertEquals(INVALID_PLAYER, valid(true,false,false,true,true,true,false));
    }
    @Test void spectatorDenied() {
        assertEquals(SPECTATOR, valid(true,true,true,true,true,true,false));
    }
    @Test void cooldownDenied() {
        assertEquals(COOLDOWN, valid(true,true,false,true,true,true,true));
    }
    @Test void absentDestination() {
        assertEquals(INVALID_TARGET, OneiricReturnPolicy.confirm(false,false,false,false,false));
    }
    @Test void nullTeleport() {
        assertEquals(TELEPORT_REJECTED, OneiricReturnPolicy.confirm(true,false,false,false,false));
    }
    @Test void deadReturnedPlayer() {
        assertEquals(TELEPORT_REJECTED, OneiricReturnPolicy.confirm(true,true,false,true,true));
    }
    @Test void wrongPlayerUuid() {
        assertEquals(TELEPORT_REJECTED, OneiricReturnPolicy.confirm(true,true,true,false,true));
    }
    @Test void wrongDestination() {
        assertEquals(WRONG_DESTINATION, OneiricReturnPolicy.confirm(true,true,true,true,false));
    }
    @Test void confirmedTeleport() {
        assertEquals(SUCCESS, OneiricReturnPolicy.confirm(true,true,true,true,true));
    }
    @Test void survivalConsumesOne() {
        assertEquals(1, OneiricReturnPolicy.consumption(SUCCESS,false));
    }
    @Test void adventureConsumesOne() {
        assertEquals(1, OneiricReturnPolicy.consumption(SUCCESS,false));
    }
    @Test void creativeRetainsItem() {
        assertEquals(0, OneiricReturnPolicy.consumption(SUCCESS,true));
    }
    @Test void canceledUseNoConsumption() {
        assertEquals(0, OneiricReturnPolicy.consumption(INVALID_ITEM_STATE,false));
    }
    @Test void noConsumptionOnFailedTeleport() {
        for (var reason : new OneiricReturnPolicy.Result[]{INVALID_TARGET, TELEPORT_REJECTED,
                WRONG_DESTINATION, TELEPORT_EXCEPTION}) {
            assertEquals(0, OneiricReturnPolicy.consumption(reason,false), reason.toString());
        }
    }
    @Test void cooldownOnlyAfterConfirmedTeleport() {
        for (var reason : OneiricReturnPolicy.Result.values()) {
            assertEquals(reason == SUCCESS, OneiricReturnPolicy.shouldApplyCooldown(reason), reason.toString());
        }
    }
}
