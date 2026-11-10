package com.dreamsdimensions.mod.entity.spawn;

import com.dreamsdimensions.mod.DreamsDimensions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * One shared, server-only population coordinator. Profiles specify species
 * policy; no per-mob tick listener and no chunk generation or block writes.
 */
public final class DreamMobPopulationService {
    private static final List<MobPopulationProfile<? extends Mob>> PROFILES = new ArrayList<>();
    private static final int REGION_SHIFT = 7; // 128-block cells
    private static final int MAX_REGION_KEYS = 256;
    // Opt-in diagnostics only. No persistent tags or second spawn system.
    private static final boolean AUDIT_SPAWNS = Boolean.getBoolean("dreamsdimensions.auditMobSpawn");
    private static final Map<ServerLevel, LinkedHashMap<RegionKey, Long>> RECENT = new WeakHashMap<>();

    private DreamMobPopulationService() {}

    public static void register(MobPopulationProfile<? extends Mob> profile) {
        for (MobPopulationProfile<? extends Mob> existing : PROFILES) {
            if (existing.id().equals(profile.id())) {
                throw new IllegalStateException("Duplicate dream mob population profile: " + profile.id());
            }
        }
        PROFILES.add(profile);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)
                || player.isSpectator() || !player.isAlive()
                || !level.getGameRules().get(GameRules.SPAWN_MOBS)) return;

        for (MobPopulationProfile<? extends Mob> profile : PROFILES) {
            if (level.dimension() != profile.dimension()
                    || player.getY() < profile.minPlayerY()
                    || player.getY() > profile.maxPlayerY()
                    || (player.tickCount + Math.floorMod(player.getUUID().hashCode(), profile.periodTicks()))
                            % profile.periodTicks() != 0) continue;
            if (!reserveRegion(level, player, profile)) continue;
            replenish(level, player, profile);
        }
    }

    /**
     * Prevent two nearby players from executing the same 48-column sweep
     * during the same evaluation interval. Bounded per-level memo, never
     * persistent world state and never a reference to a player/entity.
     */
    private static boolean reserveRegion(ServerLevel level, ServerPlayer player,
            MobPopulationProfile<? extends Mob> profile) {
        LinkedHashMap<RegionKey, Long> regions = RECENT.computeIfAbsent(level,
                ignored -> new LinkedHashMap<>());
        RegionKey key = new RegionKey(profile.id(),
                player.blockPosition().getX() >> REGION_SHIFT,
                player.blockPosition().getZ() >> REGION_SHIFT);
        long now = level.getGameTime();
        Long previous = regions.get(key);
        if (previous != null && now >= previous && now - previous < profile.periodTicks()) return false;
        regions.put(key, now);
        if (regions.size() > MAX_REGION_KEYS) {
            regions.remove(regions.keySet().iterator().next());
        }
        return true;
    }

    private static <T extends Mob> void replenish(ServerLevel level, ServerPlayer player,
            MobPopulationProfile<T> profile) {
        Vec3 origin = player.position();
        AABB surrounding = new AABB(player.getX() - profile.countRadius(), profile.minSpawnY(),
                player.getZ() - profile.countRadius(), player.getX() + profile.countRadius(),
                profile.maxSpawnY() + 10, player.getZ() + profile.countRadius());
        List<T> existing = level.getEntitiesOfClass(
                profile.mobClass(), surrounding, Mob::isAlive);
        if (existing.size() >= profile.localCap()) return;

        RandomSource random = level.getRandom();
        int spawned = 0;
        List<Vec3> newPositions = new ArrayList<>();
        for (int attempt = 0; attempt < profile.candidateLimit()
                && spawned < profile.maxPerPass()
                && existing.size() + spawned < profile.localCap(); attempt++) {
            BlockPos surface = profile.candidateSelection().select(level, player, random, profile);
            if (surface == null || !level.hasChunkAt(surface)) continue;
            int y = surface.getY();
            Vec3 target = Vec3.atBottomCenterOf(surface);
            if (!profile.habitat().isValid(level, surface)
                    || Math.abs(y - origin.y) > profile.maxVerticalDistance()
                    || origin.distanceToSqr(target)
                            < profile.minimumPlayerDistance() * profile.minimumPlayerDistance()) {
                continue;
            }
            boolean closeToExisting = false;
            for (T mob : existing) {
                if (mob.position().distanceToSqr(target)
                        < profile.minimumMobSpacing() * profile.minimumMobSpacing()) {
                    closeToExisting = true;
                    break;
                }
            }
            if (closeToExisting) continue;
            for (Vec3 newPosition : newPositions) {
                if (newPosition.distanceToSqr(target)
                        < profile.minimumMobSpacing() * profile.minimumMobSpacing()) {
                    closeToExisting = true;
                    break;
                }
            }
            if (closeToExisting) continue;

            // Vanilla entity initialization and NeoForge spawn events are kept.
            T mob = profile.entityType().get().spawn(
                    level, surface, EntitySpawnReason.NATURAL);
            if (mob == null) continue;
            // Distinguishes coordinator contribution from vanilla population
            // during in-game sampling, without changing mob NBT or behavior.
            if (AUDIT_SPAWNS) {
                DreamsDimensions.LOGGER.info(
                        "DREAM_MOB_SPAWN origin=coordinator type={} dimension={} pos={} nearby_before={} local_cap={}",
                        profile.id(), level.dimension().identifier(), mob.blockPosition(),
                        existing.size() + spawned, profile.localCap());
            }
            spawned++;
            newPositions.add(target);
            if (profile.stopAfterFirstSuccess()) break; // Preserve the Fragmento's one-success rule.
        }
    }

    /** Drop per-server transient throttle state after integrated/dedicated shutdown. */
    public static void onServerStopped(ServerStoppedEvent event) {
        RECENT.clear();
    }

    /** Historical heightmap annulus selection, now callable from other species profiles. */
    public static BlockPos annulusHeightmapCandidate(ServerLevel level, ServerPlayer player,
            RandomSource random, MobPopulationProfile<?> profile) {
        Vec3 origin = player.position();
        double angle = random.nextDouble() * Math.PI * 2.0;
        double radius = profile.innerRadius()
                + random.nextDouble() * (profile.outerRadius() - profile.innerRadius());
        int x = (int) Math.floor(origin.x + Math.cos(angle) * radius);
        int z = (int) Math.floor(origin.z + Math.sin(angle) * radius);
        BlockPos chunkProbe = new BlockPos(x, 192, z);
        if (!level.hasChunkAt(chunkProbe)) return null;
        int y = level.getHeight(profile.heightmap(), x, z);
        return new BlockPos(x, y, z);
    }

    private record RegionKey(String profileId, int regionX, int regionZ) {}
}
