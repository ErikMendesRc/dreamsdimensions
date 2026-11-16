package com.dreamsdimensions.mod.entity.movement;

/**
 * Immutable tuning of physical flight. Species own their profiles; the flight
 * controller contains no Fragmento-specific state or animation assumptions.
 */
public record FlightProfile(
        double speedFactor, double minimumSpeed, double maximumSpeed,
        double maximumAcceleration, double waitDamping, double arrivalDamping,
        double obstacleDamping, double arrivalDistanceSquared,
        double horizontalWave, double verticalWave, double wavePhaseSpeed,
        double verticalWaveRatio, double yawSmoothing,
        double hoverYAmplitude, double hoverXZAmplitude, double hoverPeriodTicks) {

    public FlightProfile {
        if (minimumSpeed <= 0 || maximumSpeed < minimumSpeed || maximumAcceleration <= 0
                || hoverPeriodTicks <= 0 || speedFactor <= 0
                || waitDamping < 0 || waitDamping > 1
                || arrivalDamping < 0 || arrivalDamping > 1
                || obstacleDamping < 0 || obstacleDamping > 1) {
            throw new IllegalArgumentException("Invalid flight profile");
        }
    }
}
