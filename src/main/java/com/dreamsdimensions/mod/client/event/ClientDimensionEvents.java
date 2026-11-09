package com.dreamsdimensions.mod.client.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.client.screen.DreamTransitionScreen;
import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.dimension.DreamDimensionDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;

/**
 * Single client MOD-bus handler for configured transition pairs.
 * Exactly preserves the old Overworld -> Dreamscape loading screen.
 */
@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)
public final class ClientDimensionEvents {
    private ClientDimensionEvents() {}

    @SubscribeEvent
    public static void onRegisterDimensionTransitionScreen(RegisterDimensionTransitionScreenEvent event) {
        for (DreamDimensionDefinition definition : DreamDimensionCatalog.all()) {
            definition.transition().ifPresent(transition -> {
                switch (transition.visual()) {
                    case DREAM_MESSAGE -> event.registerConditionalEffect(
                            definition.dimension(), transition.fromDimension(), DreamTransitionScreen::new);
                }
            });
        }
    }
}
