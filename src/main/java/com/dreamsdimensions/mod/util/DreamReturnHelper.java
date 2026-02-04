package com.dreamsdimensions.mod.util;

import com.dreamsdimensions.mod.attachment.DreamReturnData;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

/**
 * Helpers para retorno seguro do mundo dos sonhos.
 */
public final class DreamReturnHelper {
    private static final int SAFE_SEARCH_UP = 6;

    private DreamReturnHelper() {}

    public static boolean isDreamDimension(ServerLevel level) {
        return DreamsConfig.isDreamDimension(level.dimension());
    }

    public static Optional<TeleportTransition> buildReturnTransition(ServerPlayer player) {
        ServerLevel currentLevel = player.level();

        MinecraftServer server = currentLevel.getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return Optional.empty();
        }

        ReturnTarget target = resolveTarget(player, overworld);

        TeleportTransition transition = new TeleportTransition(
                overworld,
                target.position(),
                Vec3.ZERO,
                target.yaw(),
                player.getXRot(),
                Set.of(),
                TeleportTransition.PLACE_PORTAL_TICKET
        );
        return Optional.of(transition);
    }

    private static ReturnTarget resolveTarget(ServerPlayer player, ServerLevel overworld) {
        DreamReturnData data = player.getData(ModAttachments.DREAM_RETURN);

        // 1) Stand-up de cama gravada (mais "natural" pra retorno)
        Optional<Vec3> bedStandUp = resolveBedStandUp(overworld, data);
        if (bedStandUp.isPresent()) {
            float yaw = data.getYawOr(player.getYRot());
            return new ReturnTarget(bedStandUp.get(), yaw);
        }

        // 2) Respawn do player (cama/âncora) — fallback vanilla
        try {
            TeleportTransition respawn = player.findRespawnPositionAndUseSpawnBlock(
                    false,
                    TeleportTransition.DO_NOTHING
            );
            return new ReturnTarget(respawn.position(), respawn.yRot());
        } catch (Throwable ignored) {
            // cai pro fallback abaixo
        }

        // 3) Fallback: spawn do mundo (1.21.10+: via RespawnData)
        BlockPos worldSpawn = overworld.getLevelData().getRespawnData().pos();
        Vec3 fallback = findSafeSpawn(overworld, worldSpawn);
        return new ReturnTarget(fallback, player.getYRot());
    }

    private static Optional<Vec3> resolveBedStandUp(ServerLevel overworld, DreamReturnData data) {
        if (!data.hasBedData() || data.getDimension() != Level.OVERWORLD) {
            return Optional.empty();
        }

        BlockPos bedPos = data.getPos();
        if (bedPos == null) {
            return Optional.empty();
        }

        overworld.getChunkAt(bedPos);
        BlockState state = overworld.getBlockState(bedPos);

        if (!(state.getBlock() instanceof BedBlock)) {
            return Optional.empty();
        }

        // 1.21.11+: BedBlock.canSetSpawn(...) sumiu; regra vem dos EnvironmentAttributes
        BedRule bedRule = overworld.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, bedPos);
        if (!bedRule.canSetSpawn(overworld)) {
            return Optional.empty();
        }

        return BedBlock.findStandUpPosition(
                EntityType.PLAYER,
                overworld,
                bedPos,
                state.getValue(BedBlock.FACING),
                data.getYawOr(0.0F)
        );
    }

    private static Vec3 findSafeSpawn(ServerLevel level, BlockPos origin) {
        level.getChunkAt(origin);

        int y = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                origin.getX(),
                origin.getZ()
        );

        if (y <= level.getMinY()) {
            y = level.getMinY() + 2;
        }

        BlockPos basePos = new BlockPos(origin.getX(), y, origin.getZ());
        Vec3 safe = findSafeDismount(level, basePos);
        if (safe != null) {
            return safe;
        }

        return Vec3.upFromBottomCenterOf(basePos, 1.0);
    }

    @Nullable
    private static Vec3 findSafeDismount(ServerLevel level, BlockPos basePos) {
        BlockPos.MutableBlockPos mutable = basePos.mutable();
        for (int i = 0; i <= SAFE_SEARCH_UP; i++) {
            Vec3 candidate = DismountHelper.findSafeDismountLocation(
                    EntityType.PLAYER,
                    level,
                    mutable,
                    false
            );
            if (candidate != null) {
                return candidate;
            }
            mutable.move(Direction.UP);
        }
        return null;
    }

    private record ReturnTarget(Vec3 position, float yaw) {}
}