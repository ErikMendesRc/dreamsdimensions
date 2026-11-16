package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModItems;
import com.dreamsdimensions.mod.service.DreamRecoveryLocator;
import com.dreamsdimensions.mod.service.DreamAwakenerInventory;
import com.dreamsdimensions.mod.service.EchoMossCooldown;
import com.dreamsdimensions.mod.service.EchoMossEligibility;
import com.dreamsdimensions.mod.service.EchoMossSearch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Eco do Musgo V0: a single RightClickBlock adapter, not a tick task or
 * additional block override. Eligibility is checked on both logical sides
 * for consistent cancellation; only the server scans and provides feedback.
 */
public final class EchoMossInteractionHandler {
    private static final int COOLDOWN_TICKS = 100;
    private static final EchoMossCooldown COOLDOWNS = new EchoMossCooldown(COOLDOWN_TICKS);
    private static final boolean DEBUG = Boolean.getBoolean("dreamsdimensions.echoMossDebug");

    private EchoMossInteractionHandler() {}

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!EchoMossEligibility.accepts(event.getEntity().isAlive(),
                event.getEntity().gameMode() == GameType.SURVIVAL,
                DreamsConfig.isDreamDimension(event.getLevel().dimension()),
                event.getHand() == InteractionHand.MAIN_HAND,
                event.getItemStack().isEmpty())) return;

        BlockPos origin = event.getPos();
        if (!event.getEntity().isWithinBlockInteractionRange(origin, 0.0)
                || !event.getLevel().getWorldBorder().isWithinBounds(origin)
                || !event.getLevel().hasChunkAt(origin)
                || !event.getLevel().getBlockState(origin).is(ModBlocks.DREAM_GLOW_MOSS.get())) return;

        // Consume this *eligible* empty-hand interaction on both sides.
        // No custom packet, item use, block mutation or offhand replay.
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)) return;
        if (!COOLDOWNS.tryAcquire(player.getUUID(), level.getGameTime())) return;

        // Recoverable dream: when there is no intact Awakener, redirect the
        // existing Echo interaction toward the nearest recovery structure.
        // Rate-limited by the same per-player cooldown as the ordinary echo.
        boolean intact = DreamAwakenerInventory.hasReturnItem(player, false);
        if (!intact) {
            try {
                var ruin = DreamRecoveryLocator.find(level, origin);
                if (ruin.isPresent()) {
                    BlockPos target = ruin.get();
                    signal(player, level, origin,
                            new EchoMossSearch.Cell(target.getX(), target.getY(), target.getZ()));
                    int distance = (int) Math.round(Math.sqrt(origin.distSqr(target)));
                    player.sendSystemMessage(Component.translatable(
                            "message.dreamsdimensions.echo_moss.recovery",
                            DreamRecoveryLocator.direction(origin, target), distance));
                    return;
                }
                player.sendOverlayMessage(Component.translatable(
                        "message.dreamsdimensions.echo_moss.recovery_missing"));
            } catch (RuntimeException exception) {
                DreamsDimensions.LOGGER.warn(
                        "Recovery structure search unavailable for player {}", player.getUUID(), exception);
                player.sendOverlayMessage(Component.translatable(
                        "message.dreamsdimensions.echo_moss.unavailable"));
            }
            return;
        }

        long started = DEBUG ? System.nanoTime() : 0;
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        EchoMossSearch.Result result = EchoMossSearch.findNearest(
                new EchoMossSearch.Cell(origin.getX(), origin.getY(), origin.getZ()),
                level.getMinY(), level.getMaxY(),
                (x, y, z) -> {
                    probe.set(x, y, z);
                    if (!level.getWorldBorder().isWithinBounds(probe)) {
                        return EchoMossSearch.Sample.OUTSIDE;
                    }
                    // getChunkNow is explicitly non-loading. Never request/getChunk
                    // without its "now"/non-generating guarantee.
                    LevelChunk chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
                    if (chunk == null) return EchoMossSearch.Sample.UNAVAILABLE;
                    return chunk.getBlockState(probe).is(ModBlocks.DREAM_SHIMMER_BLOCK.get())
                            ? EchoMossSearch.Sample.TARGET : EchoMossSearch.Sample.OTHER;
                });
        if (DEBUG) {
            DreamsDimensions.LOGGER.debug(
                    "ECHO_MOSS result={} inspected={} unloaded={} durationMicros={}",
                    result.status(), result.inspected(), result.unavailable(),
                    (System.nanoTime() - started) / 1_000);
        }
        if (result.found()) {
            signal(player, level, origin, result.target());
        } else {
            String key = result.status() == EchoMossSearch.Status.UNAVAILABLE
                    ? "message.dreamsdimensions.echo_moss.unavailable"
                    : "message.dreamsdimensions.echo_moss.absent";
            player.sendOverlayMessage(Component.translatable(key));
        }
    }

    /** 12 vanilla particles total, short-lived, player-specific and not a GPS marker. */
    private static void signal(ServerPlayer player, ServerLevel level, BlockPos origin,
            EchoMossSearch.Cell target) {
        double sx = origin.getX() + 0.5, sy = origin.getY() + 0.65, sz = origin.getZ() + 0.5;
        double dx = target.x() - origin.getX();
        double dy = target.y() - origin.getY();
        double dz = target.z() - origin.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double length = Math.min(3.6, distance);
        for (int i = 1; i <= 9; i++) {
            double step = length * i / 9.0 / Math.max(1.0, distance);
            level.sendParticles(player, ParticleTypes.END_ROD, false, false,
                    sx + dx * step, sy + dy * step, sz + dz * step,
                    1, 0, 0, 0, 0);
        }
        level.sendParticles(player, ParticleTypes.ENCHANT, false, false,
                sx, sy, sz, 3, 0.3, 0.3, 0.3, 0);
        level.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS, 0.55F, 1.25F);
        player.sendOverlayMessage(Component.translatable("message.dreamsdimensions.echo_moss.found"));
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        COOLDOWNS.clear(event.getEntity().getUUID());
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        COOLDOWNS.clear(event.getEntity().getUUID());
    }

    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        COOLDOWNS.clear(event.getEntity().getUUID());
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        COOLDOWNS.clearAll();
    }
}
