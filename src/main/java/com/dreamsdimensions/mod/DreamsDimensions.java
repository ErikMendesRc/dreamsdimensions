package com.dreamsdimensions.mod;

import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.event.CommonEvents;
import com.dreamsdimensions.mod.event.DreamDimensionEffectsHandler;
import com.dreamsdimensions.mod.event.DreamReturnAttachmentHandler;
import com.dreamsdimensions.mod.event.SleepTeleportHandler;
import com.dreamsdimensions.mod.registry.ModAttachments;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModCreativeTabs;
import com.dreamsdimensions.mod.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Bootstrap principal do mod Dreams Dimensions.
 * <p>
 * Este construtor registra registries no {@link IEventBus} do MOD (lifecycle) e listeners
 * de runtime no {@link NeoForge#EVENT_BUS}, conforme o padrão descrito nos javadocs de
 * {@link IEventBus#addListener} e do próprio {@link NeoForge#EVENT_BUS}.
 * </p>
 * <p>
 * A configuração é registrada via {@link ModContainer#registerConfig} (ver javadoc de
 * {@link ModConfig}) para garantir que o {@link com.dreamsdimensions.mod.config.DreamsConfig}
 * seja carregado no momento correto do lifecycle.
 * </p>
 */
@Mod(DreamsDimensions.MODID)
public class DreamsDimensions {
    public static final String MODID = "dreamsdimensions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DreamsDimensions(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Dreams Dimensions Mod está carregando!");

        modContainer.registerConfig(ModConfig.Type.COMMON, DreamsConfig.SPEC);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModAttachments.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.addListener(CommonEvents::onServerStarting);
        NeoForge.EVENT_BUS.addListener(DreamDimensionEffectsHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(DreamReturnAttachmentHandler::onPlayerSetSpawn);
        NeoForge.EVENT_BUS.addListener(SleepTeleportHandler::onPlayerTick);

        LOGGER.info("Registros e listeners de Dreams Dimensions configurados.");
    }

    /**
     * Setup comum do mod (lifecycle MOD bus) para ajustes que dependem de registries já carregados.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Executando Common Setup para Dreams Dimensions...");
        LOGGER.info("Common Setup de Dreams Dimensions concluído.");
    }
}
