package com.dreamsdimensions.mod.util;

import com.dreamsdimensions.mod.attachment.DreamReturnData;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Set;

/**
 * Resolve a posição de retorno no Overworld sem usar alturas fixas nem spawn inseguro.
 *
 * Prioridade: cama onírica registrada > respawn pessoal válido > spawn seguro do mundo.
 * Se nenhuma alternativa for segura, o teleporte não é executado.
 */
public final class DreamReturnHelper {
    private static final int SEARCH_RADIUS = 4;

    private DreamReturnHelper() {}

    public static boolean isDreamDimension(ServerLevel level) {
        return DreamsConfig.isDreamDimension(level.dimension());
    }

    public static Optional<TeleportTransition> buildReturnTransition(ServerPlayer player) {
        ServerLevel overworld = ((ServerLevel) player.level()).getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return Optional.empty();
        }

        return resolveTarget(player, overworld).map(target -> new TeleportTransition(
                overworld, target.position(), Vec3.ZERO,
                target.yaw(), player.getXRot(), Set.of(), TeleportTransition.PLACE_PORTAL_TICKET
        ));
    }

    private static Optional<ReturnTarget> resolveTarget(ServerPlayer player, ServerLevel overworld) {
        DreamReturnData data = player.getData(ModAttachments.DREAM_RETURN);

        Optional<Vec3> nearBed = resolveBedStandUp(player, overworld, data);
        if (nearBed.isPresent()) {
            return Optional.of(new ReturnTarget(nearBed.get(), data.getYawOr(player.getYRot())));
        }

        ServerPlayer.RespawnConfig respawn = player.getRespawnConfig();
        if (respawn != null && respawn.respawnData().dimension() == Level.OVERWORLD) {
            TeleportTransition personalRespawn =
                    player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
            if (personalRespawn.newLevel().dimension() == Level.OVERWORLD
                    && !personalRespawn.missingRespawnBlock()) {
                return Optional.of(new ReturnTarget(personalRespawn.position(), personalRespawn.yRot()));
            }
        }

        // O spawn global também precisa passar pelo teste físico de espaço disponível.
        BlockPos sharedSpawn = overworld.getLevelData().getRespawnData().pos();
        return findSafeSpawn(player, overworld, sharedSpawn)
                .map(position -> new ReturnTarget(position, player.getYRot()));
    }

    private static Optional<Vec3> resolveBedStandUp(ServerPlayer player, ServerLevel overworld, DreamReturnData data) {
        if (!data.hasBedData() || data.getDimension() != Level.OVERWORLD) {
            return Optional.empty();
        }

        BlockPos bedPos = data.getPos();
        if (bedPos == null || !overworld.isInsideBuildHeight(bedPos)) {
            return Optional.empty();
        }

        overworld.getChunkAt(bedPos);
        BlockState state = overworld.getBlockState(bedPos);
        if (!(state.getBlock() instanceof BedBlock)) {
            return Optional.empty();
        }

        return BedBlock.findStandUpPosition(
                player.getType(), overworld, bedPos,
                state.getValue(BedBlock.FACING), data.getYawOr(0.0F)
        );
    }

    private static Optional<Vec3> findSafeSpawn(ServerPlayer player, ServerLevel level, BlockPos origin) {
        for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius > 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    if (y <= level.getMinY() || y >= level.getMaxY() - 2) {
                        continue;
                    }
                    Vec3 candidate = DismountHelper.findSafeDismountLocation(
                            player.getType(), level, new BlockPos(x, y, z), false);
                    if (candidate != null) {
                        return Optional.of(candidate);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private record ReturnTarget(Vec3 position, float yaw) {}
}
