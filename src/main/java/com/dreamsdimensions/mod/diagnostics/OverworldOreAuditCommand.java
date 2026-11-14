package com.dreamsdimensions.mod.diagnostics;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Bounded, opt-in, read-only survey of ACTUAL generated Overworld blocks.
 * It is deliberately NOT registered in normal gameplay or production.
 *
 * External CI/operator must preload chunks; this class never loads chunks,
 * changes blocks, gives dust or runs a worldgen algorithm.
 *
 * A report of 0 means 0 ore voxels in the loaded sampled region, NOT
 * absence from all seeds or impossibility of finding deposits elsewhere.
 */
public final class OverworldOreAuditCommand {
    private static final int MAX_SIDE_CHUNKS = 8;
    private static final int MIN_SCAN_Y = -64;
    private static final int MAX_SCAN_Y = 64;
    private static final int MAX_HITS_PER_TYPE = 5;
    private static final int[][] SIDES = {
        {1, 0, 0}, {-1, 0, 0}, {0, 1, 0},
        {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
    };

    private OverworldOreAuditCommand() {}

    public static void register(RegisterCommandsEvent event) {
        if (!Boolean.getBoolean("dreamsdimensions.enableOreAudit")) return;
        event.getDispatcher().register(Commands.literal("dream_ore_audit")
            .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
            .then(Commands.argument("chunk_x", IntegerArgumentType.integer())
            .then(Commands.argument("chunk_z", IntegerArgumentType.integer())
            .then(Commands.argument("chunks_per_side",
                    IntegerArgumentType.integer(1, MAX_SIDE_CHUNKS))
                    .executes(ctx -> inspect(ctx.getSource(),
                            IntegerArgumentType.getInteger(ctx, "chunk_x"),
                            IntegerArgumentType.getInteger(ctx, "chunk_z"),
                            IntegerArgumentType.getInteger(ctx, "chunks_per_side")))))));
    }

    private static int inspect(CommandSourceStack source, int chunkX, int chunkZ, int side) {
        ServerLevel level = source.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return skipped(source, "missing_overworld");
        int yMin = Math.max(MIN_SCAN_Y, level.getMinY());
        int yMax = Math.min(MAX_SCAN_Y, level.getMaxY() - 1);
        if (yMax < yMin) return skipped(source, "height_range_empty");

        // Atomic preflight: reject a partially generated survey rather than
        // silently counting an empty/unloaded chunk as 0 ore.
        for (int cx = chunkX; cx < chunkX + side; cx++) {
            for (int cz = chunkZ; cz < chunkZ + side; cz++) {
                if (!level.hasChunkAt(new BlockPos(cx << 4, 64, cz << 4))) {
                    return skipped(source, "chunk_not_loaded_" + cx + "_" + cz);
                }
            }
        }

        int normal = 0, deep = 0, exposedNormal = 0, exposedDeep = 0;
        int chunksWithOre = 0, sampledChunks = 0, unknownExposure = 0;
        int[] byBandNormal = new int[4], byBandDeep = new int[4];
        List<String> samples = new ArrayList<>();
        Map<String, int[]> perBiome = new HashMap<>();
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();

        for (int cx = chunkX; cx < chunkX + side; cx++) {
            for (int cz = chunkZ; cz < chunkZ + side; cz++) {
                int localNormal = 0, localDeep = 0;
                for (int y = yMin; y <= yMax; y++) {
                    int band = y < -32 ? 0 : y < 0 ? 1 : y < 32 ? 2 : 3;
                    for (int x = cx << 4; x < (cx << 4) + 16; x++) {
                        for (int z = cz << 4; z < (cz << 4) + 16; z++) {
                            probe.set(x, y, z);
                            BlockState state = level.getBlockState(probe);
                            boolean regular = state.is(ModBlocks.DREAM_ORE.get());
                            boolean deepslate = state.is(ModBlocks.DEEPSLATE_DREAM_ORE.get());
                            if (!regular && !deepslate) continue;

                            boolean exposed = false;
                            boolean unknown = false;
                            for (int[] shift : SIDES) {
                                BlockPos adjacent = probe.offset(shift[0], shift[1], shift[2]);
                                if (!level.hasChunkAt(adjacent)) {
                                    unknown = true;
                                    continue;
                                }
                                if (level.getBlockState(adjacent).isAir()) {
                                    exposed = true;
                                    break;
                                }
                            }
                            if (unknown && !exposed) unknownExposure++;
                            if (regular) {
                                normal++;
                                localNormal++;
                                byBandNormal[band]++;
                                if (exposed) exposedNormal++;
                            } else {
                                deep++;
                                localDeep++;
                                byBandDeep[band]++;
                                if (exposed) exposedDeep++;
                            }

                            String biome = level.getBiome(probe).unwrapKey()
                                    .map(key -> key.identifier().toString()).orElse("unknown");
                            int[] biomeCounts = perBiome.computeIfAbsent(biome, ignored -> new int[2]);
                            biomeCounts[regular ? 0 : 1]++;
                            if (samples.size() < 2 * MAX_HITS_PER_TYPE) {
                                String kind = regular ? "dream_ore" : "deepslate_dream_ore";
                                // Grounded actual location; never present as a safe player landing.
                                samples.add(kind + "@" + x + "," + y + "," + z
                                        + ":biome=" + biome + ":exposed=" + exposed);
                            }
                        }
                    }
                }
                sampledChunks++;
                if (localNormal + localDeep > 0) chunksWithOre++;
            }
        }

        String summary = "DREAM_ORE_AUDIT status=OK dimension=minecraft:overworld "
                + "start_chunk=" + chunkX + "," + chunkZ + " side=" + side
                + " chunks=" + sampledChunks + " chunks_with_ore=" + chunksWithOre
                + " dream_ore=" + normal + " deepslate_dream_ore=" + deep
                + " exposed_dream_ore=" + exposedNormal
                + " exposed_deepslate_dream_ore=" + exposedDeep
                + " unknown_exposure=" + unknownExposure
                + " y_range=" + yMin + ":" + yMax;
        source.sendSuccess(() -> Component.literal(summary), false);
        DreamsDimensions.LOGGER.info(summary);
        DreamsDimensions.LOGGER.info("DREAM_ORE_AUDIT_BANDS origin={},{} normal={} deep={} biomes={}",
                chunkX, chunkZ, java.util.Arrays.toString(byBandNormal),
                java.util.Arrays.toString(byBandDeep), perBiome);
        for (String hit : samples) {
            DreamsDimensions.LOGGER.info("DREAM_ORE_AUDIT_HIT start_chunk={},{} {}", chunkX, chunkZ, hit);
        }
        return normal + deep;
    }

    private static int skipped(CommandSourceStack source, String reason) {
        String message = "DREAM_ORE_AUDIT status=SKIPPED reason=" + reason;
        source.sendFailure(Component.literal(message));
        DreamsDimensions.LOGGER.warn(message);
        return 0;
    }
}
