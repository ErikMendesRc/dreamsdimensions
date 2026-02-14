package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.attachment.DreamReturnData;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.registry.ModAttachments;
import com.dreamsdimensions.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SleepTeleportHandler {

    private static final ResourceKey<Level> FALLBACK_DREAM_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "dreamscape")
    );

    private static final Logger LOGGER = DreamsDimensions.LOGGER;

    private static final Set<UUID> TELEPORTED_THIS_SLEEP = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> BLOCKED_SLEEP_TELEPORT_THIS_CYCLE = ConcurrentHashMap.newKeySet();

    private SleepTeleportHandler() {}

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            TELEPORTED_THIS_SLEEP.remove(player.getUUID());
            BLOCKED_SLEEP_TELEPORT_THIS_CYCLE.remove(player.getUUID());
            return;
        }

        if (serverLevel.dimension() != Level.OVERWORLD) {
            TELEPORTED_THIS_SLEEP.remove(player.getUUID());
            BLOCKED_SLEEP_TELEPORT_THIS_CYCLE.remove(player.getUUID());
            return;
        }

        if (!player.isSleeping()) {
            TELEPORTED_THIS_SLEEP.remove(player.getUUID());
            BLOCKED_SLEEP_TELEPORT_THIS_CYCLE.remove(player.getUUID());
            return;
        }

        if (!player.isSleepingLongEnough()) {
            return;
        }

        if (isUsingAwakener(player)) {
            if (BLOCKED_SLEEP_TELEPORT_THIS_CYCLE.add(player.getUUID())) {
                LOGGER.info("[SleepTeleport][BLOCKED] player={} reason=using_awakener remainingTicks={}", player.getGameProfile().getName(), player.getUseItemRemainingTicks());
            }
            return;
        }

        if (!TELEPORTED_THIS_SLEEP.add(player.getUUID())) {
            return;
        }

        LOGGER.info("[SleepTeleport][READY] player={} slept>=100 starting teleport", player.getName().getString());
        actuallyTeleport(player);
    }

    private static boolean isUsingAwakener(ServerPlayer player) {
        return player.isUsingItem() && player.getUseItem().is(ModItems.ONEIRIC_AWAKENER.get());
    }

    public static ResourceKey<Level> pickDreamDimension(ServerPlayer player, MinecraftServer server) {
        DreamsConfig.logResolvedDreamDimensions(server);

        List<ResourceKey<Level>> candidates = DreamsConfig.getDreamDimensions().stream()
                .filter(key -> server.getLevel(key) != null)
                .sorted((a, b) -> a.identifier().toString().compareTo(b.identifier().toString()))
                .toList();

        LOGGER.info("[SleepTeleport][PICK] player={} candidates={} size={}",
                player.getGameProfile().getName(),
                candidates.stream().map(key -> key.identifier().toString()).toList(),
                candidates.size()
        );

        if (candidates.isEmpty()) {
            LOGGER.error("[SleepTeleport][PICK] No configured dream dimension exists on server. fallback={}", FALLBACK_DREAM_DIMENSION.identifier());
            return FALLBACK_DREAM_DIMENSION;
        }

        if (candidates.size() == 1) {
            return candidates.getFirst();
        }

        DreamReturnData data = player.getData(ModAttachments.DREAM_RETURN);
        ResourceKey<Level> last = data.getLastDreamDimension();

        List<ResourceKey<Level>> withoutLast = new ArrayList<>(candidates);
        if (last != null) {
            withoutLast.remove(last);
        }

        List<ResourceKey<Level>> pool = withoutLast.isEmpty() ? candidates : withoutLast;

        RandomSource rng = server.overworld().getRandom();
        int chosenIndex = rng.nextInt(pool.size());
        ResourceKey<Level> chosen = pool.get(chosenIndex);

        LOGGER.info("[SleepTeleport][PICK] player={} lastDream={} pool={} poolSize={} chosenIndex={} chosen={}",
                player.getGameProfile().getName(),
                last == null ? "<none>" : last.identifier(),
                pool.stream().map(key -> key.identifier().toString()).toList(),
                pool.size(),
                chosenIndex,
                chosen.identifier()
        );

        data.setLastDreamDimension(chosen);
        return chosen;
    }

    public static void actuallyTeleport(ServerPlayer player) {
        ServerLevel currentLevel = (ServerLevel) player.level();
        MinecraftServer server = currentLevel.getServer();

        ResourceKey<Level> targetDimensionKey = pickDreamDimension(player, server);

        ServerLevel targetLevel = server.getLevel(targetDimensionKey);
        if (targetLevel == null) {
            LOGGER.error("[SleepTeleport] Target dimension {} not found for {}",
                    targetDimensionKey.identifier(),
                    player.getName().getString());
            return;
        }

        player.stopSleeping();

        LevelData.RespawnData respawn = targetLevel.getLevelData().getRespawnData();
        BlockPos worldSpawn = respawn.pos();

        BlockPos spawn = findSafeSpawnLocation(targetLevel, worldSpawn);

        LOGGER.info("[SleepTeleport] player={} from={} to={} spawn=({}, {}, {})",
                player.getGameProfile().getName(),
                currentLevel.dimension().identifier(),
                targetDimensionKey.identifier(),
                spawn.getX(),
                spawn.getY(),
                spawn.getZ()
        );

        player.teleportTo(
                targetLevel,
                spawn.getX() + 0.5,
                spawn.getY(),
                spawn.getZ() + 0.5,
                Set.of(),
                player.getYRot(),
                player.getXRot(),
                true
        );
    }

    private static BlockPos findSafeSpawnLocation(ServerLevel level, BlockPos origin) {
        level.getChunkAt(origin);

        int y = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                origin.getX(),
                origin.getZ()
        );

        if (y <= level.getMinY()) {
            y = 150;
        }

        return new BlockPos(origin.getX(), y, origin.getZ());
    }
}
