package com.dreamsdimensions.mod.service;

/** Pure gate for the empty-main-hand Survival interaction. */
public final class EchoMossEligibility {
    private EchoMossEligibility() {}

    public static boolean accepts(boolean alive, boolean survival, boolean dreamscape,
            boolean mainHand, boolean emptyHand) {
        return alive && survival && dreamscape && mainHand && emptyHand;
    }
}
