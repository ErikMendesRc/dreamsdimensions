package com.dreamsdimensions.mod.worldgen;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Dreamscape-only six silhouette families. Immutable configured-feature data
 * and absolute-coordinate seeded regional masks; no chunk caches or tick hooks.
 * Geometry is planned and fully checked before any worldgen writes.
 */
public record DreamscapeEcosystemFeature(
        String form, BlockState stone, BlockState log, BlockState leaves,
        BlockState grass, BlockState flower, BlockState moss, BlockState shimmer,
        int minHeight, int maxHeight, int maxRelief, float density
) implements Feature {
    public static final MapCodec<DreamscapeEcosystemFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("form").forGetter(DreamscapeEcosystemFeature::form),
            BlockState.CODEC.fieldOf("stone").forGetter(DreamscapeEcosystemFeature::stone),
            BlockState.CODEC.fieldOf("log").forGetter(DreamscapeEcosystemFeature::log),
            BlockState.CODEC.fieldOf("leaves").forGetter(DreamscapeEcosystemFeature::leaves),
            BlockState.CODEC.fieldOf("grass").forGetter(DreamscapeEcosystemFeature::grass),
            BlockState.CODEC.fieldOf("flower").forGetter(DreamscapeEcosystemFeature::flower),
            BlockState.CODEC.fieldOf("moss").forGetter(DreamscapeEcosystemFeature::moss),
            BlockState.CODEC.fieldOf("shimmer").forGetter(DreamscapeEcosystemFeature::shimmer),
            Codec.intRange(1, 32).fieldOf("min_height").forGetter(DreamscapeEcosystemFeature::minHeight),
            Codec.intRange(1, 32).fieldOf("max_height").forGetter(DreamscapeEcosystemFeature::maxHeight),
            Codec.intRange(0, 8).fieldOf("max_relief").forGetter(DreamscapeEcosystemFeature::maxRelief),
            Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(DreamscapeEcosystemFeature::density)
    ).apply(i, DreamscapeEcosystemFeature::new));

    private static final int FLAGS = 2;
    private static final int MAX_PLANNED_BLOCKS = 1750;
    // The protected core is wider than the 12-block natural spawn search
    // and the horizontal extent of any planned structure.
    private static final int SPAWN_PROTECTION = 68;
    private static final boolean METRICS = Boolean.getBoolean("dreamsdimensions.worldgenMetrics");
    private enum Target { AIR, GRASS }
    private record Change(BlockState block, Target target) {}

    private static final class Plan {
        final Map<BlockPos, Change> blocks = new LinkedHashMap<>();
        boolean valid = true;
        void add(BlockPos pos, BlockState block, Target target) {
            BlockPos p = pos.immutable();
            Change old = blocks.get(p);
            if (old != null && old.target != target) {
                valid = false;
            } else if (old == null || block.is(old.block.getBlock())) {
                blocks.put(p, new Change(block, target));
            } else if (target == Target.AIR) {
                // Logs supersede foliage, stone supersedes incidental flower.
                boolean newCore = block.hasProperty(RotatedPillarBlock.AXIS);
                boolean oldLeaf = old.block.hasProperty(LeavesBlock.DISTANCE);
                if (newCore && oldLeaf) blocks.put(p, new Change(block, target));
            }
        }
        void air(BlockPos p, BlockState state) { add(p, state, Target.AIR); }
        void soil(BlockPos p, BlockState state) { add(p, state, Target.GRASS); }
    }

    @Override
    public MapCodec<DreamscapeEcosystemFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (minHeight > maxHeight || !knownForm()
                || origin.getY() <= level.getMinY() + 3
                || origin.getY() + maxHeight + 12 >= level.getMaxY()) return false;
        long x = origin.getX(), z = origin.getZ();
        if (x * x + z * z < (long) SPAWN_PROTECTION * SPAWN_PROTECTION) return false;
        if (!level.getWorldBorder().isWithinBounds(origin)) return false;

        // First gate is very cheap; avoid heightmap/geometry work in empty regions.
        long seed = level.getSeed();
        double xx = x, zz = z;
        double wx = xx + 33 * noise(seed ^ 0x771ACEL, xx / 265, zz / 265);
        double wz = zz + 33 * noise(seed ^ 0x3D102L, xx / 265 + 17, zz / 265 - 39);
        double region = noise(seed ^ 0x6F123L, wx / 187, wz / 187);
        double detail = noise(seed ^ 0x8831L, wx / 83, wz / 83);
        double combined = 0.75 * region + 0.25 * detail;
        double weight = switch (form) {
            case "memory_tree" -> smooth(-0.31, 0.37, combined);
            case "witness_tree" -> smooth(0.12, 0.63, combined);
            case "hanging_roots" -> smooth(-0.15, 0.54, -region + 0.35 * detail);
            case "fractured_monolith" -> smooth(-0.10, 0.56, -0.65 * region + 0.35 * detail);
            case "stone_ribs" -> smooth(0.02, 0.62, -0.48 * region + 0.52 * detail);
            case "echo_garden" -> smooth(-0.25, 0.38, 0.46 * region - 0.54 * detail);
            default -> 0;
        };
        double altitude = switch (form) {
            case "witness_tree" -> origin.getY() >= 125 ? 1.0 : 0.16;
            case "hanging_roots" -> origin.getY() >= 157 ? 1.0 : 0;
            case "fractured_monolith" -> origin.getY() >= 92 ? 1.0 : 0.47;
            case "stone_ribs" -> origin.getY() >= 102 ? 1.0 : 0.34;
            case "echo_garden" -> origin.getY() <= 160 ? 1.0 : 0.36;
            default -> 1.0;
        };
        if (random.nextFloat() >= density * altitude * weight) return false;

        boolean hanging = form.equals("hanging_roots");
        BlockPos floor = origin.below();
        if (!hanging) {
            if (!air(level, origin) || !naturalFloor(level, floor, requiresGrass())) return false;
            // Topmost heightmap can refer to an island: vegetation stays on the
            // actual standing surface rather than an assumed continental Y.
            if (!relief(level, origin, form.equals("stone_ribs") ? 5 : 4)) return false;
        }

        Plan plan = new Plan();
        int height = minHeight + random.nextInt(maxHeight - minHeight + 1);
        boolean made = switch (form) {
            case "memory_tree" -> memoryTree(plan, origin, height, random);
            case "witness_tree" -> witnessTree(plan, origin, height, random);
            case "hanging_roots" -> hangingRoots(level, plan, origin, height, random);
            case "fractured_monolith" -> monolith(plan, origin, height, random);
            case "stone_ribs" -> stoneRibs(level, plan, origin, height, random);
            case "echo_garden" -> echoGarden(level, plan, origin, height, random);
            default -> false;
        };
        if (!made || !plan.valid || plan.blocks.isEmpty()
                || plan.blocks.size() > MAX_PLANNED_BLOCKS) return false;
        if (form.equals("memory_tree") || form.equals("witness_tree")) connectLeaves(plan);
        if (!plan.valid || plan.blocks.isEmpty()) return false;

        // For every planned block: terrain, border, fluids, write region and
        // occupancy must pass BEFORE even one setBlock call is attempted.
        for (Map.Entry<BlockPos, Change> e : plan.blocks.entrySet()) {
            BlockPos p = e.getKey();
            if (p.getY() <= level.getMinY() || p.getY() >= level.getMaxY()
                    || !level.ensureCanWrite(p) || !level.getWorldBorder().isWithinBounds(p)
                    || !level.getFluidState(p).isEmpty()) return false;
            if (e.getValue().target == Target.AIR) {
                if (!level.getBlockState(p).isAir()) return false;
            } else {
                if (!level.getBlockState(p).is(grass.getBlock())
                        || !air(level, p.above())) return false;
            }
        }
        for (Map.Entry<BlockPos, Change> e : plan.blocks.entrySet()) {
            level.setBlock(e.getKey(), e.getValue().block, FLAGS);
        }
        // Opt-in real placement evidence for CI; zero means no feature
        // was observed, not that its algorithm was validated by decoding.
        if (METRICS) {
            DreamsDimensions.LOGGER.info("DREAMSCAPE_ECOSYSTEM_GENERATED form={} x={} y={} z={} blocks={}",
                    form, origin.getX(), origin.getY(), origin.getZ(), plan.blocks.size());
        }
        return true;
    }

    private boolean knownForm() {
        return switch (form) {
            case "memory_tree", "witness_tree", "hanging_roots",
                 "fractured_monolith", "stone_ribs", "echo_garden" -> true;
            default -> false;
        };
    }

    private boolean requiresGrass() {
        return form.equals("memory_tree") || form.equals("witness_tree") || form.equals("echo_garden");
    }

    private boolean naturalFloor(WorldGenLevel level, BlockPos p, boolean grassOnly) {
        BlockState state = level.getBlockState(p);
        return level.getFluidState(p).isEmpty() && (state.is(grass.getBlock())
                || (!grassOnly && state.is(stone.getBlock())));
    }

    private boolean relief(WorldGenLevel level, BlockPos origin, int radius) {
        int maxDelta = maxRelief;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    origin.getX() + radius * d.getStepX(), origin.getZ() + radius * d.getStepZ());
            if (Math.abs(y - origin.getY()) > maxDelta) return false;
        }
        return true;
    }

    private static boolean air(WorldGenLevel level, BlockPos p) {
        return level.getBlockState(p).isAir() && level.getFluidState(p).isEmpty();
    }

    private void wood(Plan plan, BlockPos p, Direction.Axis axis) {
        BlockState state = log;
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            state = state.setValue(RotatedPillarBlock.AXIS, axis);
        }
        plan.air(p, state);
    }

    /**
     * Step along a piecewise-linear beam in Manhattan-adjacent voxels. Each
     * segment is 6-connected even when X/Z/Y increment on the same sample.
     */
    private void limb(Plan plan, BlockPos from, BlockPos to) {
        int steps = Math.max(1, Math.max(Math.abs(to.getX() - from.getX()),
                Math.max(Math.abs(to.getY() - from.getY()), Math.abs(to.getZ() - from.getZ()))));
        BlockPos p = from;
        for (int i = 1; i <= steps; i++) {
            int tx = from.getX() + (int) Math.round((to.getX() - from.getX()) * i / (double) steps);
            int ty = from.getY() + (int) Math.round((to.getY() - from.getY()) * i / (double) steps);
            int tz = from.getZ() + (int) Math.round((to.getZ() - from.getZ()) * i / (double) steps);
            while (p.getY() != ty) {
                p = p.offset(0, Integer.signum(ty - p.getY()), 0);
                wood(plan, p, Direction.Axis.Y);
            }
            while (p.getX() != tx) {
                p = p.offset(Integer.signum(tx - p.getX()), 0, 0);
                wood(plan, p, Direction.Axis.X);
            }
            while (p.getZ() != tz) {
                p = p.offset(0, 0, Integer.signum(tz - p.getZ()));
                wood(plan, p, Direction.Axis.Z);
            }
        }
    }

    private void brokenCrown(Plan plan, BlockPos center, int rx, int ry, int rz, RandomSource random) {
        // Unequal radii and negative space create loose pieces, not a ball.
        for (int dy = -ry; dy <= ry; dy++) {
            for (int dx = -rx; dx <= rx; dx++) {
                for (int dz = -rz; dz <= rz; dz++) {
                    double dist = dx * dx / (double) (rx * rx)
                            + dy * dy / (double) (ry * ry)
                            + dz * dz / (double) (rz * rz);
                    if (dist > 1.0 || random.nextFloat() < (dist > 0.55 ? 0.48F : 0.17F)) continue;
                    BlockPos p = center.offset(dx, dy, dz);
                    if (!plan.blocks.containsKey(p)) plan.air(p, leaves);
                }
            }
        }
    }

    private boolean memoryTree(Plan plan, BlockPos root, int h, RandomSource random) {
        int sx = random.nextBoolean() ? 1 : -1;
        int sz = random.nextBoolean() ? 1 : -1;
        BlockPos previous = root;
        for (int y = 0; y <= h; y++) {
            double t = y / (double) h;
            BlockPos next = root.offset(sx * (int) Math.round(3.0 * t * t),
                    y, sz * (int) Math.round(1.5 * Math.sin(t * Math.PI)));
            limb(plan, previous, next);
            wood(plan, next, Direction.Axis.Y);
            // Thick gnarled base, strictly above a natural original root.
            if (y <= 3) {
                wood(plan, next.east(), Direction.Axis.Y);
                wood(plan, next.south(), Direction.Axis.Y);
            }
            previous = next;
        }
        int limbs = 4 + random.nextInt(2);
        double rotation = random.nextDouble() * Math.PI * 2;
        for (int i = 0; i < limbs; i++) {
            double angle = rotation + 2 * Math.PI * i / limbs + random.nextDouble() * 0.36;
            int reach = 4 + random.nextInt(5);
            int y = h / 2 + random.nextInt(Math.max(2, h / 2));
            BlockPos junction = root.offset(sx * (int) Math.round(3.0 * y * y / (h * (double) h)),
                    y, sz * (int) Math.round(1.5 * Math.sin(Math.PI * y / h)));
            BlockPos tip = junction.offset((int) Math.round(reach * Math.cos(angle)),
                    2 + random.nextInt(3), (int) Math.round(reach * Math.sin(angle)));
            limb(plan, junction, tip);
            brokenCrown(plan, tip, 2 + random.nextInt(2), 2, 2, random);
            if (i % 2 == 0) brokenCrown(plan, tip.above(2), 2, 1, 2, random);
        }
        brokenCrown(plan, previous.above(), 3, 2, 2, random);
        return true;
    }

    private boolean witnessTree(Plan plan, BlockPos root, int h, RandomSource random) {
        int leanX = random.nextBoolean() ? 1 : -1;
        int leanZ = random.nextBoolean() ? 1 : -1;
        BlockPos prev = root;
        for (int y = 0; y <= h; y++) {
            BlockPos p = root.offset(leanX * y / (h + 3), y, leanZ * y / (h + 5));
            limb(plan, prev, p);
            wood(plan, p, Direction.Axis.Y);
            prev = p;
        }
        // Sparse, long sideways arms at different vertical levels, visibly
        // unlike the low clustered asymmetrical Memory tree.
        int arms = 2 + random.nextInt(2);
        double angle = random.nextDouble() * Math.PI * 2;
        for (int i = 0; i < arms; i++) {
            int y = h - 5 - i * 5;
            if (y < h / 3) break;
            BlockPos origin = root.offset(leanX * y / (h + 3), y, leanZ * y / (h + 5));
            double a = angle + i * 2.31;
            int d = 6 + random.nextInt(4);
            BlockPos tip = origin.offset((int) Math.round(d * Math.cos(a)), 1 + i % 2,
                    (int) Math.round(d * Math.sin(a)));
            limb(plan, origin, tip);
            brokenCrown(plan, tip, 2, 1 + i % 2, 2, random);
        }
        brokenCrown(plan, prev.above(), 2, 2, 2, random);
        return true;
    }

    private void connectLeaves(Plan plan) {
        // Like the homologated blue implementation, BFS is strictly bounded
        // to leaf nodes that were planned, with vanilla distance <= 6.
        Map<BlockPos, Integer> distances = new HashMap<>();
        Deque<BlockPos> todo = new ArrayDeque<>();
        for (Map.Entry<BlockPos, Change> e : plan.blocks.entrySet()) {
            if (!e.getValue().block.is(log.getBlock())) continue;
            for (Direction side : Direction.values()) {
                BlockPos p = e.getKey().relative(side);
                Change leaf = plan.blocks.get(p);
                if (leaf != null && leaf.block.is(leaves.getBlock()) && !distances.containsKey(p)) {
                    distances.put(p, 1);
                    todo.add(p);
                }
            }
        }
        while (!todo.isEmpty()) {
            BlockPos p = todo.removeFirst();
            int dist = distances.get(p);
            if (dist >= 6) continue;
            for (Direction side : Direction.values()) {
                BlockPos next = p.relative(side);
                Change leaf = plan.blocks.get(next);
                if (leaf != null && leaf.block.is(leaves.getBlock()) && !distances.containsKey(next)) {
                    distances.put(next, dist + 1);
                    todo.add(next);
                }
            }
        }
        Iterator<Map.Entry<BlockPos, Change>> it = plan.blocks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Change> e = it.next();
            if (!e.getValue().block.is(leaves.getBlock())) continue;
            Integer d = distances.get(e.getKey());
            if (d == null) {
                it.remove();
            } else {
                BlockState state = e.getValue().block;
                if (state.hasProperty(LeavesBlock.DISTANCE)) state = state.setValue(LeavesBlock.DISTANCE, d);
                if (state.hasProperty(LeavesBlock.PERSISTENT)) state = state.setValue(LeavesBlock.PERSISTENT, false);
                e.setValue(new Change(state, Target.AIR));
            }
        }
    }

    /**
     * Real underside search, never guessed from top heightmap. A high exposed
     * stone underside with air 6/12 below and solid overburden is mandatory.
     * Scans a bounded vertical span ONLY after the rare regional gate.
     */
    private boolean hangingRoots(WorldGenLevel level, Plan plan, BlockPos origin,
                                 int length, RandomSource random) {
        if (origin.getY() < 158) return false;
        int x = origin.getX(), z = origin.getZ();
        int lower = Math.max(132, origin.getY() - 84);
        BlockPos anchor = null;
        for (int y = Math.min(224, origin.getY() - 3); y >= lower; y--) {
            BlockPos p = new BlockPos(x, y, z);
            if (!level.getBlockState(p).is(stone.getBlock())) continue;
            if (!level.getBlockState(p.above()).is(stone.getBlock())) continue;
            if (!air(level, p.below()) || !air(level, p.below(6))
                    || !air(level, p.below(12))) continue;
            anchor = p;
            break;
        }
        if (anchor == null || anchor.getY() - length - 4 <= level.getMinY()) return false;
        BlockPos base = anchor.below();
        BlockPos prior = base;
        wood(plan, base, Direction.Axis.Y); // touches existing stone above
        int bendX = random.nextBoolean() ? 1 : -1;
        int bendZ = random.nextBoolean() ? 1 : -1;
        for (int dy = 1; dy <= length; dy++) {
            BlockPos p = base.offset(bendX * dy / 5, -dy, bendZ * dy / 7);
            limb(plan, prior, p);
            wood(plan, p, Direction.Axis.Y);
            if (dy == length / 2 || dy == length - 3) {
                BlockPos off = p.offset(bendZ * (2 + random.nextInt(2)),
                        -(1 + random.nextInt(2)), -bendX * 2);
                limb(plan, p, off);
            }
            prior = p;
        }
        return true;
    }

    private boolean monolith(Plan plan, BlockPos root, int height, RandomSource random) {
        int sx = random.nextBoolean() ? 1 : -1;
        int sz = random.nextBoolean() ? 1 : -1;
        BlockPos prev = root;
        for (int y = 0; y < height; y++) {
            double t = y / (double) height;
            BlockPos p = root.offset(sx * (int) Math.round(t * t * 3), y,
                    sz * (int) Math.round(t * 2));
            if (y > 0 && (p.getX() != prev.getX() || p.getZ() != prev.getZ())) {
                plan.air(new BlockPos(prev.getX(), p.getY(), prev.getZ()), stone);
                plan.air(new BlockPos(p.getX(), p.getY(), prev.getZ()), stone);
            }
            plan.air(p, stone);
            int radius = y < 3 ? 2 : y < height * 0.65 ? 1 : 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dz == 0 || Math.abs(dx) + Math.abs(dz) > radius) continue;
                    if (radius == 2 && Math.abs(dx) + Math.abs(dz) == 2
                            && random.nextFloat() < 0.40F) continue;
                    plan.air(p.offset(dx, 0, dz), stone);
                }
            }
            prev = p;
        }
        // A skewed, incomplete secondary splinter attached to the parent.
        BlockPos spur = root.above(height / 3).east(sx);
        for (int n = 0; n < 3; n++) plan.air(spur.offset(sx * n, n, sz), stone);
        return true;
    }

    private boolean stoneRibs(WorldGenLevel level, Plan plan, BlockPos origin,
                               int span, RandomSource random) {
        // Short two-footed arch: both supports must be naturally grounded
        // and at close elevations. No bridge across a large terrain fracture.
        boolean alongX = random.nextBoolean();
        int radius = Math.max(3, Math.min(5, span / 2));
        BlockPos a = floor(level, origin.getX() - (alongX ? radius : 0),
                origin.getZ() - (alongX ? 0 : radius));
        BlockPos b = floor(level, origin.getX() + (alongX ? radius : 0),
                origin.getZ() + (alongX ? 0 : radius));
        if (!naturalFloor(level, a, false) || !naturalFloor(level, b, false)
                || Math.abs(a.getY() - b.getY()) > 2
                || !air(level, a.above()) || !air(level, b.above())) return false;
        int rise = 4 + random.nextInt(3);
        int prevY = a.getY() + 1;
        for (int i = 0; i <= radius * 2; i++) {
            double t = i / (radius * 2.0);
            int along = i - radius;
            int x = origin.getX() + (alongX ? along : 0);
            int z = origin.getZ() + (alongX ? 0 : along);
            int base = (int) Math.round((a.getY() + 1) * (1 - t) + (b.getY() + 1) * t);
            int y = base + (int) Math.round(rise * (1 - (2 * t - 1) * (2 * t - 1)));
            for (int py = Math.min(prevY, y); py <= Math.max(prevY, y); py++) {
                plan.air(new BlockPos(x, py, z), stone);
            }
            prevY = y;
        }
        return plan.blocks.size() >= radius * 2 + 1;
    }

    private boolean echoGarden(WorldGenLevel level, Plan plan, BlockPos origin,
                               int size, RandomSource random) {
        int radius = Math.max(3, Math.min(6, size));
        double angle = random.nextDouble() * Math.PI;
        double ca = Math.cos(angle), sa = Math.sin(angle);
        int supported = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double u = dx * ca - dz * sa, v = dx * sa + dz * ca;
                double waviness = 0.24 * Math.sin(dx * 0.73 + dz * 0.46);
                double d = u * u / (radius * (double) radius)
                        + v * v / (radius * radius * 0.51) + waviness;
                if (d > 1.0 || random.nextFloat() < 0.22) continue;
                BlockPos ground = floor(level, origin.getX() + dx, origin.getZ() + dz);
                if (!naturalFloor(level, ground, true)
                        || Math.abs(ground.getY() - (origin.getY() - 1)) > maxRelief
                        || !air(level, ground.above())) continue;
                supported++;
                float choice = random.nextFloat();
                if (choice < 0.20F) plan.soil(ground, moss);
                else if (choice < 0.41F) plan.air(ground.above(), flower);
                else if (choice > 0.982F) plan.air(ground.above(), shimmer);
            }
        }
        return supported >= 9 && plan.blocks.size() >= 5;
    }

    private static BlockPos floor(WorldGenLevel level, int x, int z) {
        return new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
    }

    private static double smooth(double low, double high, double n) {
        double t = Math.max(0, Math.min(1, (n - low) / (high - low)));
        return t * t * (3 - 2 * t);
    }
    private static double noise(long seed, double x, double z) {
        long ix = (long) Math.floor(x), iz = (long) Math.floor(z);
        double fx = smooth(0, 1, x - ix), fz = smooth(0, 1, z - iz);
        double a = hash(seed, ix, iz), b = hash(seed, ix + 1, iz);
        double c = hash(seed, ix, iz + 1), d = hash(seed, ix + 1, iz + 1);
        return (a + (b - a) * fx) * (1 - fz) + (c + (d - c) * fx) * fz;
    }
    private static double hash(long seed, long x, long z) {
        long v = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        v ^= v >>> 30;
        v *= 0xBF58476D1CE4E5B9L;
        v ^= v >>> 27;
        v *= 0x94D049BB133111EBL;
        v ^= v >>> 31;
        return ((v >>> 11) * 0x1.0p-53) * 2 - 1.0;
    }
}
