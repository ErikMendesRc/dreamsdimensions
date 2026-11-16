package com.dreamsdimensions.mod.block;

import net.minecraft.core.BlockPos;
import com.dreamsdimensions.mod.service.CoreChamberPolicy;
import com.dreamsdimensions.mod.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import com.dreamsdimensions.mod.menu.OneiricCoreMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Task 8.5: lightweight vanilla particle accent for an already-registered
 * decorative/crafting block. The block ID, state, loot, tools, collisions,
 * redstone and world generation are unchanged. Vanilla calls animateTick
 * client-side for visible nearby blocks; no server listeners or radius scan.
 */
public final class OneiricCoreBlock extends Block {
    public static final BooleanProperty ATTUNED = BooleanProperty.create("attuned");
    private static final DustParticleOptions CORE_DUST =
            new DustParticleOptions(0x82CED2, 0.65F);

    public OneiricCoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ATTUNED,false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
        builder.add(ATTUNED);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.REMINISCENCE.get())) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!CoreChamberPolicy.canAttune(level,pos)) {
            if (!level.isClientSide()) player.sendSystemMessage(
                    Component.translatable("message.dreamsdimensions.chamber.attunement_missing"));
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos,state.setValue(ATTUNED,true),3);
            if(!player.getAbilities().instabuild)stack.shrink(1);
            player.sendSystemMessage(Component.translatable("message.dreamsdimensions.chamber.attuned"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, owner) -> new OneiricCoreMenu(id, inventory,
                            ContainerLevelAccess.create(level, pos)),
                    Component.translatable("container.dreamsdimensions.oneiric_core")));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // At most one particle, approximately once per 12 visible random ticks.
        if (random.nextInt(12) != 0) return;
        double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
        double y = pos.getY() + 0.9 + random.nextDouble() * 0.15;
        double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
        level.addParticle(CORE_DUST, x, y, z, 0, 0.007, 0);
    }
}
