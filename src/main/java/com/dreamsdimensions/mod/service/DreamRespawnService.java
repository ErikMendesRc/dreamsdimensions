package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.attachment.DreamExpeditionAnchorData;
import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.dimension.DreamDimensionDefinition;
import com.dreamsdimensions.mod.registry.ModAttachments;
import com.dreamsdimensions.mod.util.DreamSpawnHelper;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The respawn position is changed BEFORE PlayerList constructs the new player.
 * Inventory, XP, Curios and vanilla gamerules remain owned by PlayerList.
 *
 * No second teleport, no death cancellation and no in-memory per-player cache.
 */
public final class DreamRespawnService {
    private static final int ANCHOR_SEARCH_RADIUS = 8;
    private static final int DEATH_SEARCH_RADIUS = 8;

    private DreamRespawnService() {}

    /** Called after confirmed sleep-to-dream teleport (not for admin teleports). */
    public static void recordExpedition(ServerPlayer player, Vec3 position) {
        ResourceKey<Level> dimension = player.level().dimension();
        if (DreamDimensionCatalog.find(dimension).isEmpty()) return;

        ServerLevel level = (ServerLevel) player.level();
        BlockPos feet = BlockPos.containing(position);
        if (!DreamSpawnHelper.isSafeStandingSpace(level, feet, player)) {
            DreamsDimensions.LOGGER.error("DREAM_RESPAWN player_uuid={} source={} result=unsafe_arrival_no_anchor pos={}",
                    player.getUUID(), dimension.identifier(), feet);
            return;
        }

        // setData triggers persistence and the 26.3 attachment copyOnDeath path.
        DreamExpeditionAnchorData anchor = new DreamExpeditionAnchorData(dimension, feet, player.getYRot());
        player.setData(ModAttachments.DREAM_EXPEDITION_ANCHOR, anchor);
        DreamsDimensions.LOGGER.info("DREAM_RESPAWN player_uuid={} dimension={} result=anchor_recorded pos={}",
                player.getUUID(), dimension.identifier(), feet);
    }

    /** Called only after Oneiric Awakener has CONFIRMED its paid return. */
    public static void completeExpedition(ServerPlayer player) {
        if (player.level().dimension() != Level.OVERWORLD) return;
        if (player.hasData(ModAttachments.DREAM_EXPEDITION_ANCHOR)) {
            player.removeData(ModAttachments.DREAM_EXPEDITION_ANCHOR);
            DreamsDimensions.LOGGER.info("DREAM_RESPAWN player_uuid={} result=anchor_cleared reason=awakener",
                    player.getUUID());
        }
    }

    /**
     * NeoForge fires this before the new ServerPlayer exists, after vanilla
     * resolves its own bed/respawn. Override ONLY deaths in catalog dreams;
     * never a Nether/End/other-mod/Overworld respawn, and never Hardcore.
     */
    public static void onRespawnPosition(PlayerRespawnPositionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer old)) return;
        ResourceKey<Level> source = old.level().dimension();

        Optional<DreamDimensionDefinition> definition = DreamDimensionCatalog.find(source);
        if (!DreamRespawnPolicy.interceptDeath(definition.isPresent(),
                event.isFromEndFight(), old.level().getLevelData().isHardcore())) return;

        ServerLevel dream = ((ServerLevel) old.level()).getServer().getLevel(source);
        if (dream == null) {
            DreamsDimensions.LOGGER.error("DREAM_RESPAWN player_uuid={} source={} result=missing_dream_level "
                            + "fallback=vanilla_technical_emergency",
                    old.getUUID(), source.identifier());
            return; // Exceptional corrupt/unloaded dimension: no fabricated coordinates.
        }

        try {
            Resolution resolution = resolve(dream, old, definition.orElseThrow());
            if (resolution == null) {
                DreamsDimensions.LOGGER.error("DREAM_RESPAWN player_uuid={} source={} result=no_safe_location "
                                + "fallback=vanilla_technical_emergency",
                        old.getUUID(), source.identifier());
                return;
            }

            // Unlike setting only the respawn level, this transition includes
            // coordinates validated in that SAME dimension.
            event.setTeleportTransition(new TeleportTransition(
                    dream, resolution.position(), Vec3.ZERO, resolution.yaw(), old.getXRot(),
                    Set.of(), TeleportTransition.DO_NOTHING
            ));
            // Preserve vanilla personal respawn (especially an Overworld bed).
            // Do not force setCopyOriginalSpawnPosition(true) if vanilla found
            // the saved bed broken/missing; leave original event semantics.
            DreamsDimensions.LOGGER.info(
                    "DREAM_RESPAWN player_uuid={} source={} target={} mode={} result=destination_selected pos={}",
                    old.getUUID(), source.identifier(), dream.dimension().identifier(),
                    resolution.selection(), BlockPos.containing(resolution.position()));
        } catch (RuntimeException exception) {
            // No unsafe guess: the original event destination remains intact.
            DreamsDimensions.LOGGER.error(
                    "DREAM_RESPAWN player_uuid={} source={} result=resolution_exception "
                            + "fallback=vanilla_technical_emergency",
                    old.getUUID(), source.identifier(), exception);
        }
    }

    /**
     * Read-only checks first; the existing bounded 5x5 platform builder is
     * available ONLY as a final emergency around the level's global spawn.
     * Reexisting platforms are naturally reused by the surface search.
     */
    private static Resolution resolve(ServerLevel dream, ServerPlayer player,
            DreamDimensionDefinition definition) {
        var dimension = dream.dimension();
        DreamExpeditionAnchorData anchor = player.hasData(ModAttachments.DREAM_EXPEDITION_ANCHOR)
                ? player.getData(ModAttachments.DREAM_EXPEDITION_ANCHOR)
                : null;
        boolean matches = anchor != null && anchor.isActiveFor(dimension);

        Optional<Vec3> anchorPoint = Optional.empty();
        Optional<Vec3> nearAnchor = Optional.empty();
        if (matches) {
            BlockPos feet = anchor.pos();
            // A saved expedition point belongs to previously generated terrain:
            // load its one chunk if necessary; then revalidate its real floor.
            if (dream.isInsideBuildHeight(feet) && dream.getWorldBorder().isWithinBounds(feet)) {
                dream.getChunkAt(feet);
                if (DreamSpawnHelper.isSafeStandingSpace(dream, feet, player)) {
                    anchorPoint = Optional.of(Vec3.atBottomCenterOf(feet));
                } else {
                    nearAnchor = DreamSpawnHelper.findNaturalArrival(
                            dream, feet, player, ANCHOR_SEARCH_RADIUS);
                }
            } // Corrupt/out-of-border anchor: skip nearby probing altogether.
        }

        Optional<Vec3> nearDeath = Optional.empty();
        if (anchorPoint.isEmpty() && nearAnchor.isEmpty()) {
            // Admin-driven entry may have no expedition attachment. Its death
            // must still resolve locally without claiming a legitimate trip.
            BlockPos deathXZ = player.blockPosition();
            nearDeath = DreamSpawnHelper.findNaturalArrival(
                    dream, deathXZ, player, DEATH_SEARCH_RADIUS);
        }

        Optional<Vec3> worldSpawn = Optional.empty();
        Optional<Vec3> controlledFallback = Optional.empty();
        BlockPos worldOrigin = dream.getLevelData().getRespawnData().pos();
        if (anchorPoint.isEmpty() && nearAnchor.isEmpty() && nearDeath.isEmpty()) {
            worldSpawn = DreamSpawnHelper.findNaturalArrival(
                    dream, worldOrigin, player, definition.naturalSpawnRadius());
        }
        if (anchorPoint.isEmpty() && nearAnchor.isEmpty() && nearDeath.isEmpty()
                && worldSpawn.isEmpty()) {
            // Same bounded and air-only platform algorithm already used by
            // legitimate arrival. Never construct a platform at death's void.
            controlledFallback = DreamSpawnHelper.findOrCreateSafeArrival(
                    dream, definition, worldOrigin, player);
        }

        DreamRespawnPolicy.Selection mode = DreamRespawnPolicy.choose(
                matches, anchorPoint.isPresent(), nearAnchor.isPresent(),
                nearDeath.isPresent(), worldSpawn.isPresent(), controlledFallback.isPresent());
        Optional<Vec3> destination = switch (mode) {
            case EXPEDITION_ANCHOR -> anchorPoint;
            case NEAR_ANCHOR -> nearAnchor;
            case NEAR_DEATH -> nearDeath;
            case WORLD_SPAWN -> worldSpawn;
            case CONTROLLED_PLATFORM -> controlledFallback;
            default -> Optional.empty();
        };
        if (destination.isEmpty()) return null;

        float yaw = matches && (mode == DreamRespawnPolicy.Selection.EXPEDITION_ANCHOR
                || mode == DreamRespawnPolicy.Selection.NEAR_ANCHOR)
                ? anchor.yawOr(player.getYRot()) : player.getYRot();
        return new Resolution(destination.get(), yaw, mode);
    }

    /** Observation only. The dimension was already selected before the clone. */
    public static void onRespawnComplete(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (DreamDimensionCatalog.find(player.level().dimension()).isPresent()) {
            DreamsDimensions.LOGGER.info("DREAM_RESPAWN player_uuid={} target={} result=respawn_completed "
                            + "anchor_present={}",
                    player.getUUID(), player.level().dimension().identifier(),
                    player.hasData(ModAttachments.DREAM_EXPEDITION_ANCHOR));
        }
    }

    /**
     * Admin/world-mod transitions are not a genuine expedition. Cleanup is
     * delayed until the NEXT server tick, after synchronous teleport callbacks
     * and the Awakener's result/consumption have finished.
     */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.hasData(ModAttachments.DREAM_EXPEDITION_ANCHOR)) return;
        DreamExpeditionAnchorData anchor = player.getData(ModAttachments.DREAM_EXPEDITION_ANCHOR);
        if (!anchor.isActiveFor(player.level().dimension())) {
            player.removeData(ModAttachments.DREAM_EXPEDITION_ANCHOR);
            DreamsDimensions.LOGGER.info(
                    "DREAM_RESPAWN player_uuid={} result=anchor_cleared reason=dimension_mismatch current={}",
                    player.getUUID(), player.level().dimension().identifier());
        }
    }

    private record Resolution(Vec3 position, float yaw, DreamRespawnPolicy.Selection selection) {}
}
