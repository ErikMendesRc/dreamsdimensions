package com.dreamsdimensions.mod.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
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
 * Five data-driven vegetation forms. All noise samples use absolute world
 * coordinates and the world seed; no per-chunk state, tick hooks or global RNG.
 * Only biome JSONs that reference this feature can trigger it.
 */
public record DreamVegetationFeature(
        String form, BlockState log, BlockState leaves, BlockState flower,
        BlockState moss, BlockState soil, int minHeight, int maxHeight, float density
) implements Feature {
    public static final MapCodec<DreamVegetationFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("form").forGetter(DreamVegetationFeature::form),
            BlockState.CODEC.fieldOf("log").forGetter(DreamVegetationFeature::log),
            BlockState.CODEC.fieldOf("leaves").forGetter(DreamVegetationFeature::leaves),
            BlockState.CODEC.fieldOf("flower").forGetter(DreamVegetationFeature::flower),
            BlockState.CODEC.fieldOf("moss").forGetter(DreamVegetationFeature::moss),
            BlockState.CODEC.fieldOf("soil").forGetter(DreamVegetationFeature::soil),
            Codec.intRange(1, 28).fieldOf("min_height").forGetter(DreamVegetationFeature::minHeight),
            Codec.intRange(1, 32).fieldOf("max_height").forGetter(DreamVegetationFeature::maxHeight),
            Codec.floatRange(0.0F, 1.0F).fieldOf("density").forGetter(DreamVegetationFeature::density)
    ).apply(i, DreamVegetationFeature::new));

    private static final Direction[] NEIGHBORS = Direction.values();
    private static final int WRITE_FLAGS = 2;

    @Override
    public MapCodec<DreamVegetationFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (minHeight > maxHeight || origin.getY() <= level.getMinY() + 2
                || origin.getY() + maxHeight + 9 >= level.getMaxY()) {
            return false;
        }
        // Only the natural surface of this configured ecosystem is acceptable.
        if (!level.getBlockState(origin.below()).is(soil.getBlock())
                || !level.getBlockState(origin).isAir() || !level.getFluidState(origin).isEmpty()) {
            return false;
        }
        // Dedicated unobstructed arrival area. Not a distribution anchor.
        // 48 blocks include the outer reach of umbrella crowns and arched
        // branches; the safety clearing itself remains 36 blocks.
        if ((long) origin.getX() * origin.getX() + (long) origin.getZ() * origin.getZ() < 48L * 48L) {
            return false;
        }

        boolean tree = form.equals("sentinel") || form.equals("umbrella") || form.equals("arch");
        long seed = level.getSeed();
        double x = origin.getX();
        double z = origin.getZ();
        double warpX = noise(seed ^ 0x459L, x / 220.0, z / 220.0) * 45.0;
        double warpZ = noise(seed ^ 0xC13L, x / 220.0 + 91, z / 220.0 - 37) * 45.0;
        double wx = x + warpX, wz = z + warpZ;
        double forest = 0.67 * noise(seed ^ 0xA07L, wx / 145.0, wz / 145.0)
                + 0.33 * noise(seed ^ 0xE53L, wx / 61.0, wz / 61.0);
        double woodland = smooth(-0.22, 0.27, forest);
        double detail = 0.72 + 0.28 * ((noise(seed ^ 0x8D1L, x / 33.0, z / 33.0) + 1) * 0.5);
        double chance;
        switch (form) {
            case "sentinel" -> {
                // Isolated sentinels on high plateaus, otherwise only woodland.
                double highSolitary = origin.getY() > 122 && woodland < 0.19 ? 0.055 : 0;
                chance = density * Math.max(woodland * 0.94, highSolitary) * detail;
            }
            case "umbrella" -> chance = density * woodland * woodland * detail;
            case "arch" -> chance = density * woodland * 0.62 * detail;
            case "flower" -> chance = density * (0.32 + 0.68 * (1.0 - woodland)) * detail;
            case "moss" -> {
                double lowland = origin.getY() < 92 ? 1.0 : (origin.getY() < 130 ? 0.62 : 0.22);
                chance = density * (0.15 + 0.85 * woodland) * lowland * detail;
            }
            default -> {
                return false;
            }
        }
        if (random.nextFloat() >= chance) {
            return false;
        }
        // Save four neighbor-heightmap reads for unsuccessful cheap gates.
        // The same relief threshold is enforced for accepted candidates.
        int span = tree ? 4 : 3;
        int relief = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    origin.getX() + side.getStepX() * span, origin.getZ() + side.getStepZ() * span);
            relief = Math.max(relief, Math.abs(y - origin.getY()));
        }
        if (relief > (tree ? 4 : 3)) {
            return false;
        }
        if (!tree) {
            return patch(level, random, origin, form.equals("moss"));
        }

        int height = minHeight + random.nextInt(maxHeight - minHeight + 1);
        Map<BlockPos, BlockState> plan = new LinkedHashMap<>();
        switch (form) {
            case "sentinel" -> sentinel(plan, origin, height, random);
            case "umbrella" -> umbrella(plan, origin, height, random);
            case "arch" -> arch(plan, origin, height, random);
            default -> { return false; }
        }
        // Bound the number of planned writes, including future datapack
        // tuning. Candidate allocation occurs before this check.
        if (plan.size() > 2200) {
            return false;
        }
        connectLeaves(plan);

        // Full validation before ANY write: failed shapes leave no half-trees.
        for (Map.Entry<BlockPos, BlockState> entry : plan.entrySet()) {
            BlockPos at = entry.getKey();
            if (at.getY() <= level.getMinY() || at.getY() >= level.getMaxY()
                    || !level.ensureCanWrite(at) || !level.getBlockState(at).isAir()
                    || !level.getFluidState(at).isEmpty()) {
                return false;
            }
        }
        if (plan.isEmpty()) {
            return false;
        }
        for (Map.Entry<BlockPos, BlockState> entry : plan.entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), WRITE_FLAGS);
        }
        return true;
    }

    private void sentinel(Map<BlockPos, BlockState> plan, BlockPos root, int h, RandomSource random) {
        for (int y = 0; y < h; y++) {
            wood(plan, root.above(y), Direction.Axis.Y);
        }
        // Tall, tapering crown: a vertical spire rather than a vanilla sphere.
        for (int dy = -5; dy <= 5; dy++) {
            int radius = dy < -2 ? 2 : dy < 2 ? 3 : dy < 4 ? 2 : 1;
            crown(plan, root.above(h - 1 + dy), radius, 1, radius, random);
        }
        int arms = 3 + random.nextInt(2);
        for (int branch = 0; branch < arms; branch++) {
            double angle = (Math.PI * 2.0 * branch / arms) + random.nextDouble() * 0.42;
            int dx = (int) Math.round(Math.cos(angle) * 3);
            int dz = (int) Math.round(Math.sin(angle) * 3);
            int base = h - 7 + random.nextInt(3);
            for (int step = 1; step <= 3; step++) {
                BlockPos p = root.offset((int) Math.round(dx * step / 3.0),
                        base + step, (int) Math.round(dz * step / 3.0));
                wood(plan, p, Math.abs(dx) > Math.abs(dz) ? Direction.Axis.X : Direction.Axis.Z);
            }
            crown(plan, root.offset(dx, base + 4, dz), 2, 3, 2, random);
        }
    }

    private void umbrella(Map<BlockPos, BlockState> plan, BlockPos root, int h, RandomSource random) {
        for (int y = 0; y <= h; y++) {
            wood(plan, root.above(y), Direction.Axis.Y);
        }
        crown(plan, root.above(h + 1), 3, 1, 3, random);
        // Three or four asymmetric, horizontal limbs carrying overlapping plates.
        int limbs = 3 + random.nextInt(2);
        double rotation = random.nextDouble() * 6.283185307179586;
        for (int limb = 0; limb < limbs; limb++) {
            double theta = rotation + limb * Math.PI * 2 / limbs + random.nextDouble() * 0.38;
            int length = 3 + random.nextInt(3);
            int dx = (int) Math.round(Math.cos(theta) * length);
            int dz = (int) Math.round(Math.sin(theta) * length);
            int by = h - 1 + random.nextInt(2);
            for (int step = 1; step <= length; step++) {
                BlockPos p = root.offset((int) Math.round(dx * step / (double) length),
                        by + (step > length / 2 ? 1 : 0),
                        (int) Math.round(dz * step / (double) length));
                wood(plan, p, Math.abs(dx) >= Math.abs(dz) ? Direction.Axis.X : Direction.Axis.Z);
            }
            int radius = 3 + random.nextInt(2);
            crown(plan, root.offset(dx, by + 2, dz), radius, 1, radius - random.nextInt(2), random);
        }
    }

    private void arch(Map<BlockPos, BlockState> plan, BlockPos root, int h, RandomSource random) {
        int dirX = random.nextBoolean() ? 1 : -1;
        int dirZ = random.nextBoolean() ? 1 : -1;
        boolean alongX = random.nextBoolean();
        BlockPos previous = root;
        for (int y = 0; y <= h; y++) {
            double t = y / (double) h;
            int sweep = (int) Math.round(5.0 * Math.sin(t * Math.PI * 0.5));
            int lateral = (int) Math.round(1.4 * Math.sin(t * Math.PI));
            BlockPos here = alongX
                    ? root.offset(dirX * sweep, y, dirZ * lateral)
                    : root.offset(dirX * lateral, y, dirZ * sweep);
            if (y > 0 && (previous.getX() != here.getX() || previous.getZ() != here.getZ())) {
                wood(plan, new BlockPos(previous.getX(), here.getY(), previous.getZ()),
                        alongX ? Direction.Axis.X : Direction.Axis.Z);
            }
            wood(plan, here, y < h / 2 ? Direction.Axis.Y
                    : (alongX ? Direction.Axis.X : Direction.Axis.Z));
            if (y > h / 2 && y % 3 == 0) {
                crown(plan, here.above(), 2, 2, 2, random);
            }
            previous = here;
        }
        // A second, descending arm outlines an unmistakable open arch.
        BlockPos tip = previous;
        for (int s = 1; s <= 3; s++) {
            BlockPos p = alongX ? tip.offset(dirX * s, -s / 2, 0)
                    : tip.offset(0, -s / 2, dirZ * s);
            wood(plan, p, alongX ? Direction.Axis.X : Direction.Axis.Z);
            if (s > 1) {
                crown(plan, p.above(), 2, 2, 2, random);
            }
        }
        crown(plan, tip.above(2), 3, 2, 3, random);
    }

    private void wood(Map<BlockPos, BlockState> plan, BlockPos p, Direction.Axis axis) {
        BlockState state = log;
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            state = state.setValue(RotatedPillarBlock.AXIS, axis);
        }
        plan.put(p.immutable(), state);
    }

    private void crown(Map<BlockPos, BlockState> plan, BlockPos center,
                       int rx, int ry, int rz, RandomSource random) {
        for (int dy = -ry; dy <= ry; dy++) {
            for (int dx = -rx; dx <= rx; dx++) {
                for (int dz = -rz; dz <= rz; dz++) {
                    double n = (double) dx * dx / (rx * rx)
                            + (double) dy * dy / (ry * ry)
                            + (double) dz * dz / (rz * rz);
                    if (n > 1.0 || (n > 0.78 && random.nextFloat() < 0.18F)) {
                        continue;
                    }
                    BlockPos p = center.offset(dx, dy, dz).immutable();
                    if (!plan.containsKey(p)) {
                        plan.put(p, leaves);
                    }
                }
            }
        }
    }

    /**
     * Breadth-first leaf-distance propagation through the planned canopy.
     * Natural leaves stay nonpersistent, connected and can decay when logs break.
     */
    private void connectLeaves(Map<BlockPos, BlockState> plan) {
        if (!leaves.hasProperty(LeavesBlock.DISTANCE)) {
            return;
        }
        Deque<BlockPos> queue = new ArrayDeque<>();
        Map<BlockPos, Integer> distances = new HashMap<>();
        for (Map.Entry<BlockPos, BlockState> e : plan.entrySet()) {
            if (e.getValue().is(log.getBlock())) {
                for (Direction dir : NEIGHBORS) {
                    BlockPos next = e.getKey().relative(dir);
                    if (plan.containsKey(next) && plan.get(next).is(leaves.getBlock())
                            && !distances.containsKey(next)) {
                        distances.put(next, 1);
                        queue.add(next);
                    }
                }
            }
        }
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            int distance = distances.get(current);
            if (distance >= 6) {
                continue;
            }
            for (Direction dir : NEIGHBORS) {
                BlockPos next = current.relative(dir);
                if (plan.containsKey(next) && plan.get(next).is(leaves.getBlock())
                        && !distances.containsKey(next)) {
                    distances.put(next, distance + 1);
                    queue.addLast(next);
                }
            }
        }
        Iterator<Map.Entry<BlockPos, BlockState>> iter = plan.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<BlockPos, BlockState> e = iter.next();
            if (!e.getValue().is(leaves.getBlock())) {
                continue;
            }
            Integer distance = distances.get(e.getKey());
            if (distance == null) {
                iter.remove();
            } else {
                BlockState state = e.getValue().setValue(LeavesBlock.DISTANCE, distance);
                if (state.hasProperty(LeavesBlock.PERSISTENT)) {
                    state = state.setValue(LeavesBlock.PERSISTENT, false);
                }
                e.setValue(state);
            }
        }
    }

    private boolean patch(WorldGenLevel level, RandomSource random, BlockPos center, boolean glowing) {
        int radius = 2 + random.nextInt(4);
        int planted = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double dist = (dx * dx + dz * dz) / (double) (radius * radius);
                if (dist > 1.0 || random.nextFloat() > (glowing ? 0.52 : 0.38) * (1.0 - dist * 0.46)) {
                    continue;
                }
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (Math.abs(y - center.getY()) > 2) {
                    continue;
                }
                BlockPos floor = new BlockPos(x, y - 1, z);
                BlockPos above = floor.above();
                if (!level.ensureCanWrite(floor) || !level.ensureCanWrite(above)
                        || !level.getBlockState(floor).is(soil.getBlock())
                        || !level.getFluidState(floor).isEmpty()
                        || !level.getBlockState(above).isAir() || !level.getFluidState(above).isEmpty()) {
                    continue;
                }
                // Glowing moss is a full moss block, not a floating overlay.
                if (level.setBlock(glowing ? floor : above, glowing ? moss : flower, WRITE_FLAGS)) {
                    planted++;
                }
            }
        }
        return planted > 0;
    }

    private static double smooth(double low, double high, double n) {
        double t = Math.max(0.0, Math.min(1.0, (n - low) / (high - low)));
        return t * t * (3.0 - 2.0 * t);
    }

    private static double noise(long seed, double x, double z) {
        long ix = (long) Math.floor(x), iz = (long) Math.floor(z);
        double fx = smooth(0, 1, x - ix), fz = smooth(0, 1, z - iz);
        double a = hash(seed, ix, iz), b = hash(seed, ix + 1, iz);
        double c = hash(seed, ix, iz + 1), d = hash(seed, ix + 1, iz + 1);
        return (a + (b - a) * fx) * (1 - fz) + (c + (d - c) * fx) * fz;
    }

    private static double hash(long seed, long x, long z) {
        long value = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return ((value >>> 11) * 0x1.0p-53) * 2.0 - 1.0;
    }
}
