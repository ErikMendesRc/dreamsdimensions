package com.dreamsdimensions.mod.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * TASK 1.3: four shapes, one registry type and strictly datapack-defined materials.
 * All geometry is preflighted before a single block is written. Air is the only
 * space filled; natural surface grass is the only block ever replaced (with moss).
 * Shape bounds and worldgen-region write bounds must both be satisfied.
 */
public record DreamFormationFeature(
        String form, BlockState stone, BlockState cobble, BlockState shimmer,
        BlockState moss, BlockState flower, BlockState grass,
        int minSize, int maxSize, int maxRelief, float density
) implements Feature {
    public static final MapCodec<DreamFormationFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("form").forGetter(DreamFormationFeature::form),
            BlockState.CODEC.fieldOf("stone").forGetter(DreamFormationFeature::stone),
            BlockState.CODEC.fieldOf("cobble").forGetter(DreamFormationFeature::cobble),
            BlockState.CODEC.fieldOf("shimmer").forGetter(DreamFormationFeature::shimmer),
            BlockState.CODEC.fieldOf("moss").forGetter(DreamFormationFeature::moss),
            BlockState.CODEC.fieldOf("flower").forGetter(DreamFormationFeature::flower),
            BlockState.CODEC.fieldOf("grass").forGetter(DreamFormationFeature::grass),
            Codec.intRange(2, 9).fieldOf("min_size").forGetter(DreamFormationFeature::minSize),
            Codec.intRange(2, 9).fieldOf("max_size").forGetter(DreamFormationFeature::maxSize),
            Codec.intRange(0, 8).fieldOf("max_relief").forGetter(DreamFormationFeature::maxRelief),
            Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(DreamFormationFeature::density)
    ).apply(i, DreamFormationFeature::new));

    private static final int FLAGS = 2;
    private enum Target { AIR, GRASS }
    private record Change(BlockState state, Target target) {}

    /** A bounded write-set allows validating cross-chunk geometry atomically. */
    private static final class Plan {
        final Map<BlockPos, Change> changes = new LinkedHashMap<>();
        boolean add(BlockPos pos, BlockState state, Target target) {
            BlockPos key = pos.immutable();
            Change current = changes.get(key);
            if (current != null) {
                return current.target() == target;
            }
            changes.put(key, new Change(state, target));
            return true;
        }
        void rock(BlockPos pos, BlockState state) {
            add(pos, state, Target.AIR);
        }
        void moss(BlockPos pos, BlockState state) {
            add(pos, state, Target.GRASS);
        }
    }

    @Override
    public MapCodec<DreamFormationFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (minSize > maxSize || !validForm() || origin.getY() < level.getMinY() + 3
                || origin.getY() > level.getMaxY() - 26
                || !level.getBlockState(origin).isAir()
                || !level.getFluidState(origin).isEmpty()) {
            return false;
        }
        BlockPos floor = origin.below();
        if (!naturalFloor(level, floor, requiresGrass())) {
            return false;
        }
        int size = minSize + random.nextInt(maxSize - minSize + 1);
        int footprint = size + (form.equals("spines") ? 3 : 2);
        if (!reasonableRelief(level, origin, footprint)) {
            return false;
        }

        // A smooth, seed-dependent domain-warped regional field.
        long seed = level.getSeed();
        double x = origin.getX(), z = origin.getZ();
        double warpX = noise(seed ^ 0x271A4DL, x / 241.0, z / 241.0) * 38;
        double warpZ = noise(seed ^ 0x77F21L, x / 241.0 + 19, z / 241.0 - 67) * 38;
        double regional = noise(seed ^ 0x5A817L, (x + warpX) / 164.0, (z + warpZ) / 164.0);
        double small = noise(seed ^ 0xCA75DL, x / 47.0, z / 47.0);
        double weight = switch (form) {
            case "spines" -> smooth(-0.38, 0.48, 0.72 * regional + 0.28 * small);
            case "garden" -> smooth(-0.35, 0.25, -0.56 * regional + 0.44 * small);
            case "arch" -> smooth(-0.15, 0.55, 0.80 * regional + 0.20 * small);
            case "circle" -> smooth(-0.35, 0.40, -0.30 * regional + 0.70 * small);
            default -> 0.0;
        };
        double altitude = switch (form) {
            case "spines" -> origin.getY() >= 106 ? 1.0 : 0.66;
            case "garden" -> origin.getY() <= 124 ? 1.0 : 0.60;
            case "arch" -> origin.getY() >= 104 ? 1.0 : 0.64;
            case "circle" -> 1.0;
            default -> 0.0;
        };
        // Nonzero outskirts allow isolated landmarks; noise still strongly favors regions.
        if (random.nextFloat() >= density * altitude * (0.12 + 0.88 * weight)) {
            return false;
        }

        Plan plan = new Plan();
        boolean formed = switch (form) {
            case "spines" -> createSpines(level, random, origin, size, plan);
            case "garden" -> createGarden(level, random, origin, size, plan);
            case "arch" -> createArch(level, random, origin, size, plan);
            case "circle" -> createCircle(level, random, origin, size, plan);
            default -> false;
        };
        if (!formed || plan.changes.isEmpty() || plan.changes.size() > 1600) {
            return false;
        }
        // No per-block best-effort writes: reject the whole landmark on
        // obstruction, fluid, bad Y, world border or inaccessible worldgen region.
        for (Map.Entry<BlockPos, Change> entry : plan.changes.entrySet()) {
            BlockPos p = entry.getKey();
            Change change = entry.getValue();
            if (p.getY() <= level.getMinY() || p.getY() >= level.getMaxY()
                    || !level.ensureCanWrite(p) || !level.getWorldBorder().isWithinBounds(p)
                    || !level.getFluidState(p).isEmpty()) {
                return false;
            }
            if (change.target() == Target.AIR) {
                if (!level.getBlockState(p).isAir()) return false;
            } else {
                if (!level.getBlockState(p).is(grass.getBlock())
                        || !level.getFluidState(p.above()).isEmpty()
                        || !level.getBlockState(p.above()).isAir()) {
                    return false;
                }
            }
        }
        for (Map.Entry<BlockPos, Change> entry : plan.changes.entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue().state(), FLAGS);
        }
        return true;
    }

    private boolean validForm() {
        return form.equals("spines") || form.equals("garden")
                || form.equals("arch") || form.equals("circle");
    }

    private boolean requiresGrass() {
        return form.equals("garden") || form.equals("circle");
    }

    private boolean naturalFloor(WorldGenLevel level, BlockPos p, boolean grassOnly) {
        BlockState state = level.getBlockState(p);
        if (!level.getFluidState(p).isEmpty()) return false;
        if (grassOnly) return state.is(grass.getBlock());
        return state.is(grass.getBlock()) || state.is(stone.getBlock()) || state.is(cobble.getBlock());
    }

    /** Eight radial samples catch cliffs, overhangs and sharp terrace rims. */
    private boolean reasonableRelief(WorldGenLevel level, BlockPos origin, int radius) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                int x = origin.getX() + radius * dx;
                int z = origin.getZ() + radius * dz;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (Math.abs(y - origin.getY()) > maxRelief + 2) return false;
            }
        }
        return true;
    }

    /** Spines: offset skeletal cores, irregular tapered slices and low scree. */
    private boolean createSpines(WorldGenLevel level, RandomSource random,
                                  BlockPos origin, int size, Plan plan) {
        int count = 2 + random.nextInt(3);
        boolean atLeastOne = false;
        for (int n = 0; n < count; n++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int distance = n == 0 ? 0 : 2 + random.nextInt(Math.max(1, size - 1));
            int sx = origin.getX() + (int) Math.round(Math.cos(angle) * distance);
            int sz = origin.getZ() + (int) Math.round(Math.sin(angle) * distance);
            int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx, sz);
            BlockPos root = new BlockPos(sx, surface, sz);
            if (Math.abs(surface - origin.getY()) > maxRelief
                    || !naturalFloor(level, root.below(), false)
                    || !level.getBlockState(root).isAir()) return false;
            int h = Math.max(2, size + 1 + random.nextInt(size + 2) - n * 2);
            double leanX = (random.nextDouble() - 0.5) * 2.2;
            double leanZ = (random.nextDouble() - 0.5) * 2.2;
            BlockPos previous = root;
            for (int y = 0; y < h; y++) {
                double rise = y / (double) h;
                int ox = (int) Math.round(leanX * rise * rise);
                int oz = (int) Math.round(leanZ * rise * rise);
                BlockPos spine = root.offset(ox, y, oz);
                // Join horizontal steps at the previous position, never diagonal gaps.
                if (y > 0 && (spine.getX() != previous.getX()
                        || spine.getZ() != previous.getZ())) {
                    plan.rock(new BlockPos(previous.getX(), spine.getY(), previous.getZ()), cobble);
                }
                plan.rock(spine, stoneFor(random, y, h));
                int radiusX = y < h / 3 ? 2 : y < h * 2 / 3 ? 1 : 0;
                int radiusZ = y < h / 4 ? 1 : y < h / 2 ? 1 : 0;
                for (int dx = -radiusX; dx <= radiusX; dx++) {
                    for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        double ell = radiusX == 0 || radiusZ == 0 ? 2.0
                                : (dx * dx) / (double) (radiusX * radiusX)
                                + (dz * dz) / (double) (radiusZ * radiusZ);
                        if (ell <= 1.4 && random.nextFloat() < 0.66F) {
                            plan.rock(spine.offset(dx, 0, dz), stoneFor(random, y, h));
                        }
                    }
                }
                previous = spine;
            }
            atLeastOne = true;
        }
        return atLeastOne;
    }

    private BlockState stoneFor(RandomSource random, int y, int height) {
        if (y > 1 && random.nextFloat() < 0.020F) return shimmer;
        return random.nextFloat() < (y < height / 3 ? 0.35F : 0.18F) ? cobble : stone;
    }

    /** Lighting only comes from existing moss; shimmer is an accent, not a lamp. */
    private boolean createGarden(WorldGenLevel level, RandomSource random,
                                  BlockPos origin, int size, Plan plan) {
        boolean any = false;
        double skew = random.nextBoolean() ? 1.25 : 0.79;
        int supported = 0;
        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                double distance = (dx * dx * skew + dz * dz / skew) / (size * size);
                if (distance > 1.0) continue;
                // A mostly open natural passage bisects the low garden.
                if (Math.abs(dx + dz * 0.23) < 1.1 && random.nextFloat() < 0.84F) continue;
                BlockPos floor = surfaceFloor(level, origin.getX() + dx, origin.getZ() + dz);
                if (Math.abs(floor.getY() - (origin.getY() - 1)) > maxRelief
                        || !naturalFloor(level, floor, true)
                        || !level.getBlockState(floor.above()).isAir()) continue;
                supported++;
                float roll = random.nextFloat();
                if (roll < 0.26F * (1.0 - distance * 0.45)) {
                    plan.moss(floor, moss);
                    any = true;
                } else if (roll > 0.93F && distance > 0.20) {
                    plan.rock(floor.above(), shimmer);
                    any = true;
                } else if (roll > 0.70F && roll < 0.86F) {
                    plan.rock(floor.above(), flower);
                    any = true;
                }
            }
        }
        return supported >= 12 && any && plan.changes.size() >= 5;
    }

    /** A small freestanding, two-footed arch. Never bridges distant chunks. */
    private boolean createArch(WorldGenLevel level, RandomSource random,
                               BlockPos origin, int radius, Plan plan) {
        boolean alongX = random.nextBoolean();
        BlockPos left = surfaceFloor(level, origin.getX() + (alongX ? -radius : 0),
                                          origin.getZ() + (alongX ? 0 : -radius));
        BlockPos right = surfaceFloor(level, origin.getX() + (alongX ? radius : 0),
                                           origin.getZ() + (alongX ? 0 : radius));
        if (!naturalFloor(level, left, false) || !naturalFloor(level, right, false)
                || Math.abs(left.getY() - right.getY()) > Math.min(2, maxRelief)
                || !level.getBlockState(left.above()).isAir()
                || !level.getBlockState(right.above()).isAir()) return false;

        int rise = radius / 2 + 3 + random.nextInt(3);
        int lastY = left.getY() + 1;
        int width = random.nextBoolean() ? 1 : 0;
        for (int step = 0; step <= radius * 2; step++) {
            double t = step / (radius * 2.0);
            double archHeight = rise * (1.0 - Math.pow(t * 2.0 - 1.0, 2));
            int base = (int) Math.round((left.getY() + 1) * (1 - t) + (right.getY() + 1) * t);
            int y = base + (int) Math.round(archHeight);
            int along = -radius + step;
            int x = origin.getX() + (alongX ? along : 0);
            int z = origin.getZ() + (alongX ? 0 : along);
            // At each new column connect vertically to previous column Y:
            // the arch skeleton is always 6-connected, including steep legs.
            int from = step == 0 ? y : Math.min(lastY, y);
            int to = step == 0 ? y : Math.max(lastY, y);
            for (int yy = from; yy <= to; yy++) {
                BlockPos p = new BlockPos(x, yy, z);
                plan.rock(p, stoneFor(random, yy - base, rise + 1));
                if (width == 1 && random.nextFloat() < 0.82F) {
                    BlockPos rib = alongX ? p.offset(0, 0, 1) : p.offset(1, 0, 0);
                    plan.rock(rib, random.nextFloat() < 0.25F ? cobble : stone);
                }
            }
            lastY = y;
        }
        // Explicitly both feet at natural terrain elevations, not hanging.
        return plan.changes.size() >= radius * 2 + 1;
    }

    /** Broken, elliptical stone ribs following existing terrain: not a portal. */
    private boolean createCircle(WorldGenLevel level, RandomSource random,
                                 BlockPos origin, int radius, Plan plan) {
        double stretch = 0.78 + random.nextDouble() * 0.50;
        double phase = random.nextDouble() * Math.PI * 2;
        int segments = radius * 13;
        int pillars = 0;
        for (int i = 0; i < segments; i++) {
            double a = Math.PI * 2.0 * i / segments;
            // Grouped negative space, not missing random pixels or an ideal ring.
            if (Math.sin(4 * a + phase) > 0.72 || random.nextFloat() < 0.08F) continue;
            double r = radius + 0.44 * Math.sin(3 * a + phase);
            int x = origin.getX() + (int) Math.round(r * Math.cos(a) * stretch);
            int z = origin.getZ() + (int) Math.round(r * Math.sin(a) / stretch);
            BlockPos floor = surfaceFloor(level, x, z);
            if (Math.abs(floor.getY() - (origin.getY() - 1)) > maxRelief
                    || !naturalFloor(level, floor, true)
                    || !level.getBlockState(floor.above()).isAir()) continue;
            int h = random.nextFloat() < 0.21F ? 2 : 1;
            for (int y = 1; y <= h; y++) {
                plan.rock(floor.above(y), y == h && random.nextFloat() < 0.025F
                        ? shimmer : (random.nextFloat() < 0.37F ? cobble : stone));
            }
            pillars++;
        }
        // Low, scattered interior moss and flowers; keep the middle traversable.
        for (int n = 0; n < radius * 2; n++) {
            int dx = random.nextInt(radius * 2 - 1) - radius + 1;
            int dz = random.nextInt(radius * 2 - 1) - radius + 1;
            double d = dx * dx + dz * dz;
            if (d < 3 || d > (radius - 1) * (radius - 1)) continue;
            BlockPos floor = surfaceFloor(level, origin.getX() + dx, origin.getZ() + dz);
            if (Math.abs(floor.getY() - (origin.getY() - 1)) <= maxRelief
                    && naturalFloor(level, floor, true)
                    && level.getBlockState(floor.above()).isAir()) {
                if (random.nextBoolean()) plan.moss(floor, moss);
                else plan.rock(floor.above(), flower);
            }
        }
        return pillars >= radius * 4;
    }

    private static BlockPos surfaceFloor(WorldGenLevel level, int x, int z) {
        return new BlockPos(x,
                level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
    }

    private static double smooth(double low, double high, double n) {
        double t = Math.max(0.0, Math.min(1.0, (n - low) / (high - low)));
        return t * t * (3 - 2 * t);
    }

    private static double noise(long seed, double x, double z) {
        long ix = (long) Math.floor(x), iz = (long) Math.floor(z);
        double tx = smooth(0, 1, x - ix), tz = smooth(0, 1, z - iz);
        double a = hash(seed, ix, iz), b = hash(seed, ix + 1, iz);
        double c = hash(seed, ix, iz + 1), d = hash(seed, ix + 1, iz + 1);
        return (a + (b - a) * tx) * (1 - tz) + (c + (d - c) * tx) * tz;
    }

    private static double hash(long seed, long x, long z) {
        long v = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        v ^= v >>> 30;
        v *= 0xBF58476D1CE4E5B9L;
        v ^= v >>> 27;
        v *= 0x94D049BB133111EBL;
        v ^= v >>> 31;
        return ((v >>> 11) * 0x1.0p-53) * 2 - 1;
    }
}
