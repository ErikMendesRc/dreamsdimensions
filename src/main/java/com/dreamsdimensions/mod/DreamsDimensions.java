package com.dreamsdimensions.mod;

import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.diagnostics.DreamscapeBridgeAuditCommand;
import com.dreamsdimensions.mod.event.CommonEvents;
import com.dreamsdimensions.mod.event.DreamDimensionEffectsHandler;
import com.dreamsdimensions.mod.event.DreamReturnAttachmentHandler;
import com.dreamsdimensions.mod.event.SleepTeleportHandler;
import com.dreamsdimensions.mod.registry.ModAttachments;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModCreativeTabs;
import com.dreamsdimensions.mod.registry.ModEntities;
import com.dreamsdimensions.mod.entity.FragmentoIslandPopulation;
import com.dreamsdimensions.mod.entity.VigiaIslandPopulation;
import com.dreamsdimensions.mod.entity.spawn.DreamMobPopulationService;
import com.dreamsdimensions.mod.registry.ModItems;
import com.dreamsdimensions.mod.worldgen.DreamWorldgen;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/** Registers content on the mod bus and runtime handlers on the game bus. */
@Mod(DreamsDimensions.MODID)
public class DreamsDimensions {
    public static final String MODID = "dreamsdimensions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DreamsDimensions(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Dreams Dimensions Mod está carregando!");
        modContainer.registerConfig(ModConfig.Type.LOCAL, DreamsConfig.SPEC);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModEntities.register(modEventBus);
        DreamWorldgen.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        // Read-only test-server diagnostics; absent in normal mod startup.
        if (Boolean.getBoolean("dreamsdimensions.enableBridgeAudit")) {
            NeoForge.EVENT_BUS.addListener(DreamscapeBridgeAuditCommand::register);
        }

        NeoForge.EVENT_BUS.addListener(CommonEvents::onServerStarting);
        DreamMobPopulationService.register(FragmentoIslandPopulation.profile());
        DreamMobPopulationService.register(VigiaIslandPopulation.profile());
        NeoForge.EVENT_BUS.addListener(DreamMobPopulationService::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(DreamDimensionEffectsHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(DreamDimensionEffectsHandler::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(DreamReturnAttachmentHandler::onPlayerSetSpawn);
        NeoForge.EVENT_BUS.addListener(SleepTeleportHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(SleepTeleportHandler::onPlayerWakeUp);
        NeoForge.EVENT_BUS.addListener(SleepTeleportHandler::onPlayerLoggedOut);
        LOGGER.info("Registros e listeners de Dreams Dimensions configurados.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Motor onírico pronto: {} perfis dimensionais lógicos.",
                DreamDimensionCatalog.all().size());
    }
}
