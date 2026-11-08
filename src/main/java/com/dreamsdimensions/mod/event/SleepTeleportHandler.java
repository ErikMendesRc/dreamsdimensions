package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * O servidor controla o acesso às dimensões oníricas após 100 ticks de sono contínuo.
 *
 * Nunca mantém ServerPlayer em um mapa estático: o contador é indexado pelo UUID e
 * descartado ao acordar, trocar de dimensão ou desconectar.
 */
public final class SleepTeleportHandler {
    public static final int TICKS_BEFORE_TELEPORT = 100;
    private static final Map<UUID, Integer> SLEEP_TICKS = new HashMap<>();
    private static final int SEARCH_RADIUS = 4;

    private SleepTeleportHandler() {}

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        UUID uuid = player.getUUID();
        if (!player.isAlive() || player.level().dimension() != Level.OVERWORLD || !player.isSleeping()) {
            SLEEP_TICKS.remove(uuid);
            return;
        }

        int ticks = SLEEP_TICKS.merge(uuid, 1, Integer::sum);
        if (ticks < TICKS_BEFORE_TELEPORT) {
            return;
        }

        // Remove antes do teleporte: um segundo tick não pode disparar outro teleporte.
        SLEEP_TICKS.remove(uuid);
        actuallyTeleport(player);
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SLEEP_TICKS.remove(event.getEntity().getUUID());
    }

    /**
     * Retorna true apenas quando encontrou uma dimensão carregada e um ponto seguro.
     */
    public static boolean actuallyTeleport(ServerPlayer player) {
        if (!player.isAlive() || player.level().dimension() != Level.OVERWORLD || !player.isSleeping()) {
            return false;
        }

        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        List<ServerLevel> targets = DreamsConfig.getDreamDimensions().stream()
                .map(server::getLevel)
                .filter(level -> level != null && level.dimension() != Level.OVERWORLD)
                .toList();

        if (targets.isEmpty()) {
            DreamsDimensions.LOGGER.warn("Nenhuma dimensão onírica configurada está carregada.");
            return false;
        }

        ServerLevel target = targets.get(ThreadLocalRandom.current().nextInt(targets.size()));
        Optional<Vec3> spawn = findSafeSpawn(target, target.getLevelData().getRespawnData().pos(), player);
        if (spawn.isEmpty()) {
            DreamsDimensions.LOGGER.warn("Nenhum ponto seguro encontrado para {}", target.dimension());
            return false;
        }

        // Captura a cama real deste sono, antes de despertar ou trocar de dimensão.
        player.getSleepingPos().ifPresent(pos -> player.getData(ModAttachments.DREAM_RETURN)
                .setOverworldBed(pos, player.getYRot()));

        player.stopSleeping();
        player.teleport(new TeleportTransition(
                target, spawn.get(), Vec3.ZERO, player.getYRot(), player.getXRot(),
                Set.of(), TeleportTransition.PLACE_PORTAL_TICKET
        ));
        return true;
    }

    private static Optional<Vec3> findSafeSpawn(ServerLevel level, BlockPos origin, ServerPlayer player) {
        // Busca limitada para não carregar uma região inteira do mundo a cada sono.
        for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius > 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }

                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    if (groundY <= level.getMinY() || groundY >= level.getMaxY() - 2) {
                        continue;
                    }

                    BlockPos pos = new BlockPos(x, groundY, z);
                    Vec3 safe = DismountHelper.findSafeDismountLocation(player.getType(), level, pos, false);
                    if (safe != null) {
                        return Optional.of(safe);
                    }
                }
            }
        }
        return Optional.empty();
    }
}
