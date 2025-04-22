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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SleepTeleportHandler {

    private static final ResourceKey<Level> DREAMSCAPE_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(DreamsDimensions.MODID, "dreamscape")
    );
    private static final Logger LOGGER = DreamsDimensions.LOGGER;

    private static final int TICKS_BEFORE_TELEPORT = 100;
    private static final Map<ServerPlayer, Integer> sleepingPlayers = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.isSleeping() && player.serverLevel().dimension() == Level.OVERWORLD) {
            int t = sleepingPlayers.merge(player, 1, Integer::sum);
            LOGGER.debug("sleepingPlayers tick #{} para {}", t, player.getGameProfile().getName());
            if (t >= TICKS_BEFORE_TELEPORT) {
                LOGGER.info("Chegou em {} ticks de sono para {}, teleportando para Dreamscape",
                        TICKS_BEFORE_TELEPORT, player.getGameProfile().getName());
                actuallyTeleport(player);
                sleepingPlayers.remove(player);
            }
        } else {
            if (sleepingPlayers.containsKey(player)) {
                LOGGER.debug("Reset do contador de sono para {} (acordou ou mudou de dimensão)",
                        player.getGameProfile().getName());
            }
            sleepingPlayers.remove(player);
        }
    }

    public static void actuallyTeleport(ServerPlayer player) {
        LOGGER.info("actuallyTeleport(): iniciando teleporte de {}", player.getGameProfile().getName());
        MinecraftServer server = player.server;
        ServerLevel targetLevel = server.getLevel(DREAMSCAPE_KEY);

        if (targetLevel == null) {
            LOGGER.error("Dimensão {} não encontrada para {}",
                    DREAMSCAPE_KEY.location(), player.getGameProfile().getName());
            return;
        }

        BlockPos spawn = findSafeSpawnLocation(targetLevel, targetLevel.getSharedSpawnPos());
        LOGGER.info("findSafeSpawnLocation resultou em {}", spawn);

        LOGGER.info("stopSleeping() para {}", player.getGameProfile().getName());
        player.stopSleeping();

        LOGGER.info("teleportTo() para Dreamscape em {}", spawn);
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