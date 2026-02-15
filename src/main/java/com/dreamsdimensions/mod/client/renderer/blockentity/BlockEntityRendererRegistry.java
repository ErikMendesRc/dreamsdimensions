package com.dreamsdimensions.mod.client.renderer.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Wrapper local para registrar renderers de BlockEntity no ciclo de setup do cliente.
 */
public final class BlockEntityRendererRegistry {
    private BlockEntityRendererRegistry() {
    }

    public static <T extends BlockEntity, S extends BlockEntityRenderState> void register(
            BlockEntityType<? extends T> type,
            BlockEntityRendererProvider<T, S> provider
    ) {
        BlockEntityRenderers.register(type, provider);
    }
}
