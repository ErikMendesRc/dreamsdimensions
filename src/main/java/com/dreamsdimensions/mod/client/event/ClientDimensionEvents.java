package com.dreamsdimensions.mod.client.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.client.screen.DreamTransitionScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;

/**
 * Eventos client-only relacionados à transição de dimensão.
 * <p>
 * O registro usa {@link RegisterDimensionTransitionScreenEvent} no MOD bus do cliente,
 * conforme o javadoc do evento.
 * </p>
 */
@EventBusSubscriber(
        modid = DreamsDimensions.MODID,
        value = Dist.CLIENT
)
public final class ClientDimensionEvents {

    private static final ResourceKey<Level> DREAMSCAPE_KEY =
            ResourceKey.create(
                    Registries.DIMENSION,
                    Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "dreamscape")
            );

    private ClientDimensionEvents() {
    }

    @SubscribeEvent
    public static void onRegisterDimensionTransitionScreen(RegisterDimensionTransitionScreenEvent event) {
        event.registerConditionalEffect(
                DREAMSCAPE_KEY,
                Level.OVERWORLD,
                DreamTransitionScreen::new
        );
    }
}