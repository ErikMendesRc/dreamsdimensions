package com.dreamsdimensions.mod.client.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.content.emissive.NightEmissiveDebug;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)
public final class NightEmissiveClientDebugEvents {
    private NightEmissiveClientDebugEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        NightEmissiveDebug.logHeartbeat(minecraft.level, "clientTick");

        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) {
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = minecraft.level.getBlockState(pos);
        NightEmissiveDebug.logClientProbe(
                minecraft.level,
                pos,
                state,
                BuiltInRegistries.BLOCK.getKey(state.getBlock())
        );
    }
}
