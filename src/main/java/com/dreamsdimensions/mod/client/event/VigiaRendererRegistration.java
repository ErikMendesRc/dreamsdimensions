package com.dreamsdimensions.mod.client.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.registry.ModEntities;
import com.geckolib.renderer.GeoEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** GeckoLib 5 renderer uses the imported vigia_adormecido asset trio. */
@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)
public final class VigiaRendererRegistration {
    private VigiaRendererRegistration() {}

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.VIGIA_ADORMECIDO.get(),
                context -> new GeoEntityRenderer<>(context, ModEntities.VIGIA_ADORMECIDO.get()));
    }
}
