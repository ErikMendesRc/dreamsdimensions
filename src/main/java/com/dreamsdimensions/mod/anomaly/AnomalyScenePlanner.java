package com.dreamsdimensions.mod.anomaly;

import static com.dreamsdimensions.mod.anomaly.OneiricAnomalyContract.*;

import java.util.Objects;

/**
 * Task 7.6 — deterministic macrocell proposal, independent of chunk load
 * order and gameplay ticks. This class does NOT generate or place a scene.
 *
 * The terrain adapter in Task 7.7 is required to validate both routes,
 * anchors, provenance, player accessibility and secondary-site support
 * before committing any physical nodes or persistent anomaly identity.
 */
public final class AnomalyScenePlanner {
    public static final int CELL_SIDE_CHUNKS = 12;
    public static final int CANDIDATE_DENOMINATOR = 3;
    private static final long DOMAIN_SALT = 0xD3A5B472F0C1E967L;

    public record Cell(int x, int z) {}

    public record Candidate(
            Cell cell,
            Lineage lineage,
            long stableSignature,
            int rotationQuarterTurns,
            boolean candidateOnly
    ) {
        public Candidate {
            Objects.requireNonNull(cell, "cell");
            Objects.requireNonNull(lineage, "lineage");
            if (rotationQuarterTurns < 0 || rotationQuarterTurns > 3) {
                throw new IllegalArgumentException("Rotation must be a cardinal quarter turn");
            }
            if (!candidateOnly) {
                throw new IllegalArgumentException("Planner must not claim a site was generated");
            }
        }

        public String stableId() {
            String label = lineage == Lineage.BLUE_FIELD ? "blue_field" : "dreamscape";
            return "dreamsdimensions:" + label + "/cell_"
                    + Integer.toString(cell.x(), 36) + "_" + Integer.toString(cell.z(), 36)
                    + "_" + Long.toUnsignedString(stableSignature, 36);
        }
    }

    public record ProposedGeometry(
            Pos anchor,
            Pos geologicalTrace,
            Pos corroboratingTrace,
            Pos interpretationPoint,
            Pos physicalPayoff,
            boolean terrainVerified
    ) {
        public ProposedGeometry {
            Objects.requireNonNull(anchor, "anchor");
            Objects.requireNonNull(geologicalTrace, "geologicalTrace");
            Objects.requireNonNull(corroboratingTrace, "corroboratingTrace");
            Objects.requireNonNull(interpretationPoint, "interpretationPoint");
            Objects.requireNonNull(physicalPayoff, "physicalPayoff");
            if (terrainVerified) {
                throw new IllegalArgumentException("Geometry proposals are NOT verified world placements");
            }
        }
    }

    private AnomalyScenePlanner() {}

    /** Correct for negative world coordinates; no seam at 0 crossing. */
    public static Cell containingChunk(int chunkX, int chunkZ) {
        return new Cell(Math.floorDiv(chunkX, CELL_SIDE_CHUNKS),
                Math.floorDiv(chunkZ, CELL_SIDE_CHUNKS));
    }

    /**
     * One vote per macrocell, independent of the number of queried chunks.
     * This 1-in-3 candidate rate is a PROPOSAL, not calibrated world density.
     * The real number of generated sites may be lower after terrain checks.
     */
    public static boolean isCandidateCell(long worldSeed, Cell cell) {
        long hash = hash(worldSeed, cell, 0x7A1EC5F60C23D49BL);
        return Math.floorMod(hash, CANDIDATE_DENOMINATOR) == 0;
    }

    /** Only returns a candidate, never claims generation or existence. */
    public static Candidate propose(long worldSeed, int chunkX, int chunkZ) {
        Cell cell = containingChunk(chunkX, chunkZ);
        if (!isCandidateCell(worldSeed, cell)) return null;
        long sig = hash(worldSeed, cell, DOMAIN_SALT);
        Lineage lineage = (sig & 1L) == 0L ? Lineage.BLUE_FIELD : Lineage.DREAMSCAPE;
        int rotation = (int) ((sig >>> 4) & 3);
        return new Candidate(cell, lineage, sig, rotation, true);
    }

    /**
     * Toy-local geometry at a SERVER-verified natural stone reference, never a
     * valid site on its own: all five candidate positions must be checked in
     * loaded/generated chunks and the player route must be proven.
     */
    public static ProposedGeometry geometry(Candidate candidate, Pos naturalStoneReference) {
        Objects.requireNonNull(candidate, "candidate");
        Objects.requireNonNull(naturalStoneReference, "naturalStoneReference");
        int rotation = candidate.rotationQuarterTurns();
        Pos geology = offset(naturalStoneReference, rotation, 24, 0);
        Pos corroboration = offset(naturalStoneReference, rotation, 0, 32);
        Pos interpretation = offset(naturalStoneReference, rotation, 8, 8);
        Pos target = offset(naturalStoneReference, rotation, 80, 48);
        return new ProposedGeometry(naturalStoneReference, geology, corroboration,
                interpretation, target, false);
    }

    private static Pos offset(Pos anchor, int rotation, int dx, int dz) {
        return switch (rotation) {
            case 0 -> new Pos(anchor.x() + dx, anchor.y(), anchor.z() + dz);
            case 1 -> new Pos(anchor.x() - dz, anchor.y(), anchor.z() + dx);
            case 2 -> new Pos(anchor.x() - dx, anchor.y(), anchor.z() - dz);
            case 3 -> new Pos(anchor.x() + dz, anchor.y(), anchor.z() - dx);
            default -> throw new IllegalArgumentException("Invalid orientation");
        };
    }

    private static long hash(long seed, Cell cell, long salt) {
        long value = seed ^ salt;
        value ^= ((long) cell.x()) * 0x9E3779B97F4A7C15L;
        value ^= ((long) cell.z()) * 0xC2B2AE3D27D4EB4FL;
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return value;
    }
}
