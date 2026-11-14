package com.dreamsdimensions.mod.service;

/**
 * Pure, bounded search for the nearest matching block. The caller owns all
 * world access: OUTSIDE excludes world-border cells; UNAVAILABLE means an
 * unloaded chunk and never triggers a load. No global cache or tick loop.
 */
public final class EchoMossSearch {
    public static final int HORIZONTAL_RADIUS = 12;
    public static final int VERTICAL_RADIUS = 6;
    public static final int MAX_BOX_POSITIONS = 25 * 25 * 13;

    public record Cell(int x, int y, int z) {}

    public enum Sample { TARGET, OTHER, OUTSIDE, UNAVAILABLE }
    public enum Status { FOUND, ABSENT, UNAVAILABLE }

    @FunctionalInterface
    public interface CellReader {
        Sample sample(int x, int y, int z);
    }

    public record Result(Status status, Cell target, int inspected, int unavailable) {
        public boolean found() {
            return status == Status.FOUND;
        }
    }

    private EchoMossSearch() {}

    /**
     * World bounds are [minY, maxY), matching Level's block-height contract.
     * Deterministic tie-break: distance squared, x, y, then z ascending.
     */
    public static Result findNearest(Cell origin, int minY, int maxY, CellReader reader) {
        if (minY >= maxY) throw new IllegalArgumentException("Invalid world height");
        Cell closest = null;
        long bestDistance = Long.MAX_VALUE;
        int inspected = 0;
        int unavailable = 0;

        for (int dx = -HORIZONTAL_RADIUS; dx <= HORIZONTAL_RADIUS; dx++) {
            for (int dz = -HORIZONTAL_RADIUS; dz <= HORIZONTAL_RADIUS; dz++) {
                int horizontalSquared = dx * dx + dz * dz;
                if (horizontalSquared > HORIZONTAL_RADIUS * HORIZONTAL_RADIUS) continue;
                for (int dy = -VERTICAL_RADIUS; dy <= VERTICAL_RADIUS; dy++) {
                    int y = origin.y() + dy;
                    if (y < minY || y >= maxY) continue;
                    int x = origin.x() + dx;
                    int z = origin.z() + dz;
                    Sample sample = reader.sample(x, y, z);
                    if (sample == Sample.UNAVAILABLE) {
                        unavailable++;
                        continue;
                    }
                    if (sample == Sample.OUTSIDE) continue;
                    inspected++;
                    if (sample != Sample.TARGET) continue;
                    long distance = (long) horizontalSquared + (long) dy * dy;
                    Cell candidate = new Cell(x, y, z);
                    if (closest == null || distance < bestDistance
                            || (distance == bestDistance && precedes(candidate, closest))) {
                        closest = candidate;
                        bestDistance = distance;
                    }
                }
            }
        }
        if (closest != null) return new Result(Status.FOUND, closest, inspected, unavailable);
        return new Result(unavailable > 0 ? Status.UNAVAILABLE : Status.ABSENT,
                null, inspected, unavailable);
    }

    private static boolean precedes(Cell a, Cell b) {
        if (a.x() != b.x()) return a.x() < b.x();
        if (a.y() != b.y()) return a.y() < b.y();
        return a.z() < b.z();
    }
}
