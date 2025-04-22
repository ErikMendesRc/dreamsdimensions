package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.client.screen.DreamTransitionScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(
        modid = DreamsDimensions.MODID,
        bus   = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientDimensionEvents {
    private static final ResourceKey<Level> DREAMSCAPE_KEY =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(DreamsDimensions.MODID, "dreamscape")
            );

    @SubscribeEvent
    public static void onRegisterDimensionTransitionScreen(RegisterDimensionTransitionScreenEvent event) {
        event.registerConditionalEffect(
                DREAMSCAPE_KEY,
                Level.OVERWORLD,
                DreamTransitionScreen::new
        );
    }
}