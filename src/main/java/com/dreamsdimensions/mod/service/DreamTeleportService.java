package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.qa.DreamQaRecorder;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.dimension.DreamDimensionDefinition;
import com.dreamsdimensions.mod.dimension.WeightedDimensionPicker;
import com.dreamsdimensions.mod.registry.ModAttachments;
import com.dreamsdimensions.mod.util.DreamSpawnHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side orchestration: enabled+loaded selection, safe arrival,
 * teleport and successful-bed persistence. The sleep event owns no worldgen.
 */
public final class DreamTeleportService {
    // Ephemeral party rendezvous. Never stores persisted player data, never ticks
    // or generates chunks. The existing safe-spawn helper validates each arrival.
    private record GroupArrival(UUID owner, BlockPos bed, ResourceKey<Level> dimension,
            BlockPos destination, long enteredAt) {}
    private static final Map<UUID, GroupArrival> GROUP_ARRIVALS = new HashMap<>();

    private static Optional<GroupArrival> companion(ServerPlayer player, BlockPos bed) {
        long now = player.level().getGameTime();
        GROUP_ARRIVALS.entrySet().removeIf(row ->
                now - row.getValue().enteredAt() > DreamRecoveryPolicy.GROUP_WINDOW_TICKS
                        || now < row.getValue().enteredAt());
        return GROUP_ARRIVALS.values().stream()
                .filter(visit -> !visit.owner().equals(player.getUUID()))
                .filter(visit -> DreamRecoveryPolicy.canGroup(
                        visit.bed().getX() - bed.getX(),
                        visit.bed().getY() - bed.getY(),
                        visit.bed().getZ() - bed.getZ(),
                        now - visit.enteredAt()))
                .min((a, b) -> Double.compare(
                        a.bed().distSqr(bed), b.bed().distSqr(bed)));
    }

    public static void onServerStopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
        GROUP_ARRIVALS.clear();
    }

    private DreamTeleportService() {}

    private static void qa(String name, String dimension, String status, String reason) {
        DreamQaRecorder.global().record("TELEPORT", "dream_entry",
                name, dimension, status, reason);
    }

    public static boolean enterDream(ServerPlayer player, BlockPos bedPos, float bedYaw) {
        if (!player.isAlive() || player.level().dimension() != Level.OVERWORLD) {
            qa("DREAM_ENTRY_REJECTED", "minecraft:overworld", "SKIPPED", "INVALID_SOURCE");
            return false;
        }
        qa("DREAM_ENTRY_REQUESTED", "minecraft:overworld", "PASS", "ELIGIBLE_REQUEST");

        MinecraftServer server = ((ServerLevel) player.level()).getServer();
        List<WeightedDimensionPicker.Candidate<DreamDimensionDefinition>> available = new ArrayList<>();
        for (DreamDimensionDefinition definition : DreamsConfig.getEnabledDefinitions()) {
            ServerLevel target = server.getLevel(definition.dimension());
            if (target == null) {
                DreamsDimensions.LOGGER.warn("Dimensão habilitada, porém não carregada: {}",
                        definition.dimension().identifier());
                continue;
            }
            available.add(new WeightedDimensionPicker.Candidate<>(definition, definition.selectionWeight()));
        }

        if (available.isEmpty()) {
            qa("DREAM_ENTRY_REJECTED", "minecraft:overworld", "FAIL", "DIMENSION_UNAVAILABLE");
            DreamsDimensions.LOGGER.warn("Teleporte onírico bloqueado: nenhuma dimensão habilitada está carregada.");
            player.sendSystemMessage(Component.literal(
                    "Dreams Dimensions: nenhuma dimensão dos sonhos está disponível."));
            return false;
        }

        Optional<GroupArrival> nearby = companion(player, bedPos);
        List<DreamDimensionDefinition> ordered = WeightedDimensionPicker
                .order(available, ThreadLocalRandom.current());
        if (nearby.isPresent()) {
            ResourceKey<Level> shared = nearby.get().dimension();
            // Preserve group destination or fail safely; never quietly choose
            // a different world from the companion.
            ordered = ordered.stream().filter(def -> def.dimension() == shared).toList();
            if (ordered.isEmpty()) {
                qa("DREAM_ENTRY_REJECTED", "minecraft:overworld", "FAIL",
                        "GROUP_DESTINATION_UNAVAILABLE");
                return false;
            }
        }
        for (DreamDimensionDefinition definition : ordered) {
            ServerLevel target = server.getLevel(definition.dimension());
            if (target == null) continue; // May unload between eligibility and selection.
            BlockPos spawnOrigin = nearby.isPresent()
                    ? nearby.get().destination()
                    : target.getLevelData().getRespawnData().pos();
            var spawn = DreamSpawnHelper.findOrCreateSafeArrival(target, definition, spawnOrigin, player);
            if (spawn.isEmpty()) {
                DreamsDimensions.LOGGER.warn("Sem chegada segura em {} perto de {}.",
                        definition.dimension().identifier(), spawnOrigin);
                continue;
            }

            try {
                if (player.isSleeping()) player.stopSleeping();
                ServerPlayer teleported = player.teleport(new TeleportTransition(
                        target, spawn.get(), Vec3.ZERO, player.getYRot(), player.getXRot(),
                        Set.of(), TeleportTransition.PLACE_PORTAL_TICKET
                ));
                if (teleported == null || !player.getUUID().equals(teleported.getUUID())
                        || teleported.level().dimension() != target.dimension()) {
                    DreamsDimensions.LOGGER.warn("Teleporte recusado para jogador {} em {}.",
                            player.getUUID(), definition.dimension().identifier());
                    qa("DREAM_ENTRY_REJECTED", target.dimension().identifier().toString(),
                            "FAIL", "TELEPORT_UNCONFIRMED");
                    return false; // Do not blindly retry after a partial transition.
                }

                // Commit return coordinates ONLY after a confirmed transition.
                // Keep dream_return's codec and persisted field names unchanged.
                teleported.getData(ModAttachments.DREAM_RETURN).setOverworldBed(bedPos, bedYaw);
                // Task 5.5: a new expedition starts only AFTER a confirmed
                // sleep journey, never during a failed roll or admin /tp.
                DreamRespawnService.recordExpedition(teleported, spawn.get());
                GROUP_ARRIVALS.put(player.getUUID(), new GroupArrival(
                        player.getUUID(), bedPos, target.dimension(),
                        BlockPos.containing(spawn.get()), server.overworld().getGameTime()));
                qa("DREAM_ENTRY_COMPLETED", target.dimension().identifier().toString(),
                        "PASS", "CONFIRMED");
                DreamsDimensions.LOGGER.info("Jogador {} entrou em {} após sono completo.",
                        player.getUUID(), definition.dimension().identifier());
                return true;
            } catch (RuntimeException exception) {
                DreamsDimensions.LOGGER.error("Falha de teleporte para {} em {} (sem tentar outro mundo).",
                        player.getUUID(), definition.dimension().identifier(), exception);
                qa("DREAM_ENTRY_REJECTED", target.dimension().identifier().toString(),
                        "FAIL", "TELEPORT_EXCEPTION");
                return false;
            }
        }

        qa("DREAM_ENTRY_REJECTED", "minecraft:overworld", "FAIL", "NO_SAFE_DESTINATION");
        player.sendSystemMessage(Component.literal(
                "Dreams Dimensions: não encontrei um local seguro nas dimensões dos sonhos. Consulte latest.log."));
        return false;
    }
}
