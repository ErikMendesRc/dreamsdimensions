package com.dreamsdimensions.mod.util;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Pose;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Resolve a chegada em uma dimensão dos sonhos.
 *
 * Primeiro procura terreno natural em uma área limitada. Se uma dimensão
 * não tiver superfície segura perto do spawn do mundo (ex.: ilhas no vazio),
 * cria uma pequena plataforma de pedra temática SOMENTE em blocos de ar.
 * Não remove estruturas, não altera a geração de mundo e nunca usa Y fixo
 * fora dos limites da dimensão.
 *
 * Executar somente no thread do servidor.
 */
public final class DreamSpawnHelper {
    private static final int NATURAL_SEARCH_RADIUS = 12;
    private static final int PLATFORM_RADIUS = 2; // Plataforma 5x5
    private static final int[] PLATFORM_OFFSETS = {0, 6, -6, 12, -12};
    private static final int MAX_PLATFORM_RISE = 32;

    private DreamSpawnHelper() {}

    public static Optional<Vec3> findOrCreateSafeArrival(ServerLevel level, BlockPos origin, ServerPlayer player) {
        Optional<Vec3> natural = findNaturalArrival(level, origin, player);
        if (natural.isPresent()) {
            return natural;
        }

        DreamsDimensions.LOGGER.warn(
                "Sem superfície natural segura em {} perto de {} (raio {}). Tentando criar área de chegada.",
                level.dimension(), origin, NATURAL_SEARCH_RADIUS
        );
        return createArrivalPlatform(level, origin, player);
    }

    private static Optional<Vec3> findNaturalArrival(ServerLevel level, BlockPos origin, ServerPlayer player) {
        // O heightmap retorna o primeiro bloco livre ACIMA do terreno, mas
        // pode indicar o fundo do mundo quando a região é inteiramente vazia.
        for (int radius = 0; radius <= NATURAL_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius != 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }

                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
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

    private static boolean isSafeStandingSpace(ServerLevel level, BlockPos feet, ServerPlayer player) {
        if (feet.getY() <= level.getMinY() || feet.getY() >= level.getMaxY() - 2
                || !level.getWorldBorder().isWithinBounds(feet)) {
            return false;
        }

        BlockPos floor = feet.below();
        BlockState floorState = level.getBlockState(floor);
        // Evita água, lava, folhas, solo sem colisão e superfícies perigosas.
        if (!floorState.isFaceSturdy(level, floor, Direction.UP)
                || !level.getFluidState(floor).isEmpty()
                || floorState.is(Blocks.MAGMA_BLOCK)
                || floorState.is(Blocks.CACTUS)
                || floorState.is(Blocks.CAMPFIRE)
                || floorState.is(Blocks.SOUL_CAMPFIRE)) {
            return false;
        }

        if (!level.getBlockState(feet).isAir()
                || !level.getBlockState(feet.above()).isAir()
                || !level.getFluidState(feet).isEmpty()
                || !level.getFluidState(feet.above()).isEmpty()) {
            return false;
        }

        Vec3 position = Vec3.atBottomCenterOf(feet);
        return level.noCollision(player, player.getDimensions(Pose.STANDING).makeBoundingBox(position));
    }

    private static Optional<Vec3> createArrivalPlatform(ServerLevel level, BlockPos origin, ServerPlayer player) {
        BlockState platform = getDimensionFloor(level);
        int minFloor = level.getMinY() + 3;
        int maxFloor = level.getMaxY() - 5;
        if (maxFloor <= minFloor) {
            return Optional.empty();
        }

        // Poucos pontos perto da origem, sem geração de centenas de chunks.
        for (int dx : PLATFORM_OFFSETS) {
            for (int dz : PLATFORM_OFFSETS) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int highest = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                int startingY = Math.max(minFloor, Math.min(maxFloor, Math.max(highest, origin.getY() - 1)));

                for (int y = startingY; y <= Math.min(maxFloor, startingY + MAX_PLATFORM_RISE); y += 4) {
                    BlockPos centerFloor = new BlockPos(x, y, z);
                    if (!isClearForPlatform(level, centerFloor)) {
                        continue;
                    }

                    if (!placePlatform(level, centerFloor, platform)) {
                        continue;
                    }

                    BlockPos feet = centerFloor.above();
                    if (isSafeStandingSpace(level, feet, player)) {
                        DreamsDimensions.LOGGER.info(
                                "Área de chegada 5x5 criada em {} no piso {} (jogador em {}).",
                                level.dimension(), centerFloor, feet
                        );
                        return Optional.of(Vec3.atBottomCenterOf(feet));
                    }
                }
            }
        }

        DreamsDimensions.LOGGER.error(
                "Falha ao preparar área de chegada segura em {}: nenhum espaço livre 5x5 encontrado perto de {}.",
                level.dimension(), origin
        );
        return Optional.empty();
    }

    private static BlockState getDimensionFloor(ServerLevel level) {
        Identifier blue = Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "campo_onirico_azul");
        Block block = level.dimension().location().equals(blue)
                ? ModBlocks.BLUE_DREAM_STONE.get()
                : ModBlocks.SERENE_STONE_BLOCK.get();
        return block.defaultBlockState();
    }

    private static boolean isClearForPlatform(ServerLevel level, BlockPos center) {
        for (int dx = -PLATFORM_RADIUS; dx <= PLATFORM_RADIUS; dx++) {
            for (int dz = -PLATFORM_RADIUS; dz <= PLATFORM_RADIUS; dz++) {
                BlockPos floor = center.offset(dx, 0, dz);
                if (!level.getWorldBorder().isWithinBounds(floor)) {
                    return false;
                }

                // Apenas ar pode ser alterado. Não danificamos construções,
                // vegetação, líquidos ou blocos das dimensões.
                for (int dy = 0; dy <= 3; dy++) {
                    if (!level.getBlockState(floor.above(dy)).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean placePlatform(ServerLevel level, BlockPos center, BlockState state) {
        // Revalidação antes de qualquer escrita para não sobrepor terreno.
        if (!isClearForPlatform(level, center)) {
            return false;
        }
        for (int dx = -PLATFORM_RADIUS; dx <= PLATFORM_RADIUS; dx++) {
            for (int dz = -PLATFORM_RADIUS; dz <= PLATFORM_RADIUS; dz++) {
                BlockPos floor = center.offset(dx, 0, dz);
                if (!level.setBlockAndUpdate(floor, state)) {
                    DreamsDimensions.LOGGER.error("Falha ao colocar bloco de chegada em {} ({})", floor, level.dimension());
                    return false;
                }
            }
        }
        return true;
    }
}
