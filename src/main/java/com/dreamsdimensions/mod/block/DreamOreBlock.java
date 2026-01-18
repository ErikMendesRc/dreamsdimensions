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
 * <p>
 * Usa {@link BooleanProperty} {@link #CLICKED} para controlar o estado interativo e garantir
 * variantes válidas no blockstate JSON (ver javadoc de {@link BlockState} e {@link StateDefinition}).
 * </p>
 */
public class DreamOreBlock extends DropExperienceBlock {
    public static final BooleanProperty CLICKED = BooleanProperty.create("clicked");

    /**
     * @param properties propriedades do bloco, definidas no registro.
     * @param xpRange    provedor de XP dropado (ex: UniformInt.of(1, 3)).
     */
    public DreamOreBlock(BlockBehaviour.Properties properties, IntProvider xpRange) {
        super(xpRange, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CLICKED, Boolean.FALSE));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
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
