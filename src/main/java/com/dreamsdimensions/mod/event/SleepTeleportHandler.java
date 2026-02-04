package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SleepTeleportHandler {

    private static final List<ResourceKey<Level>> DREAM_DIMENSIONS = List.of(
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(DreamsDimensions.MODID, "dreamscape")),
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(DreamsDimensions.MODID, "campo_onirico_azul"))
    );

    private static final Logger LOGGER = DreamsDimensions.LOGGER;

    /**
     * Guarda quem já foi teleportado "neste ciclo de sono".
     * Assim: quando atingir 100 ticks, teleportamos 1x e não repetimos todo tick.
     */
    private static final Set<UUID> TELEPORTED_THIS_SLEEP = ConcurrentHashMap.newKeySet();

    private SleepTeleportHandler() {}

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Garantir que estamos no servidor
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            TELEPORTED_THIS_SLEEP.remove(player.getUUID());
            return;
        }

        // Só no Overworld
        if (serverLevel.dimension() != Level.OVERWORLD) {
            TELEPORTED_THIS_SLEEP.remove(player.getUUID());
            return;
        }

        // Se não está dormindo, reseta o "ciclo"
        if (!player.isSleeping()) {
            TELEPORTED_THIS_SLEEP.remove(player.getUUID());
            return;
        }

        // Timer vanilla (>= 100 ticks de sono)
        if (!player.isSleepingLongEnough()) {
            return;
        }

        // Garante 1x por ciclo de sono
        if (!TELEPORTED_THIS_SLEEP.add(player.getUUID())) {
            return;
        }

        LOGGER.info("Dormiu >= 100 ticks: teleportando {} para dimensão dos sonhos",
                player.getName().getString());

        actuallyTeleport(player);
    }

    public static void actuallyTeleport(ServerPlayer player) {
        ServerLevel currentLevel = (ServerLevel) player.level();
        MinecraftServer server = currentLevel.getServer();

        ResourceKey<Level> targetDimensionKey =
                DREAM_DIMENSIONS.get(player.getRandom().nextInt(DREAM_DIMENSIONS.size()));

        ServerLevel targetLevel = server.getLevel(targetDimensionKey);
        if (targetLevel == null) {
            LOGGER.error("Dimensão {} não encontrada para {}",
                    targetDimensionKey.location(),
                    player.getName().getString());
            return;
        }

        // Atualiza estado de sono corretamente
        player.stopSleeping();

        // 1.21.10+: spawn/respawn vem do LevelData.RespawnData
        LevelData.RespawnData respawn = targetLevel.getLevelData().getRespawnData();
        BlockPos worldSpawn = respawn.pos();

        BlockPos spawn = findSafeSpawnLocation(targetLevel, worldSpawn);

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
            y = 150; // fallback
        }

        return new BlockPos(origin.getX(), y, origin.getZ());
    }
}