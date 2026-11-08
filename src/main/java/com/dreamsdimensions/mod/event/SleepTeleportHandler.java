package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.registry.ModAttachments;
import com.dreamsdimensions.mod.util.DreamSpawnHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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
            player.sendSystemMessage(Component.literal("Dreams Dimensions: nenhuma dimensão dos sonhos está disponível."));
            return false;
        }

        // Preferimos uma superfície natural. Em áreas sem terreno acessível, a
        // rotina de chegada prepara uma plataforma mínima com blocos da dimensão.
        Collections.shuffle(targets);
        for (ServerLevel target : targets) {
            BlockPos spawnOrigin = target.getLevelData().getRespawnData().pos();
            Optional<Vec3> spawn = DreamSpawnHelper.findOrCreateSafeArrival(target, spawnOrigin, player);
            if (spawn.isEmpty()) {
                DreamsDimensions.LOGGER.warn("Não foi possível encontrar ou preparar chegada segura em {} próximo de {}.",
                        target.dimension(), spawnOrigin);
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

        player.sendSystemMessage(Component.literal(
                "Dreams Dimensions: não encontrei um local seguro nas dimensões dos sonhos. Consulte latest.log."));
        return false;
    }

}
