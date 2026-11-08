package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Teleporte onírico: ocorre após os 100 ticks nativos do sono.
 *
 * O Minecraft pode acordar o jogador para avançar a noite antes de um
 * PlayerTickEvent.Post observar o tick 100. Por isso também capturamos
 * PlayerWakeUpEvent (antes do estado de sono ser limpo) e executamos o
 * teleporte no próximo PlayerTickEvent.Post, fora do callback de despertar.
 *
 * Estado transitório indexado por UUID; nunca retém ServerPlayer.
 */
public final class SleepTeleportHandler {
    public static final int TICKS_BEFORE_TELEPORT = 100;
    private static final int SEARCH_RADIUS = 12;
    private static final Set<UUID> ATTEMPTED_SLEEP = new HashSet<>();
    private static final Set<UUID> TELEPORTING = new HashSet<>();
    private static final Map<UUID, BedOrigin> COMPLETED_WAKEUPS = new HashMap<>();

    private record BedOrigin(BlockPos pos, float yaw) {}

    private SleepTeleportHandler() {}

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        UUID uuid = player.getUUID();

        // O callback de despertar vanilla acontece durante um tick. Processar
        // aqui evita teleportar no meio de ServerPlayer.stopSleepInBed().
        BedOrigin completedWakeup = COMPLETED_WAKEUPS.remove(uuid);
        if (completedWakeup != null) {
            ATTEMPTED_SLEEP.remove(uuid);
            if (player.isAlive() && player.level().dimension() == Level.OVERWORLD && !player.isSleeping()) {
                actuallyTeleport(player, completedWakeup);
            }
            return;
        }

        if (!player.isAlive() || player.level().dimension() != Level.OVERWORLD || !player.isSleeping()) {
            ATTEMPTED_SLEEP.remove(uuid);
            return;
        }

        // Usa o contador do Minecraft, em vez de contar eventos Post
        // (que podem não coincidir com o ciclo de despertar vanilla).
        if (player.getSleepTimer() < TICKS_BEFORE_TELEPORT || !ATTEMPTED_SLEEP.add(uuid)) {
            return;
        }

        Optional<BlockPos> bed = player.getSleepingPos();
        if (bed.isEmpty()) {
            DreamsDimensions.LOGGER.warn("Sono completo, mas a cama de {} não está disponível.", player.getUUID());
            return;
        }

        actuallyTeleport(player, new BedOrigin(bed.get(), player.getYRot()));
    }

    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.isAlive()
                || player.level().dimension() != Level.OVERWORLD) {
            return;
        }

        UUID uuid = player.getUUID();
        if (TELEPORTING.contains(uuid) || ATTEMPTED_SLEEP.contains(uuid)
                || player.getSleepTimer() < TICKS_BEFORE_TELEPORT) {
            return;
        }

        // Esse evento é disparado antes de o Minecraft limpar a cama e
        // o tempo de sono. Não interferir em despertar manual antecipado.
        player.getSleepingPos().ifPresent(pos ->
                COMPLETED_WAKEUPS.put(uuid, new BedOrigin(pos, player.getYRot())));
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        ATTEMPTED_SLEEP.remove(uuid);
        TELEPORTING.remove(uuid);
        COMPLETED_WAKEUPS.remove(uuid);
    }

    private static boolean actuallyTeleport(ServerPlayer player, BedOrigin origin) {
        if (!player.isAlive() || player.level().dimension() != Level.OVERWORLD) {
            return false;
        }

        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        List<ServerLevel> targets = new ArrayList<>(DreamsConfig.getDreamDimensions().stream()
                .map(server::getLevel)
                .filter(level -> level != null && level.dimension() != Level.OVERWORLD)
                .toList());

        if (targets.isEmpty()) {
            DreamsDimensions.LOGGER.warn("Teleporte onírico bloqueado: nenhuma dimensão configurada está carregada.");
            player.displayClientMessage(Component.literal("Dreams Dimensions: nenhuma dimensão dos sonhos está disponível."), false);
            return false;
        }

        // Se a primeira dimensão não tiver spawn seguro perto de seu spawn,
        // tentamos a outra sem criar uma plataforma artificial ou arriscar queda.
        Collections.shuffle(targets);
        for (ServerLevel target : targets) {
            BlockPos spawnOrigin = target.getLevelData().getRespawnData().pos();
            Optional<Vec3> spawn = findSafeSpawn(target, spawnOrigin, player);
            if (spawn.isEmpty()) {
                DreamsDimensions.LOGGER.warn("Sem ponto seguro em {} próximo de {} (raio {}).",
                        target.dimension(), spawnOrigin, SEARCH_RADIUS);
                continue;
            }

            UUID uuid = player.getUUID();
            TELEPORTING.add(uuid);
            try {
                // Persistimos a posição real da cama antes do despertar.
                player.getData(ModAttachments.DREAM_RETURN)
                        .setOverworldBed(origin.pos(), origin.yaw());

                if (player.isSleeping()) {
                    player.stopSleeping();
                }

                ServerPlayer teleported = player.teleport(new TeleportTransition(
                        target, spawn.get(), Vec3.ZERO, player.getYRot(), player.getXRot(),
                        Set.of(), TeleportTransition.PLACE_PORTAL_TICKET
                ));

                if (teleported != null && teleported.level().dimension() == target.dimension()) {
                    DreamsDimensions.LOGGER.info("Jogador {} entrou em {} após sono completo.",
                            uuid, target.dimension());
                    return true;
                }

                DreamsDimensions.LOGGER.warn("Teleporte onírico foi recusado para {} ({})", uuid, target.dimension());
                return false;
            } finally {
                TELEPORTING.remove(uuid);
            }
        }

        player.displayClientMessage(Component.literal(
                "Dreams Dimensions: não encontrei um local seguro nas dimensões dos sonhos. Consulte latest.log."), false);
        return false;
    }

    private static Optional<Vec3> findSafeSpawn(ServerLevel level, BlockPos origin, ServerPlayer player) {
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

                    Vec3 safe = DismountHelper.findSafeDismountLocation(
                            player.getType(), level, new BlockPos(x, groundY, z), false);
                    if (safe != null) {
                        return Optional.of(safe);
                    }
                }
            }
        }
        return Optional.empty();
    }
}
