package com.dreamsdimensions.mod;

import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.qa.DreamQaRecorder;
import com.dreamsdimensions.mod.qa.DreamQaCommand;
import com.dreamsdimensions.mod.config.DreamEntryConfig;
import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.diagnostics.DreamscapeBridgeAuditCommand;
import com.dreamsdimensions.mod.diagnostics.OverworldOreAuditCommand;
import com.dreamsdimensions.mod.diagnostics.MarkedStoneAuditCommand;
import com.dreamsdimensions.mod.diagnostics.DreamRuinsAuditCommand;
import com.dreamsdimensions.mod.event.CommonEvents;
import com.dreamsdimensions.mod.event.EchoMossInteractionHandler;
import com.dreamsdimensions.mod.event.DreamDimensionEffectsHandler;
import com.dreamsdimensions.mod.event.DreamReturnAttachmentHandler;
import com.dreamsdimensions.mod.event.SleepTeleportHandler;
import com.dreamsdimensions.mod.registry.ModAttachments;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModCreativeTabs;
import com.dreamsdimensions.mod.registry.ModEntities;
import com.dreamsdimensions.mod.registry.ModEffects;
import com.dreamsdimensions.mod.entity.FragmentoIslandPopulation;
import com.dreamsdimensions.mod.entity.VigiaIslandPopulation;
import com.dreamsdimensions.mod.entity.behavior.DreamAnchorService;
import com.dreamsdimensions.mod.entity.spawn.DreamMobPopulationService;
import com.dreamsdimensions.mod.registry.ModItems;
import com.dreamsdimensions.mod.registry.ModMenuTypes;
import com.dreamsdimensions.mod.item.OneiricAwakenerItem;
import com.dreamsdimensions.mod.worldgen.DreamWorldgen;
import com.dreamsdimensions.mod.service.DreamRespawnService;
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
        modContainer.registerConfig(ModConfig.Type.SYNCED, DreamEntryConfig.SPEC);

        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModEntities.register(modEventBus);
        ModEffects.register(modEventBus);
        DreamWorldgen.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        // Read-only test-server diagnostics; absent in normal mod startup.
        if (Boolean.getBoolean("dreamsdimensions.enableBridgeAudit")) {
            NeoForge.EVENT_BUS.addListener(DreamscapeBridgeAuditCommand::register);
        }
        // CI/development-only; read-only scan of preloaded Overworld chunks.
        if (Boolean.getBoolean("dreamsdimensions.enableOreAudit")) {
            NeoForge.EVENT_BUS.addListener(OverworldOreAuditCommand::register);
        }
        // Task 7.4: opt-in read-only natural Marked Stone survey, never loads chunks.
        if (Boolean.getBoolean("dreamsdimensions.enableMarkedStoneAudit")) {
            NeoForge.EVENT_BUS.addListener(MarkedStoneAuditCommand::register);
        }
        // Task 5.7: read-only, bounded, operator-only; NEVER active by default.
        if (Boolean.getBoolean("dreamsdimensions.enableRuinsAudit")) {
            NeoForge.EVENT_BUS.addListener(DreamRuinsAuditCommand::register);
        }

        // Always register command; telemetry recording stays off until opted in.
        NeoForge.EVENT_BUS.addListener(DreamQaCommand::register);
        NeoForge.EVENT_BUS.addListener(DreamQaCommand::onServerStopped);
        NeoForge.EVENT_BUS.addListener(CommonEvents::onServerStarting);
        NeoForge.EVENT_BUS.addListener(EchoMossInteractionHandler::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(EchoMossInteractionHandler::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(EchoMossInteractionHandler::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(EchoMossInteractionHandler::onChangedDimension);
        NeoForge.EVENT_BUS.addListener(EchoMossInteractionHandler::onServerStopped);
        DreamMobPopulationService.register(FragmentoIslandPopulation.profile());
        DreamMobPopulationService.register(VigiaIslandPopulation.profile());
        NeoForge.EVENT_BUS.addListener(DreamMobPopulationService::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(DreamMobPopulationService::onServerStopped);
        NeoForge.EVENT_BUS.addListener(DreamAnchorService::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(DreamAnchorService::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(DreamAnchorService::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(DreamAnchorService::onChangedDimension);
        NeoForge.EVENT_BUS.addListener(DreamDimensionEffectsHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(DreamDimensionEffectsHandler::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(DreamReturnAttachmentHandler::onPlayerSetSpawn);
        // Pre-clone respawn destination, post-clone diagnostics, and
        // deferred cleanup of stale anchors from admin dimension changes.
        NeoForge.EVENT_BUS.addListener(DreamRespawnService::onRespawnPosition);
        NeoForge.EVENT_BUS.addListener(DreamRespawnService::onRespawnComplete);
        NeoForge.EVENT_BUS.addListener(DreamRespawnService::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(SleepTeleportHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(SleepTeleportHandler::onPlayerWakeUp);
        NeoForge.EVENT_BUS.addListener(SleepTeleportHandler::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(SleepTeleportHandler::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(SleepTeleportHandler::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(OneiricAwakenerItem::onStoppedUsing);
        NeoForge.EVENT_BUS.addListener(OneiricAwakenerItem::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(OneiricAwakenerItem::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(OneiricAwakenerItem::onChangedDimension);
        NeoForge.EVENT_BUS.addListener(OneiricAwakenerItem::onServerStopped);
        LOGGER.info("Registros e listeners de Dreams Dimensions configurados.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Motor onírico pronto: {} perfis dimensionais lógicos.",
                DreamDimensionCatalog.all().size());
    }
}
