package com.dreamsdimensions.mod.entity;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.registry.ModEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Low-frequency, player-proximate island population maintenance.
 *
 * Vanilla CREATURE spawning is sparse after generation and almost absent in
 * already explored chunks. This conservative ecological assist checks only
 * loaded terrain every 10 seconds, respects spawn_mobs, and stops at a small
 * local cap. It never changes blocks or asks Minecraft to load a chunk.
 */
public final class FragmentoIslandPopulation {
    static final int PERIOD_TICKS = 200;
    static final int CANDIDATE_LIMIT = 48;
    static final int LOCAL_CAP = 6;
    static final int MAX_NEW_MOBS_PER_PASS = 2;
    private static final int INNER_RADIUS = 16;
    private static final int OUTER_RADIUS = 64;
    private static final int MIN_PLAYER_Y = 145;
    private static final int MAX_PLAYER_Y = 244;
    private static final double MIN_SPACING_SQR = 14.0 * 14.0;

    private FragmentoIslandPopulation() {}

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)
                || level.dimension() != DreamDimensionCatalog.DREAMSCAPE
                || player.isSpectator() || !player.isAlive()
                || player.getY() < MIN_PLAYER_Y || player.getY() > MAX_PLAYER_Y
                || !level.getGameRules().get(GameRules.SPAWN_MOBS)
                || (player.tickCount + Math.floorMod(player.getUUID().hashCode(), PERIOD_TICKS))
                        % PERIOD_TICKS != 0) {
            return;
        }
        replenish(level, player);
    }

    private static void replenish(ServerLevel level, ServerPlayer player) {
        Vec3 origin = player.position();
        AABB surrounding = new AABB(player.getX() - 72, FragmentoIslandHabitat.MIN_SPAWN_Y,
                player.getZ() - 72, player.getX() + 72,
                FragmentoIslandHabitat.MAX_SPAWN_Y + 10, player.getZ() + 72);
        List<FragmentoErranteEntity> existing = level.getEntitiesOfClass(
                FragmentoErranteEntity.class, surrounding, FragmentoErranteEntity::isAlive);
        if (existing.size() >= LOCAL_CAP) return;

        // No more than 48 heightmap samples per player per ten seconds.
        // Spawn at one good perch per pass; let the mob AI form small groups.
        RandomSource random = level.getRandom();
        int spawned = 0;
        for (int attempt = 0; attempt < CANDIDATE_LIMIT
                && spawned < MAX_NEW_MOBS_PER_PASS
                && existing.size() + spawned < LOCAL_CAP; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = INNER_RADIUS + random.nextDouble() * (OUTER_RADIUS - INNER_RADIUS);
            int x = (int) Math.floor(origin.x + Math.cos(angle) * radius);
            int z = (int) Math.floor(origin.z + Math.sin(angle) * radius);
            BlockPos chunkProbe = new BlockPos(x, 192, z);
            if (!level.hasChunkAt(chunkProbe)) continue;

            // Heightmap samples can select mainland or an island. Check the
            // *actual* blocks/void before any living entity is created.
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos surface = new BlockPos(x, y, z);
            if (!FragmentoIslandHabitat.isIslandSurface(level, surface)
                    || Math.abs(y - origin.y) > 65
                    || origin.distanceToSqr(Vec3.atBottomCenterOf(surface)) < 14 * 14) {
                continue;
            }
            boolean closeToExisting = false;
            for (FragmentoErranteEntity fragment : existing) {
                if (fragment.position().distanceToSqr(Vec3.atBottomCenterOf(surface))
                        < MIN_SPACING_SQR) {
                    closeToExisting = true;
                    break;
                }
            }
            if (closeToExisting) continue;

            // Use vanilla EntityType.spawn: finalization and event hooks are
            // preserved; no direct entity constructor or teleport.
            if (ModEntities.FRAGMENTO_ERRANTE.get().spawn(
                    level, surface, EntitySpawnReason.NATURAL) == null) continue;
            spawned++;
            break;
        }
    }
}
