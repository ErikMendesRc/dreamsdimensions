package com.dreamsdimensions.mod.util;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.dimension.DreamDimensionDefinition;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Shared arrival resolver. Natural floor first, 5x5 air-only platform last.
 * Never writes into an existing building or outside the world border.
 */
public final class DreamSpawnHelper {
    private static final int PLATFORM_RADIUS = 2;
    private static final int[] PLATFORM_OFFSETS = {0, 6, -6, 12, -12};
    private static final int MAX_PLATFORM_RISE = 32;

    private DreamSpawnHelper() {}

    public static Optional<Vec3> findOrCreateSafeArrival(
            ServerLevel level, DreamDimensionDefinition definition, BlockPos origin, ServerPlayer player) {
        if (level.dimension() != definition.dimension()) {
            throw new IllegalArgumentException("Spawn profile does not match target level");
        }
        int radius = definition.naturalSpawnRadius();
        Optional<Vec3> natural = findNaturalArrival(level, origin, player, radius);
        if (natural.isPresent()) {
            DreamsDimensions.LOGGER.debug("Superfície natural encontrada em {}: {}", level.dimension(), natural.get());
            return natural;
        }
        DreamsDimensions.LOGGER.warn(
                "Sem superfície natural segura em {} perto de {} (raio {}). Tentando criar área de chegada.",
                level.dimension(), origin, radius);
        return createArrivalPlatform(level, definition, origin, player);
    }

    /** Bounded search for an existing safe standing surface; NEVER writes blocks. */
    public static Optional<Vec3> findNaturalArrival(
            ServerLevel level, BlockPos origin, ServerPlayer player, int searchRadius) {
        for (int radius = 0; radius <= searchRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius != 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    // The heightmap query may load/generate a chunk: never
                    // probe outside the world border, even in a fallback.
                    if (!level.getWorldBorder().isWithinBounds(new BlockPos(x, origin.getY(), z))) continue;
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos feet = new BlockPos(x, y, z);
                    if (isSafeStandingSpace(level, feet, player)) {
                        return Optional.of(Vec3.atBottomCenterOf(feet));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /** Shared physical validator for first arrival and death respawn. */
    public static boolean isSafeStandingSpace(ServerLevel level, BlockPos feet, ServerPlayer player) {
        if (feet.getY() <= level.getMinY() || feet.getY() >= level.getMaxY() - 2
                || !level.getWorldBorder().isWithinBounds(feet)) return false;

        BlockPos floor = feet.below();
        BlockState floorState = level.getBlockState(floor);
        if (!floorState.isFaceSturdy(level, floor, Direction.UP)
                || !level.getFluidState(floor).isEmpty()
                || floorState.is(Blocks.MAGMA_BLOCK)
                || floorState.is(Blocks.CACTUS)
                || floorState.is(Blocks.CAMPFIRE)
                || floorState.is(Blocks.SOUL_CAMPFIRE)
                || floorState.is(Blocks.POINTED_DRIPSTONE)
                || floorState.is(Blocks.SWEET_BERRY_BUSH)
                || floorState.is(Blocks.WITHER_ROSE)) return false;

        if (!level.getBlockState(feet).isAir()
                || !level.getBlockState(feet.above()).isAir()
                || !level.getFluidState(feet).isEmpty()
                || !level.getFluidState(feet.above()).isEmpty()) return false;

        Vec3 position = Vec3.atBottomCenterOf(feet);
        return level.noCollision(player, player.getDimensions(Pose.STANDING).makeBoundingBox(position));
    }

    private static Optional<Vec3> createArrivalPlatform(
            ServerLevel level, DreamDimensionDefinition definition, BlockPos origin, ServerPlayer player) {
        BlockState platform = definition.fallbackFloor().get().defaultBlockState();
        int minFloor = level.getMinY() + 3;
        int maxFloor = level.getMaxY() - 5;
        if (maxFloor <= minFloor) return Optional.empty();

        for (int dx : PLATFORM_OFFSETS) {
            for (int dz : PLATFORM_OFFSETS) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                if (!level.getWorldBorder().isWithinBounds(new BlockPos(x, origin.getY(), z))) continue;
                int highest = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                int startingY = Math.max(minFloor, Math.min(maxFloor, Math.max(highest, origin.getY() - 1)));
                for (int y = startingY; y <= Math.min(maxFloor, startingY + MAX_PLATFORM_RISE); y += 4) {
                    BlockPos centerFloor = new BlockPos(x, y, z);
                    if (!isClearForPlatform(level, centerFloor)) continue;
                    if (!placePlatform(level, centerFloor, platform)) continue;

                    BlockPos feet = centerFloor.above();
                    if (isSafeStandingSpace(level, feet, player)) {
                        DreamsDimensions.LOGGER.info("Área de chegada 5x5 criada em {} no piso {} (jogador em {}).",
                                level.dimension(), centerFloor, feet);
                        return Optional.of(Vec3.atBottomCenterOf(feet));
                    }
                }
            }
        }
        DreamsDimensions.LOGGER.error(
                "Falha ao preparar área de chegada segura em {}: nenhum espaço livre 5x5 encontrado perto de {}.",
                level.dimension(), origin);
        return Optional.empty();
    }

    private static boolean isClearForPlatform(ServerLevel level, BlockPos center) {
        for (int dx = -PLATFORM_RADIUS; dx <= PLATFORM_RADIUS; dx++) {
            for (int dz = -PLATFORM_RADIUS; dz <= PLATFORM_RADIUS; dz++) {
                BlockPos floor = center.offset(dx, 0, dz);
                if (!level.getWorldBorder().isWithinBounds(floor)) return false;
                for (int dy = 0; dy <= 3; dy++) {
                    if (!level.getBlockState(floor.above(dy)).isAir()) return false;
                }
            }
        }
        return true;
    }

    private static boolean placePlatform(ServerLevel level, BlockPos center, BlockState state) {
        if (!isClearForPlatform(level, center)) return false;
        for (int dx = -PLATFORM_RADIUS; dx <= PLATFORM_RADIUS; dx++) {
            for (int dz = -PLATFORM_RADIUS; dz <= PLATFORM_RADIUS; dz++) {
                BlockPos floor = center.offset(dx, 0, dz);
                if (!level.setBlockAndUpdate(floor, state)) {
                    DreamsDimensions.LOGGER.error("Falha ao colocar bloco de chegada em {} ({})",
                            floor, level.dimension());
                    return false;
                }
            }
        }
        return true;
    }
}
