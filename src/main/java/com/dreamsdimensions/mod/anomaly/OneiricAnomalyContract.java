package com.dreamsdimensions.mod.anomaly;

import java.util.Objects;

/**
 * Task 7.6 — pure, opt-in-free domain contract for liminal investigations.
 *
 * This is NOT an active worldgen or gameplay hook. No event listener, tick
 * scanner, packet handler, registry, or SavedData writer exists in Task 7.6.
 * Only server adapters in a subsequent task may create trusted evidence.
 *
 * A phenomenon must leave physical corroboration and a verified, reachable
 * payoff. Mere particles, noises, a relocated player block or a fake clue are
 * not enough to create an authentic anomaly.
 */
public final class OneiricAnomalyContract {
    public static final int VERSION = 1;
    public static final String OVERWORLD = "minecraft:overworld";
    public static final int TRACE_MIN_SEPARATION = 20;
    public static final int TRACE_MAX_DISTANCE = 96;
    public static final int PAYOFF_MIN_DISTANCE = 36;
    public static final int PAYOFF_MAX_DISTANCE = 192;
    public static final int PROOF_RADIUS = 8;

    private OneiricAnomalyContract() {}

    public enum Lineage {
        BLUE_FIELD("dreamsdimensions:campo_onirico_azul"),
        DREAMSCAPE("dreamsdimensions:dreamscape");

        private final String motifDimension;

        Lineage(String motifDimension) {
            this.motifDimension = motifDimension;
        }

        public String motifDimension() {
            return motifDimension;
        }
    }

    public enum Origin {
        /** Verified by dedicated server worldgen provenance, never by item appearance. */
        NATURAL_WORLDGEN,
        PLAYER_PLACED,
        COMMAND_PLACED,
        UNKNOWN
    }

    public enum Signal {
        NATURAL_MARK,
        GEOLOGIC_DISCONTINUITY,
        AMBIENT_INVERSION,
        VISUAL_FALLBACK,
        SLEEP_RECALL,
        WAKING_RECONCILIATION,
        PHYSICAL_PAYOFF
    }

    public enum Stage {
        HIDDEN,
        NOTICED,
        CORRELATED,
        INTERPRETED,
        RESOLVED
    }

    public enum ResultType {
        ADVANCED,
        NO_CHANGE,
        REJECTED
    }

    public enum Reason {
        ACCEPTED,
        ALREADY_RECORDED,
        ALREADY_RESOLVED,
        INVALID_PROVENANCE,
        UNTRUSTED_EVENT,
        WRONG_SITE,
        WRONG_DIMENSION,
        OUT_OF_RANGE,
        PREMATURE,
        TARGET_UNAVAILABLE,
        INVALID_SIGNAL
    }

    public record Pos(int x, int y, int z) {
        public long distanceSquared(Pos other) {
            Objects.requireNonNull(other, "other");
            long dx = (long) x - other.x;
            long dy = (long) y - other.y;
            long dz = (long) z - other.z;
            return dx * dx + dy * dy + dz * dz;
        }
    }

    /**
     * A Site is created only after real 7.7 worldgen has committed an anchor,
     * independent clue nodes and a real payoff. The worldgen placement adapter
     * must verify loaded chunks, terrain safety, and that those nodes exist.
     * The id is stable across save/reload and NEVER derived solely from the
     * current appearance of a block.
     */
    public record Site(
            String stableId,
            long worldSeed,
            String dimension,
            Lineage lineage,
            Origin origin,
            Pos markedAnchor,
            Pos geologicalTrace,
            Pos corroboratingTrace,
            Pos interpretationPoint,
            Pos physicalPayoff,
            boolean payoffCommitted
    ) {
        public Site {
            if (stableId == null || !stableId.matches("dreamsdimensions:[a-z0-9_./-]{1,96}")) {
                throw new IllegalArgumentException("Site needs a stable, namespaced identity");
            }
            Objects.requireNonNull(dimension, "dimension");
            Objects.requireNonNull(lineage, "lineage");
            Objects.requireNonNull(origin, "origin");
            Objects.requireNonNull(markedAnchor, "markedAnchor");
            Objects.requireNonNull(geologicalTrace, "geologicalTrace");
            Objects.requireNonNull(corroboratingTrace, "corroboratingTrace");
            Objects.requireNonNull(interpretationPoint, "interpretationPoint");
            Objects.requireNonNull(physicalPayoff, "physicalPayoff");
            if (!OVERWORLD.equals(dimension)) {
                throw new IllegalArgumentException("Task 7.6 sites originate in the Overworld");
            }
            long maxTrace = square(TRACE_MAX_DISTANCE);
            long minTrace = square(TRACE_MIN_SEPARATION);
            if (markedAnchor.distanceSquared(geologicalTrace) < minTrace
                    || markedAnchor.distanceSquared(geologicalTrace) > maxTrace
                    || markedAnchor.distanceSquared(corroboratingTrace) < minTrace
                    || markedAnchor.distanceSquared(corroboratingTrace) > maxTrace
                    || geologicalTrace.distanceSquared(corroboratingTrace) < minTrace
                    || geologicalTrace.distanceSquared(corroboratingTrace) > square(TRACE_MAX_DISTANCE * 2)) {
                throw new IllegalArgumentException("Distinct, nearby, spatially separated clues required");
            }
            if (markedAnchor.distanceSquared(physicalPayoff) < square(PAYOFF_MIN_DISTANCE)
                    || markedAnchor.distanceSquared(physicalPayoff) > square(PAYOFF_MAX_DISTANCE)) {
                throw new IllegalArgumentException("Payoff too near (trivial) or too far (false lead)");
            }
            if (interpretationPoint.distanceSquared(markedAnchor) > maxTrace) {
                throw new IllegalArgumentException("Interpretation must remain local to the anchor");
            }
            if (physicalPayoff.equals(geologicalTrace)
                    || physicalPayoff.equals(corroboratingTrace)
                    || physicalPayoff.equals(interpretationPoint)) {
                throw new IllegalArgumentException("Payoff may not masquerade as a prior clue");
            }
        }
    }

    /**
     * No Minecraft client should ever be able to forge this record. In 7.7,
     * a dedicated server adapter must derive this from world state, player
     * position, and an authoritative interaction. A passed boolean is NOT
     * by itself a security boundary.
     */
    public record Evidence(
            String siteId,
            String dimension,
            Signal signal,
            Pos observedAt,
            Origin source,
            boolean serverValidated,
            boolean payoffExistsInWorld
    ) {
        public Evidence {
            Objects.requireNonNull(siteId, "siteId");
            Objects.requireNonNull(dimension, "dimension");
            Objects.requireNonNull(signal, "signal");
            Objects.requireNonNull(observedAt, "observedAt");
            Objects.requireNonNull(source, "source");
        }
    }

    /** Immutable per-player, per-world, per-site progress: storage adapter comes in 7.7. */
    public record Progress(boolean noticed, boolean geology, boolean corroboration,
                           boolean interpreted, boolean resolved) {
        public static Progress empty() {
            return new Progress(false, false, false, false, false);
        }

        public Stage stage() {
            if (resolved) return Stage.RESOLVED;
            if (interpreted) return Stage.INTERPRETED;
            if (geology && corroboration) return Stage.CORRELATED;
            if (noticed) return Stage.NOTICED;
            return Stage.HIDDEN;
        }

        /** Reject corrupt or manually skipped phases on restoration. */
        public boolean valid() {
            return (!geology || noticed)
                    && (!corroboration || noticed)
                    && (!interpreted || (noticed && geology && corroboration))
                    && (!resolved || interpreted);
        }
    }

    public record Transition(Progress progress, ResultType result, Reason reason,
                             boolean discoveryUnlocked) {
        public Transition {
            Objects.requireNonNull(progress, "progress");
            Objects.requireNonNull(result, "result");
            Objects.requireNonNull(reason, "reason");
            if (!progress.valid()) {
                throw new IllegalArgumentException("Corrupted F0 anomaly progress");
            }
            if (discoveryUnlocked && (result != ResultType.ADVANCED || !progress.resolved())) {
                throw new IllegalArgumentException("Unlock may fire only once upon resolution");
            }
        }
    }

    public static Transition apply(Site site, Progress previous, Evidence evidence) {
        Objects.requireNonNull(site, "site");
        Objects.requireNonNull(previous, "previous");
        Objects.requireNonNull(evidence, "evidence");
        if (!previous.valid()) throw new IllegalArgumentException("Invalid persisted progress");
        if (!site.stableId().equals(evidence.siteId())) {
            return reject(previous, Reason.WRONG_SITE);
        }
        if (!site.dimension().equals(evidence.dimension())) {
            return reject(previous, Reason.WRONG_DIMENSION);
        }
        if (site.origin() != Origin.NATURAL_WORLDGEN || evidence.source() != Origin.NATURAL_WORLDGEN) {
            return reject(previous, Reason.INVALID_PROVENANCE);
        }
        if (!evidence.serverValidated()) {
            return reject(previous, Reason.UNTRUSTED_EVENT);
        }
        // Do not let a player begin investigating an incomplete, non-committed
        // scene. That would create an attractive but permanently false clue.
        if (!site.payoffCommitted()) {
            return reject(previous, Reason.TARGET_UNAVAILABLE);
        }
        if (previous.resolved()) {
            return unchanged(previous, Reason.ALREADY_RESOLVED);
        }

        Signal signal = evidence.signal();
        Pos expected = switch (signal) {
            case NATURAL_MARK -> site.markedAnchor();
            case GEOLOGIC_DISCONTINUITY -> site.geologicalTrace();
            case AMBIENT_INVERSION, VISUAL_FALLBACK -> site.corroboratingTrace();
            case SLEEP_RECALL, WAKING_RECONCILIATION -> site.interpretationPoint();
            case PHYSICAL_PAYOFF -> site.physicalPayoff();
        };
        if (expected.distanceSquared(evidence.observedAt()) > square(PROOF_RADIUS)) {
            return reject(previous, Reason.OUT_OF_RANGE);
        }
        switch (signal) {
            case NATURAL_MARK -> {
                if (previous.noticed()) return unchanged(previous, Reason.ALREADY_RECORDED);
                return advanced(new Progress(true, false, false, false, false), false);
            }
            case GEOLOGIC_DISCONTINUITY -> {
                if (!previous.noticed()) return reject(previous, Reason.PREMATURE);
                if (previous.geology()) return unchanged(previous, Reason.ALREADY_RECORDED);
                return advanced(new Progress(true, true, previous.corroboration(),
                        false, false), false);
            }
            case AMBIENT_INVERSION, VISUAL_FALLBACK -> {
                if (!previous.noticed()) return reject(previous, Reason.PREMATURE);
                if (previous.corroboration()) return unchanged(previous, Reason.ALREADY_RECORDED);
                return advanced(new Progress(true, previous.geology(), true,
                        false, false), false);
            }
            case SLEEP_RECALL, WAKING_RECONCILIATION -> {
                if (previous.stage().ordinal() < Stage.CORRELATED.ordinal()) {
                    return reject(previous, Reason.PREMATURE);
                }
                if (previous.interpreted()) return unchanged(previous, Reason.ALREADY_RECORDED);
                return advanced(new Progress(true, true, true, true, false), false);
            }
            case PHYSICAL_PAYOFF -> {
                if (!previous.interpreted()) return reject(previous, Reason.PREMATURE);
                if (!site.payoffCommitted() || !evidence.payoffExistsInWorld()) {
                    return reject(previous, Reason.TARGET_UNAVAILABLE);
                }
                return advanced(new Progress(true, true, true, true, true), true);
            }
            default -> {
                return reject(previous, Reason.INVALID_SIGNAL);
            }
        }
    }

    private static Transition advanced(Progress next, boolean unlock) {
        return new Transition(next, ResultType.ADVANCED, Reason.ACCEPTED, unlock);
    }

    private static Transition reject(Progress previous, Reason reason) {
        return new Transition(previous, ResultType.REJECTED, reason, false);
    }

    private static Transition unchanged(Progress previous, Reason reason) {
        return new Transition(previous, ResultType.NO_CHANGE, reason, false);
    }

    private static long square(int v) {
        return (long) v * v;
    }
}
