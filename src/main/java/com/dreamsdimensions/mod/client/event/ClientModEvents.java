package com.dreamsdimensions.mod.client.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Eventos de lifecycle client-only.
 * <p>
 * Registrado no MOD bus com {@link EventBusSubscriber}, conforme javadoc de
 * {@link FMLClientSetupEvent} para inicialização de renderizações no cliente.
 * </p>
 */
@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        DreamsDimensions.LOGGER.info("Executando Client Setup para Dreams Dimensions...");
        DreamsDimensions.LOGGER.info("Client Setup de Dreams Dimensions concluído.");
    }
}