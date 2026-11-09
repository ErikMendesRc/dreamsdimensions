package com.dreamsdimensions.mod.client.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.geckolib.renderer.GeoEntityRenderer;

/** GeckoLib 5 built-in renderer for the initial model/animation/texture trio. */
@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)
public final class FragmentoRendererRegistration {
    private FragmentoRendererRegistration() {}

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FRAGMENTO_ERRANTE.get(),
                context -> new GeoEntityRenderer<>(context, ModEntities.FRAGMENTO_ERRANTE.get()));
    }
}
