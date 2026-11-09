package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.dimension.DreamDimensionDefinition;
import com.dreamsdimensions.mod.dimension.WeightedDimensionPicker;
import com.dreamsdimensions.mod.registry.ModAttachments;
import com.dreamsdimensions.mod.util.DreamSpawnHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
    private DreamTeleportService() {}

    public static boolean enterDream(ServerPlayer player, BlockPos bedPos, float bedYaw) {
        if (!player.isAlive() || player.level().dimension() != Level.OVERWORLD) return false;

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
            DreamsDimensions.LOGGER.warn("Teleporte onírico bloqueado: nenhuma dimensão habilitada está carregada.");
            player.sendSystemMessage(Component.literal(
                    "Dreams Dimensions: nenhuma dimensão dos sonhos está disponível."));
            return false;
        }

        for (DreamDimensionDefinition definition :
                WeightedDimensionPicker.order(available, ThreadLocalRandom.current())) {
            ServerLevel target = server.getLevel(definition.dimension());
            if (target == null) continue; // May unload between eligibility and selection.
            BlockPos spawnOrigin = target.getLevelData().getRespawnData().pos();
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
                if (teleported == null || teleported.level().dimension() != target.dimension()) {
                    DreamsDimensions.LOGGER.warn("Teleporte recusado para jogador {} em {}.",
                            player.getUUID(), definition.dimension().identifier());
                    return false; // Do not blindly retry after a partial transition.
                }

                // Commit return coordinates ONLY after a confirmed transition.
                // Keep dream_return's codec and persisted field names unchanged.
                teleported.getData(ModAttachments.DREAM_RETURN).setOverworldBed(bedPos, bedYaw);
                DreamsDimensions.LOGGER.info("Jogador {} entrou em {} após sono completo.",
                        player.getUUID(), definition.dimension().identifier());
                return true;
            } catch (RuntimeException exception) {
                DreamsDimensions.LOGGER.error("Falha de teleporte para {} em {} (sem tentar outro mundo).",
                        player.getUUID(), definition.dimension().identifier(), exception);
                return false;
            }
        }

        player.sendSystemMessage(Component.literal(
                "Dreams Dimensions: não encontrei um local seguro nas dimensões dos sonhos. Consulte latest.log."));
        return false;
    }
}
