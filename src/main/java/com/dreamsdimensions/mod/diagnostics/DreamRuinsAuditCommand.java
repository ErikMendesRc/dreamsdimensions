package com.dreamsdimensions.mod.diagnostics;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Opt-in, loaded-only natural-structure/chest audit for 26.3 CI.
 *
 * NEVER registered in the distributed default runtime. The operator or CI
 * harness explicitly loads only the requested sample chunks beforehand.
 * It observes chest BE, untouched loot table key, walkable neighbor and
 * natural understructure support without opening/rolling/replenishing loot.
 */
public final class DreamRuinsAuditCommand {
    private static final int MAX_RADIUS = 24;

    private DreamRuinsAuditCommand() {}

    public static void register(RegisterCommandsEvent event) {
        if (!Boolean.getBoolean("dreamsdimensions.enableRuinsAudit")) return;
        event.getDispatcher().register(Commands.literal("dream_ruins_audit")
            .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
            .then(Commands.argument("variant", StringArgumentType.word())
            .then(Commands.argument("x", IntegerArgumentType.integer())
            .then(Commands.argument("z", IntegerArgumentType.integer())
            .then(Commands.argument("radius", IntegerArgumentType.integer(8, MAX_RADIUS))
                .executes(ctx -> inspect(ctx.getSource(),
                    StringArgumentType.getString(ctx, "variant"),
                    IntegerArgumentType.getInteger(ctx, "x"),
                    IntegerArgumentType.getInteger(ctx, "z"),
                    IntegerArgumentType.getInteger(ctx, "radius"))))))));
    }

    private static int inspect(CommandSourceStack source, String variant,
            int centerX, int centerZ, int radius) {
        ResourceKey<Level> dimension;
        String lootId;
        switch (variant) {
            case "blue" -> {
                dimension = DreamDimensionCatalog.BLUE_FIELD;
                lootId = "dreamsdimensions:chests/blue_dream_ruins";
            }
            case "fragmented" -> {
                dimension = DreamDimensionCatalog.DREAMSCAPE;
                lootId = "dreamsdimensions:chests/fragmented_dream_ruins";
            }
            default -> {
                return fail(source, "invalid_variant");
            }
        }
        ServerLevel level = source.getServer().getLevel(dimension);
        if (level == null) return fail(source, "dimension_missing");
        int startCx = (centerX - radius) >> 4, endCx = (centerX + radius) >> 4;
        int startCz = (centerZ - radius) >> 4, endCz = (centerZ + radius) >> 4;
        for (int cx = startCx; cx <= endCx; cx++) {
            for (int cz = startCz; cz <= endCz; cz++) {
                if (!level.hasChunkAt(new BlockPos(cx << 4, 128, cz << 4))) {
                    return fail(source, "chunk_not_loaded_" + cx + "_" + cz);
                }
            }
        }

        int chestCount = 0, correctLoot = 0, supportSafe = 0;
        List<String> positions = new ArrayList<>();
        for (int cx = startCx; cx <= endCx; cx++) {
            for (int cz = startCz; cz <= endCz; cz++) {
                for (Map.Entry<BlockPos, BlockEntity> entry
                        : level.getChunk(cx, cz).getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    if (Math.abs(pos.getX() - centerX) > radius
                            || Math.abs(pos.getZ() - centerZ) > radius
                            || !level.getBlockState(pos).is(Blocks.CHEST)
                            || !(entry.getValue() instanceof RandomizableContainerBlockEntity container)) {
                        continue;
                    }
                    chestCount++;
                    ResourceKey<LootTable> table = container.getLootTable();
                    boolean lootMatches = table != null && lootId.equals(table.identifier().toString());
                    if (lootMatches) correctLoot++;
                    boolean walkable = isReachableLidAndSide(level, pos);
                    int support = supportsBelow(level, pos);
                    boolean safe = walkable && support >= 5;
                    if (safe) supportSafe++;
                    positions.add(pos.getX() + "," + pos.getY() + "," + pos.getZ()
                            + ":loot=" + (table == null ? "already_resolved_or_absent" : table.identifier())
                            + ":support=" + support + "/9:walkable=" + walkable
                            + ":biome=" + level.getBiome(pos).unwrapKey()
                                    .map(k -> k.identifier().toString()).orElse("unknown"));
                }
            }
        }
        String line = "DREAM_RUINS_AUDIT status=OK variant=" + variant
                + " dimension=" + dimension.identifier()
                + " origin=" + centerX + "," + centerZ
                + " radius=" + radius + " chests=" + chestCount
                + " loot_matches=" + correctLoot
                + " stable_chests=" + supportSafe;
        source.sendSuccess(() -> Component.literal(line), false);
        DreamsDimensions.LOGGER.info(line);
        for (String position : positions) {
            DreamsDimensions.LOGGER.info("DREAM_RUINS_CHEST variant={} {}", variant, position);
        }
        return chestCount;
    }

    private static boolean isReachableLidAndSide(ServerLevel level, BlockPos chest) {
        if (!level.getBlockState(chest.above()).isAir()) return false;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos feet = chest.relative(side);
            if (level.getBlockState(feet).isAir()
                    && level.getBlockState(feet.above()).isAir()
                    && level.getBlockState(feet.below()).isFaceSturdy(
                            level, feet.below(), Direction.UP)) return true;
        }
        return false;
    }

    private static int supportsBelow(ServerLevel level, BlockPos chest) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos underFloor = chest.offset(dx, -2, dz);
                if (level.getBlockState(underFloor).isFaceSturdy(
                        level, underFloor, Direction.UP)) count++;
            }
        }
        return count;
    }

    private static int fail(CommandSourceStack source, String reason) {
        String result = "DREAM_RUINS_AUDIT status=SKIPPED reason=" + reason;
        source.sendFailure(Component.literal(result));
        DreamsDimensions.LOGGER.warn(result);
        return 0;
    }
}
