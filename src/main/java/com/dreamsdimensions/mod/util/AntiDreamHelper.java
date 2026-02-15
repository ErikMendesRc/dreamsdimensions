package com.dreamsdimensions.mod.util;

import com.dreamsdimensions.mod.attachment.DreamReturnData;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.registry.ModAttachments;
import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer.RespawnConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import org.jetbrains.annotations.Nullable;

public final class AntiDreamHelper {
    private AntiDreamHelper() {
    }

    public static boolean isAnchoringTotemNearBed(ServerPlayer player) {
        if (player.level().isClientSide()) {
            return false;
        }

        ServerLevel level = (ServerLevel) player.level();
        BlockPos referencePos = resolveReferencePos(player);
        int radius = DreamsConfig.getAnchoringTotemRadius();

        int minX = referencePos.getX() - radius;
        int maxX = referencePos.getX() + radius;
        int minY = referencePos.getY() - radius;
        int maxY = referencePos.getY() + radius;
        int minZ = referencePos.getZ() - radius;
        int maxZ = referencePos.getZ() + radius;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    if (level.getBlockState(cursor).is(ModBlocks.OW_ANCHORING_TOTEM.get())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static BlockPos resolveReferencePos(ServerPlayer player) {
        DreamReturnData data = player.getData(ModAttachments.DREAM_RETURN);
        if (data.hasBedData() && data.getDimension() == Level.OVERWORLD && data.getPos() != null) {
            return data.getPos();
        }

        @Nullable BlockPos respawnPos = getOverworldRespawnPos(player);
        if (respawnPos != null) {
            return respawnPos;
        }

        return player.blockPosition();
    }

    @Nullable
    private static BlockPos getOverworldRespawnPos(ServerPlayer player) {
        RespawnConfig respawnConfig = player.getRespawnConfig();
        if (respawnConfig == null) {
            return null;
        }

        LevelData.RespawnData respawnData = respawnConfig.respawnData();
        if (respawnData.dimension() != Level.OVERWORLD) {
            return null;
        }

        return respawnData.pos();
    }
}
