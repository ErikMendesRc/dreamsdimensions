package com.dreamsdimensions.mod.client.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * A camada cutout da flor é definida em assets/dreamsdimensions/models/block/dream_flower_block.json.
 * ItemBlockRenderTypes.setRenderLayer não existe mais na API moderna.
 */
@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        DreamsDimensions.LOGGER.info("Inicialização cliente do Dreams Dimensions concluída.");
    }
}
