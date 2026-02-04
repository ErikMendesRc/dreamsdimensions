package com.dreamsdimensions.mod.config;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Configuração do mod baseada em {@link ModConfigSpec}.
 * <p>
 * A classe é inscrita no MOD bus via {@link EventBusSubscriber}, conforme eventos
 * {@link ModConfigEvent.Loading} e {@link ModConfigEvent.Reloading} descritos no SDK.
 * </p>
 */
@EventBusSubscriber(modid = DreamsDimensions.MODID)
public final class DreamsConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> DREAM_DIMENSION_IDS = BUILDER
            .comment("Lista de dimensoes consideradas como sonho (ResourceLocation).")
            .defineListAllowEmpty(
                    "dream_dimensions",
                    List.of(
                            DreamsDimensions.MODID + ":dreamscape",
                            DreamsDimensions.MODID + ":campo_onirico_azul"
                    ),
                    () -> DreamsDimensions.MODID + ":dreamscape",
                    DreamsConfig::isValidResourceLocation
            );

    private static Set<ResourceKey<Level>> dreamDimensions = Set.of();

    /**
     * Especificação final registrada no {@link net.neoforged.fml.ModContainer#registerConfig}.
     */
    public static final ModConfigSpec SPEC = BUILDER.build();

    private DreamsConfig() {
    }

    /**
     * Disparado quando a configuração é carregada.
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        LOGGER.info("Carregando configuração de Dreams Dimensions: {}", event.getConfig().getFileName());
        bake();
    }

    /**
     * Disparado quando a configuração é recarregada (ex: /reload).
     */
    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        LOGGER.info("Recarregando configuração de Dreams Dimensions: {}", event.getConfig().getFileName());
        bake();
    }

    public static boolean isDreamDimension(ResourceKey<Level> dimension) {
        return dreamDimensions.contains(dimension);
    }

    private static boolean isValidResourceLocation(Object value) {
        return value instanceof String string && ResourceLocation.tryParse(string) != null;
    }

    private static void bake() {
        Set<ResourceKey<Level>> parsed = new HashSet<>();
        for (String entry : DREAM_DIMENSION_IDS.get()) {
            ResourceLocation id = ResourceLocation.tryParse(entry);
            if (id == null) {
                LOGGER.warn("Dimensão de sonho inválida na config: {}", entry);
                continue;
            }
            parsed.add(ResourceKey.create(Registries.DIMENSION, id));
        }
        dreamDimensions = Set.copyOf(parsed);
    }
}
