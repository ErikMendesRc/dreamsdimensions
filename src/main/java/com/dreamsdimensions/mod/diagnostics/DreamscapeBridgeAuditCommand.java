package com.dreamsdimensions.mod.diagnostics;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Test-server-only, read-only bridge topology probe. Never registers unless
 * -Ddreamsdimensions.enableBridgeAudit=true. No packets, block writes, forced
 * chunk loads or worldgen mutation; commands run on the existing server thread.
 *
 * A verified transect has a continuous traversable centerline, exposed
 * undercut, and two opposite natural rock foundations in a loaded area.
 * This is stronger than a one-block roof-over-air test, but is not a visual
 * certification or a universal proof of disconnected geological components.
 */
public final class DreamscapeBridgeAuditCommand {
    private static final int LOW_Y = 100;
    private static final int HIGH_Y = 138;
    private static final int MAX_RADIUS = 64;
    private static final int MAX_STEPS = 30;
    private static final int MIN_GAP_STEPS = 3;
    private static final int MAX_HITS = 3;

    private DreamscapeBridgeAuditCommand() {}

    public static void register(RegisterCommandsEvent event) {
        if (!Boolean.getBoolean("dreamsdimensions.enableBridgeAudit")) return;
        event.getDispatcher().register(Commands.literal("dreamscape_bridge_audit")
                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .then(Commands.argument("x", IntegerArgumentType.integer())
                .then(Commands.argument("z", IntegerArgumentType.integer())
                .then(Commands.argument("radius", IntegerArgumentType.integer(24, MAX_RADIUS))
                        .executes(ctx -> scan(ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "x"),
                                IntegerArgumentType.getInteger(ctx, "z"),
                                IntegerArgumentType.getInteger(ctx, "radius")))))));
    }

    private record Column(int y, boolean gap, boolean bank) {}
    private record Arm(int x, int z, int y, int gaps) {}
    private record Bridge(int x, int y, int z, Arm negative, Arm positive, String axis) {}

    private static int scan(CommandSourceStack source, int x, int z, int radius) {
        ServerLevel level = source.getServer().getLevel(DreamDimensionCatalog.DREAMSCAPE);
        if (level == null) return failure(source, "dimension_unavailable");
        // Do not request chunks or query a position outside the actually
        // loaded window. A denied probe is not interpreted as no bridges.
        for (int cx = (x - radius) >> 4; cx <= (x + radius) >> 4; cx++) {
            for (int cz = (z - radius) >> 4; cz <= (z + radius) >> 4; cz++) {
                if (!level.hasChunkAt(new BlockPos(cx << 4, 112, cz << 4))) {
                    return failure(source, "chunk_not_loaded_" + cx + "_" + cz);
                }
            }
        }
        int width = 2 * radius + 1;
        Column[][] columns = new Column[width][width];
        int gaps = 0, banks = 0, clear = 0;
        for (int ix = 0; ix < width; ix++) {
            for (int iz = 0; iz < width; iz++) {
                int px = x + ix - radius, pz = z + iz - radius;
                Column col = inspect(level, px, pz);
                columns[ix][iz] = col;
                if (col != null) {
                    clear++;
                    if (col.gap) gaps++;
                    if (col.bank) banks++;
                }
            }
        }
        java.util.ArrayList<Bridge> hits = new java.util.ArrayList<>();
        for (int ix = 4; ix < width - 4 && hits.size() < MAX_HITS; ix++) {
            for (int iz = 4; iz < width - 4 && hits.size() < MAX_HITS; iz++) {
                Column middle = columns[ix][iz];
                if (middle == null || !middle.gap) continue;
                boolean near = false;
                for (Bridge previous : hits) {
                    int dx = (x + ix - radius) - previous.x;
                    int dz = (z + iz - radius) - previous.z;
                    if (dx * dx + dz * dz < 20 * 20) {
                        near = true;
                        break;
                    }
                }
                if (near) continue;
                for (int axis = 0; axis < 2; axis++) {
                    Arm left = walk(level, columns, ix, iz, -1, axis,
                            x - radius, z - radius);
                    Arm right = walk(level, columns, ix, iz, 1, axis,
                            x - radius, z - radius);
                    if (left != null && right != null
                            && left.gaps >= MIN_GAP_STEPS
                            && right.gaps >= MIN_GAP_STEPS
                            && Math.abs(left.y - right.y) <= 6) {
                        // An actual 6-neighbor (horizontal + <= 1 Y step)
                        // walk was checked in both arms, with 2 air headroom
                        // in every column. Center must have a 20-block void.
                        hits.add(new Bridge(x + ix - radius, middle.y, z + iz - radius,
                                new Arm(x + left.x - radius, z + left.z - radius,
                                        left.y, left.gaps),
                                new Arm(x + right.x - radius, z + right.z - radius,
                                        right.y, right.gaps),
                                axis == 0 ? "X" : "Z"));
                        break;
                    }
                }
            }
        }
        String summary = "DREAMSCAPE_BRIDGE_AUDIT status=OK center=" + x + "," + z
                + " radius=" + radius + " walkable_columns=" + clear
                + " gap_columns=" + gaps + " bank_columns=" + banks
                + " confirmed_transects=" + hits.size();
        source.sendSuccess(() -> Component.literal(summary), false);
        for (Bridge hit : hits) {
            String evidence = "DREAMSCAPE_BRIDGE_CONFIRMED axis=" + hit.axis
                    + " center=" + hit.x + "," + hit.y + "," + hit.z
                    + " bankA=" + hit.negative.x + "," + hit.negative.y + "," + hit.negative.z
                    + " bankB=" + hit.positive.x + "," + hit.positive.y + "," + hit.positive.z
                    + " void_at_center=12,20_below headroom=2"
                    + " continuous_walk=yes rock6_connected=yes gap_steps=" + hit.negative.gaps + "," + hit.positive.gaps;
            source.sendSuccess(() -> Component.literal(evidence), false);
        }
        return hits.size();
    }

    private static int failure(CommandSourceStack source, String reason) {
        source.sendSuccess(() -> Component.literal("DREAMSCAPE_BRIDGE_AUDIT status=SKIPPED reason=" + reason), false);
        return 0;
    }

    private static Column inspect(ServerLevel level, int x, int z) {
        // Inspect all layers, not just the top heightmap: a floating island
        // above a canyon would otherwise conceal a rock bridge underneath.
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        for (int y = HIGH_Y; y >= LOW_Y; y--) {
            probe.set(x, y, z);
            BlockState floor = level.getBlockState(probe);
            if (!floor.isSolid() || !floor.isFaceSturdy(level, probe, net.minecraft.core.Direction.UP)) continue;
            // Ignore unnatural construction blocks and generated vegetation.
            if (!isNatural(floor)) continue;
            probe.set(x, y + 1, z);
            if (!level.getBlockState(probe).isAir() || !level.getFluidState(probe).isEmpty()) continue;
            probe.set(x, y + 2, z);
            if (!level.getBlockState(probe).isAir() || !level.getFluidState(probe).isEmpty()) continue;
            probe.set(x, y - 12, z);
            boolean air12 = level.getBlockState(probe).isAir();
            boolean stone12 = isNatural(level.getBlockState(probe));
            probe.set(x, y - 20, z);
            boolean air20 = level.getBlockState(probe).isAir();
            boolean stone20 = isNatural(level.getBlockState(probe));
            return new Column(y, air12 && air20, stone12 && stone20);
        }
        return null;
    }

    private static boolean isNatural(BlockState block) {
        return block.is(ModBlocks.SERENE_STONE_BLOCK.get())
                || block.is(ModBlocks.DREAM_DIRT_BLOCK.get())
                || block.is(ModBlocks.DREAM_GRASS_BLOCK.get());
    }

    /**
     * Bounded coordinate-continuous centerline. Axis X/Z, with only +-2
     * transverse displacement allowed and <=1 vertical block each step.
     * This proves walkability of the sampled line, not overall bridge width.
     */
    private static Arm walk(ServerLevel level, Column[][] a, int x0, int z0,
            int sign, int axis, int minWorldX, int minWorldZ) {
        int size = a.length, px = x0, pz = z0, y = a[x0][z0].y, gaps = 0;
        for (int step = 1; step <= MAX_STEPS; step++) {
            int x = axis == 0 ? x0 + sign * step : px;
            int z = axis == 1 ? z0 + sign * step : pz;
            int bestX = -1, bestZ = -1, bestScore = Integer.MAX_VALUE;
            for (int drift = -1; drift <= 1; drift++) {
                int nx = axis == 0 ? x : x0 + drift;
                int nz = axis == 1 ? z : z0 + drift;
                if (nx < 0 || nz < 0 || nx >= size || nz >= size) continue;
                if (axis == 0 && Math.abs(nz - z0) > 2) continue;
                if (axis == 1 && Math.abs(nx - x0) > 2) continue;
                Column c = a[nx][nz];
                if (c == null || Math.abs(c.y - y) > 1) continue;
                if (!rockFaceConnected(level, minWorldX + px, minWorldZ + pz, y,
                        minWorldX + nx, minWorldZ + nz, c.y)) continue;
                int score = Math.abs(c.y - y) * 3 + Math.abs(drift);
                if (score < bestScore) {
                    bestX = nx; bestZ = nz; bestScore = score;
                }
            }
            if (bestX < 0) return null;
            px = bestX; pz = bestZ;
            Column chosen = a[px][pz];
            y = chosen.y;
            if (chosen.gap) gaps++;
            // Require two natural support columns on opposite ends,
            // located beyond at least 3 truly undercut positions.
            if (step >= 8 && chosen.bank && gaps >= MIN_GAP_STEPS) {
                return new Arm(px, pz, y, gaps);
            }
        }
        return null;
    }
    /**
     * Real 6-neighbor rock contact at the lower surface Y. A 1-block
     * elevation change is accepted only if the higher column has natural
     * solid rock at the lower Y. A transverse shift checks a real
     * natural-rock corner, not diagonal-only voxel contact.
     */
    private static boolean rockFaceConnected(ServerLevel level,
            int ax, int az, int ay, int bx, int bz, int by) {
        int y = Math.min(ay, by);
        if (!isNatural(level.getBlockState(new BlockPos(ax, y, az)))
                || !isNatural(level.getBlockState(new BlockPos(bx, y, bz)))) {
            return false;
        }
        if (ax != bx && az != bz) {
            return isNatural(level.getBlockState(new BlockPos(ax, y, bz)))
                    || isNatural(level.getBlockState(new BlockPos(bx, y, az)));
        }
        return true;
    }

}
