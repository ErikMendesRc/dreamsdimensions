package com.dreamsdimensions.mod.diagnostics;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Task 7.4: opt-in, read-only measurement of naturally generated Marked Stone.
 * Available only with -Ddreamsdimensions.enableMarkedStoneAudit=true.
 * Inspects a bounded region of ALREADY loaded chunks, never loads chunks,
 * mutates blocks, spawns structures or creates persistent forced tickets.
 */
public final class MarkedStoneAuditCommand {
    private static final int MAX_SIDE = 4;
    private static final int MIN_Y = -8;
    private static final int MAX_Y = 192;
    private static final int[][] NEIGHBORS = {
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0},
            {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
    };

    private MarkedStoneAuditCommand() {}

    public static void register(RegisterCommandsEvent event) {
        if (!Boolean.getBoolean("dreamsdimensions.enableMarkedStoneAudit")) return;
        event.getDispatcher().register(Commands.literal("marked_stone_audit")
                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .then(Commands.argument("chunk_x", IntegerArgumentType.integer())
                .then(Commands.argument("chunk_z", IntegerArgumentType.integer())
                .then(Commands.argument("chunks_per_side", IntegerArgumentType.integer(1, MAX_SIDE))
                        .executes(ctx -> inspect(ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "chunk_x"),
                                IntegerArgumentType.getInteger(ctx, "chunk_z"),
                                IntegerArgumentType.getInteger(ctx, "chunks_per_side")))))));
    }

    private static int inspect(CommandSourceStack source, int chunkX, int chunkZ, int side) {
        ServerLevel level = source.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return skipped(source, "no_overworld");
        int minY = Math.max(MIN_Y, level.getMinY());
        int maxY = Math.min(MAX_Y, level.getMaxY() - 1);
        if (maxY < minY) return skipped(source, "empty_y_range");

        for (int cx = chunkX; cx < chunkX + side; cx++) {
            for (int cz = chunkZ; cz < chunkZ + side; cz++) {
                if (!level.hasChunkAt(new BlockPos(cx << 4, 64, cz << 4))) {
                    return skipped(source, "chunk_not_loaded_" + cx + "_" + cz);
                }
            }
        }

        int total = 0;
        int exposed = 0;
        int unknown = 0;
        int chunksWith = 0;
        int scannedChunks = 0;
        String first = "none";
        String firstExposed = "none";
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();

        for (int cx = chunkX; cx < chunkX + side; cx++) {
            for (int cz = chunkZ; cz < chunkZ + side; cz++) {
                int local = 0;
                for (int y = minY; y <= maxY; y++) {
                    for (int x = cx << 4; x < (cx << 4) + 16; x++) {
                        for (int z = cz << 4; z < (cz << 4) + 16; z++) {
                            probe.set(x, y, z);
                            if (!level.getBlockState(probe).is(ModBlocks.MARKED_STONE.get())) {
                                continue;
                            }
                            total++;
                            local++;
                            if ("none".equals(first)) first = x + "," + y + "," + z;
                            boolean air = false;
                            boolean neighborNotLoaded = false;
                            for (int[] shift : NEIGHBORS) {
                                BlockPos neighbor = probe.offset(shift[0], shift[1], shift[2]);
                                if (!level.hasChunkAt(neighbor)) {
                                    neighborNotLoaded = true;
                                    continue;
                                }
                                if (level.getBlockState(neighbor).isAir()) {
                                    air = true;
                                    break;
                                }
                            }
                            if (air) {
                                exposed++;
                                if ("none".equals(firstExposed)) {
                                    firstExposed = x + "," + y + "," + z;
                                }
                            } else if (neighborNotLoaded) {
                                unknown++;
                            }
                        }
                    }
                }
                scannedChunks++;
                if (local > 0) chunksWith++;
            }
        }

        String report = "MARKED_STONE_AUDIT status=OK dimension=minecraft:overworld"
                + " start_chunk=" + chunkX + "," + chunkZ
                + " side=" + side
                + " chunks=" + scannedChunks
                + " chunks_with_marked=" + chunksWith
                + " marked_stone=" + total
                + " exposed_marked_stone=" + exposed
                + " unknown_exposure=" + unknown
                + " y_range=" + minY + ":" + maxY
                + " first_any=" + first
                + " first_exposed=" + firstExposed;
        source.sendSuccess(() -> Component.literal(report), false);
        DreamsDimensions.LOGGER.info(report);
        return total;
    }

    private static int skipped(CommandSourceStack source, String reason) {
        String message = "MARKED_STONE_AUDIT status=SKIPPED reason=" + reason;
        source.sendFailure(Component.literal(message));
        DreamsDimensions.LOGGER.warn(message);
        return 0;
    }
}
