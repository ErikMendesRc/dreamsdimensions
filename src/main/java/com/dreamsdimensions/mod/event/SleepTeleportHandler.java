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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Handler de teleporte ao dormir, executado no lado servidor.
 * <p>
 * Este listener é registrado no {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS} e usa
 * {@link PlayerTickEvent.Post} para contar ticks de sono antes de teleportar o jogador.
 * </p>
 */
public final class SleepTeleportHandler {
    private static final List<ResourceKey<Level>> DREAM_DIMENSIONS = List.of(
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(DreamsDimensions.MODID, "dreamscape")),
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(DreamsDimensions.MODID, "campo_onirico_azul"))
    );
    private static final Logger LOGGER = DreamsDimensions.LOGGER;
    private static final int TICKS_BEFORE_TELEPORT = 100;
    private static final Map<ServerPlayer, Integer> SLEEPING_PLAYERS = new HashMap<>();
    private static final Random RANDOM = new Random();

    private SleepTeleportHandler() {
    }

    /**
     * Conta ticks enquanto o jogador está dormindo no Overworld e executa o teleporte após o limite.
     */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.isSleeping() && player.serverLevel().dimension() == Level.OVERWORLD) {
            int t = SLEEPING_PLAYERS.merge(player, 1, Integer::sum);
            LOGGER.debug("sleepingPlayers tick #{} para {}", t, player.getGameProfile().getName());
            if (t >= TICKS_BEFORE_TELEPORT) {
                LOGGER.info("Chegou em {} ticks de sono para {}, teleportando para uma dimensão dos sonhos",
                        TICKS_BEFORE_TELEPORT, player.getGameProfile().getName());
                actuallyTeleport(player);
                SLEEPING_PLAYERS.remove(player);
            }
        } else {
            if (SLEEPING_PLAYERS.containsKey(player)) {
                LOGGER.debug("Reset do contador de sono para {} (acordou ou mudou de dimensão)",
                        player.getGameProfile().getName());
            }
            SLEEPING_PLAYERS.remove(player);
        }
    }

    /**
     * Realiza o teleporte efetivo para uma dimensão de sonho aleatória.
     */
    public static void actuallyTeleport(ServerPlayer player) {
        LOGGER.info("actuallyTeleport(): iniciando teleporte de {}", player.getGameProfile().getName());
        MinecraftServer server = player.server;
        
        ResourceKey<Level> targetDimensionKey = DREAM_DIMENSIONS.get(RANDOM.nextInt(DREAM_DIMENSIONS.size()));
        ServerLevel targetLevel = server.getLevel(targetDimensionKey);

        if (targetLevel == null) {
            LOGGER.error("Dimensão {} não encontrada para {}",
                    targetDimensionKey.location(), player.getGameProfile().getName());
            return;
        }

        BlockPos spawn = findSafeSpawnLocation(targetLevel, targetLevel.getSharedSpawnPos());
        LOGGER.info("findSafeSpawnLocation resultou em {}", spawn);

        LOGGER.info("stopSleeping() para {}", player.getGameProfile().getName());
        player.stopSleeping();

        LOGGER.info("teleportTo() para {} em {}", targetDimensionKey.location(), spawn);
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
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        if (y <= level.getMinY()) {
            LOGGER.warn("findSafeSpawnLocation: fallback para Y=150 pois getHeight retornou {}", y);
            y = 150;
        }
        return new BlockPos(origin.getX(), y, origin.getZ());
    }
}
