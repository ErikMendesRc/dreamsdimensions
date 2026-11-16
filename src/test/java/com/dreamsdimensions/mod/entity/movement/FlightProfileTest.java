package com.dreamsdimensions.mod.entity.movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class FlightProfileTest {
    @Test
    void preservesFragmentoPhysicalCoefficients() {
        FlightProfile fragmento = new FlightProfile(
                0.17, 0.08, 0.19, 0.028, 0.78, 0.70, 0.50, 0.10,
                0.018, 0.015, 0.18, 0.87, 0.12, 0.14, 0.035, 64.0);
        assertEquals(0.19, fragmento.maximumSpeed());
        assertEquals(0.028, fragmento.maximumAcceleration());
        assertEquals(0.78, fragmento.waitDamping());
        assertEquals(0.14, fragmento.hoverYAmplitude());
        assertEquals(64.0, fragmento.hoverPeriodTicks());
    }

    @Test
    void rejectsNonsensicalFlightBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new FlightProfile(0.17, 0.20, 0.19, 0.028, 0.78, 0.70,
                        0.50, 0.10, 0.018, 0.015, 0.18, 0.87, 0.12,
                        0.14, 0.035, 64.0));
    }
}
