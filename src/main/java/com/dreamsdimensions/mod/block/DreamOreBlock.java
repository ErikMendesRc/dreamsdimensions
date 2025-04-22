package com.dreamsdimensions.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Bloco de Minério dos Sonhos.
 * Estende DropExperienceBlock para dropar orbes de XP quando minerado sem Silk Touch.
 * O item dropado (ex: Dream Dust) é definido pela loot table do bloco.
 */
public class DreamOreBlock extends DropExperienceBlock {
    public static final BooleanProperty CLICKED = BooleanProperty.create("clicked");

    /**
     * Construtor para o Bloco de Minério dos Sonhos.
     *
     * @param pProperties Propriedades do bloco (material, dureza, som, ferramenta necessária, etc.).
     *                    Estas são definidas em ModBlocks ao registrar o bloco.
     * @param pXpRange    Provedor para a quantidade de XP a ser dropada (ex: UniformInt.of(1, 3)).
     *                    Também definido em ModBlocks.
     */
    public DreamOreBlock(BlockBehaviour.Properties pProperties, IntProvider pXpRange) {
        super(pXpRange, pProperties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            boolean currentState = state.getValue(CLICKED);
            level.setBlockAndUpdate(pos, state.setValue(CLICKED, !currentState));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CLICKED);
    }
}