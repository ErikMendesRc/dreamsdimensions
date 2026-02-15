package com.dreamsdimensions.mod.client.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.client.renderer.blockentity.BlockEntityRendererRegistry;
import com.dreamsdimensions.mod.client.renderer.blockentity.OwLuminaFlowerBlockEntityRenderer;
import com.dreamsdimensions.mod.registry.ModBlockEntities;
import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Eventos de lifecycle client-only.
 */
@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        DreamsDimensions.LOGGER.info("Executando Client Setup para Dreams Dimensions...");
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.OW_LUMINA_FLOWER.get(), ChunkSectionLayer.CUTOUT);
            BlockEntityRendererRegistry.register(ModBlockEntities.OW_LUMINA_FLOWER.get(), OwLuminaFlowerBlockEntityRenderer::new);
        });
        DreamsDimensions.LOGGER.info("Client Setup de Dreams Dimensions concluído.");
    }
}
