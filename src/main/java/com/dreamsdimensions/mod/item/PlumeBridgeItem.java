package com.dreamsdimensions.mod.item;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.dimension.DreamTrait;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.block.TemporaryPlumeBridgeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;

/** Deploys a temporary 3x3 platform under the player, only into air. */
public final class PlumeBridgeItem extends Item {
    public PlumeBridgeItem(Properties properties) { super(properties); }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        boolean fall = DreamDimensionCatalog.find(level.dimension())
                .map(d -> d.traits().contains(DreamTrait.FALL)).orElse(false);
        if (!fall) return InteractionResult.FAIL;
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel server)) {
            return InteractionResult.CONSUME;
        }
        BlockPos center = player.blockPosition().below();
        List<BlockPos> cells = new ArrayList<>(9);
        for (int dx=-1;dx<=1;dx++) for (int dz=-1;dz<=1;dz++) {
            BlockPos p=center.offset(dx,0,dz);
            if (p.getY() <= server.getMinY() || !server.getWorldBorder().isWithinBounds(p)
                    || !server.getBlockState(p).isAir()
                    || !server.getFluidState(p).isEmpty()) return InteractionResult.FAIL;
            cells.add(p);
        }
        for (BlockPos p:cells) {
            server.setBlock(p, ModBlocks.TEMPORARY_PLUME_BRIDGE.get().defaultBlockState(), 3);
            server.scheduleTick(p, ModBlocks.TEMPORARY_PLUME_BRIDGE.get(),
                    TemporaryPlumeBridgeBlock.LIFETIME_TICKS);
        }
        if (!serverPlayer.getAbilities().instabuild) serverPlayer.getItemInHand(hand).shrink(1);
        return InteractionResult.CONSUME;
    }
}
