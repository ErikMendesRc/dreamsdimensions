package com.dreamsdimensions.mod.config;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = DreamsDimensions.MODID)
public final class DreamsConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> DREAM_DIMENSION_IDS = BUILDER
            .comment("Lista de dimensoes consideradas como sonho (Identifier).")
            .defineListAllowEmpty(
                    "dream_dimensions",
                    List.of(
                            DreamsDimensions.MODID + ":dreamscape",
                            DreamsDimensions.MODID + ":campo_onirico_azul"
                    ),
                    () -> DreamsDimensions.MODID + ":dreamscape",
                    DreamsConfig::isValidIdentifier
            );

    private static final ModConfigSpec.IntValue ANCHORING_TOTEM_RADIUS = BUILDER
            .comment("Raio (em blocos) para procurar ow_anchoring_totem perto da cama/spawn e bloquear teleporte onirico.")
            .defineInRange("anchoring_totem_radius", 8, 1, 64);

    private static Set<ResourceKey<Level>> dreamDimensions = Set.of();
    private static int anchoringTotemRadius = 8;
    private static boolean baked = false;

    public static final ModConfigSpec SPEC = BUILDER.build();

    private DreamsConfig() {}

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        if (!isOwnConfig(event)) return;

        LOGGER.info("[DreamsConfig] Loading config file: {}", event.getConfig().getFileName());
        bake();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        if (!isOwnConfig(event)) return;

        LOGGER.info("[DreamsConfig] Reloading config file: {}", event.getConfig().getFileName());
        bake();
    }

    private static boolean isOwnConfig(ModConfigEvent event) {
        return event.getConfig().getModId().equals(DreamsDimensions.MODID)
                && event.getConfig().getSpec() == SPEC;
    }

    public static boolean isDreamDimension(ResourceKey<Level> dimension) {

        if (!baked) {
            LOGGER.warn("[DreamsConfig] isDreamDimension called before bake. Forcing bake.");
            bake();
        }

        boolean result = dreamDimensions.contains(dimension);

        if (!result) {
            LOGGER.warn(
                    "[DreamsConfig] Dream check FAILED. asked={} bakedSet={}",
                    dimension.identifier(),
                    dreamDimensions.stream().map(key -> key.identifier().toString()).toList()
            );
        }

        return result;
    }

    public static int getAnchoringTotemRadius() {
        if (!baked) {
            LOGGER.warn("[DreamsConfig] getAnchoringTotemRadius called before bake. Forcing bake.");
            bake();
        }
        return anchoringTotemRadius;
    }

    public static Set<ResourceKey<Level>> getDreamDimensions() {
        if (!baked) {
            LOGGER.warn("[DreamsConfig] getDreamDimensions called before bake. Forcing bake.");
            bake();
        }
        return dreamDimensions;
    }

    public static void logResolvedDreamDimensions(MinecraftServer server) {
        Set<ResourceKey<Level>> configured = getDreamDimensions();
        List<String> existing = configured.stream()
                .filter(key -> server.getLevel(key) != null)
                .map(key -> key.identifier().toString())
                .sorted()
                .toList();
        List<String> missing = configured.stream()
                .filter(key -> server.getLevel(key) == null)
                .map(key -> key.identifier().toString())
                .sorted()
                .toList();

        LOGGER.info("[DreamsConfig] dream_dimensions baked={} configured={} existingOnServer={} missingOnServer={}",
                baked,
                configured.stream().map(key -> key.identifier().toString()).sorted().toList(),
                existing,
                missing
        );
    }

    private static boolean isValidIdentifier(Object value) {
        return value instanceof String string && Identifier.tryParse(string) != null;
    }

    private static void bake() {

        LOGGER.info("[DreamsConfig] Raw dream_dimensions from config: {}", DREAM_DIMENSION_IDS.get());

        Set<ResourceKey<Level>> parsed = new HashSet<>();

        for (String entry : DREAM_DIMENSION_IDS.get()) {

            Identifier id = Identifier.tryParse(entry);

            if (id == null) {
                LOGGER.warn("[DreamsConfig] Invalid dream dimension entry: {}", entry);
                continue;
            }

            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, id);
            parsed.add(key);

            LOGGER.info("[DreamsConfig] Registered dream dimension: {}", id);
        }

        dreamDimensions = Set.copyOf(parsed);
        anchoringTotemRadius = ANCHORING_TOTEM_RADIUS.get();
        baked = true;

        LOGGER.info("[DreamsConfig] Dream dimensions baked successfully: {}",
                dreamDimensions.stream().map(key -> key.identifier().toString()).toList()
        );
        LOGGER.info("[DreamsConfig] anchoring_totem_radius={}", anchoringTotemRadius);
    }
}
