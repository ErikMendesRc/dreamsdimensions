package com.dreamsdimensions.mod.content.emissive;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NightEmissiveDebug {
    private static final String DEBUG_KEY = "dreamsdimensions.debug.night_emissive";
    private static final String VERBOSE_KEY = "dreamsdimensions.debug.night_emissive.verbose";
    private static final boolean DEBUG_ENABLED_AT_BOOT = Boolean.getBoolean(DEBUG_KEY);
    private static final boolean VERBOSE_ENABLED_AT_BOOT = DEBUG_ENABLED_AT_BOOT && Boolean.getBoolean(VERBOSE_KEY);
    private static final long CLOCK_THROTTLE_MS = 5_000L;
    private static final long POSITION_THROTTLE_MS = 2_500L;
    private static final long CLIENT_THROTTLE_MS = 2_000L;

    private static final Set<Identifier> TARGET_BLOCKS = Set.of(
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "ow_somniflora"),
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "ow_somnibark_log"),
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "ow_dream_glow_moss"),
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "ow_lumina_flower")
    );

    private static final List<BlockAssetSpec> ASSET_SPECS = List.of(
            new BlockAssetSpec("ow_somniflora", "ow_somniflora_off", "ow_somniflora_on", List.of("lit=false", "lit=true")),
            new BlockAssetSpec("ow_lumina_flower", "ow_lumina_flower_off", "ow_lumina_flower_on", List.of("lit=false", "lit=true")),
            new BlockAssetSpec("ow_dream_glow_moss", "ow_dream_glow_moss_off", "ow_dream_glow_moss_on", List.of("lit=false", "lit=true")),
            new BlockAssetSpec(
                    "ow_somnibark_log",
                    "ow_somnibark_log_off",
                    "ow_somnibark_log_on",
                    List.of("axis=x,lit=false", "axis=y,lit=false", "axis=z,lit=false", "axis=x,lit=true", "axis=y,lit=true", "axis=z,lit=true")
            )
    );

    private static final Map<String, Long> THROTTLE_MAP = new ConcurrentHashMap<>();
    private static final AtomicBoolean ASSET_VALIDATION_DONE = new AtomicBoolean(false);
    private static final AtomicBoolean BLOCK_REGISTRATION_LOGGED = new AtomicBoolean(false);

    private NightEmissiveDebug() {
    }

    public static boolean isEnabled() {
        return DEBUG_ENABLED_AT_BOOT;
    }

    public static boolean isVerbose() {
        return VERBOSE_ENABLED_AT_BOOT;
    }

    public static void logDebugModeBoot() {
        if (!isEnabled()) {
            return;
        }

        DreamsDimensions.LOGGER.info(
                "[NightEmissive] DEBUG ENABLED via -D{}={} (verbose={}, rawDebug='{}', rawVerbose='{}').",
                DEBUG_KEY,
                System.getProperty(DEBUG_KEY),
                isVerbose(),
                System.getProperty(DEBUG_KEY),
                System.getProperty(VERBOSE_KEY)
        );
    }

    public static void logNightEmissiveBlockRegistration(List<Block> blocks) {
        if (!isEnabled() || !BLOCK_REGISTRATION_LOGGED.compareAndSet(false, true)) {
            return;
        }

        DreamsDimensions.LOGGER.info("[NightEmissive] Registered block base={} totalNightBlocks={}", NightEmissiveBlockBase.class.getName(), blocks.size());
        for (Block block : blocks) {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
            boolean hasLit = block.defaultBlockState().hasProperty(NightEmissiveBlockBase.LIT);
            DreamsDimensions.LOGGER.info(
                    "[NightEmissive] block={} class={} hasLitProperty={} defaultState={}",
                    blockId,
                    block.getClass().getName(),
                    hasLit,
                    block.defaultBlockState()
            );
        }
    }

    public static void ensureAssetValidation() {
        if (!isEnabled() || !ASSET_VALIDATION_DONE.compareAndSet(false, true)) {
            return;
        }

        DreamsDimensions.LOGGER.info("[NightEmissive/Assets] Iniciando validação de blockstates/modelos emissivos.");
        for (BlockAssetSpec spec : ASSET_SPECS) {
            validateBlockState(spec);
            validateModel(spec.modelOff(), false);
            validateModel(spec.modelOn(), true);
        }
        DreamsDimensions.LOGGER.info("[NightEmissive/Assets] Validação concluída.");
    }

    public static void logHeartbeat(Level level, String source) {
        if (!isEnabled()) {
            return;
        }

        String dimKey = level.dimension().identifier().toString();
        if (!shouldLog("heartbeat:" + source + ":" + dimKey, CLOCK_THROTTLE_MS) && !isVerbose()) {
            return;
        }

        long absolute = level.getDayTime();
        long modulo = Math.floorMod(absolute, NightTime.DAY_TICKS);
        boolean isNight = NightTime.computeNight(modulo);

        DreamsDimensions.LOGGER.info(
                "[NightEmissive/Heartbeat] src={} dim={} dayTimeAbs={} dayTimeMod={} isNight={}",
                source,
                dimKey,
                absolute,
                modulo,
                isNight
        );
    }

    public static void logInitialSchedule(ServerLevel level, BlockPos pos, BlockState state, Block block, int delay, String reason) {
        if (!isEnabled()) {
            return;
        }

        logClock(level, "scheduleInitial:" + reason);
        DreamsDimensions.LOGGER.info(
                "[NightEmissive/Schedule] reason={} block={} pos={} lit={} dayTime={} delay={}t",
                reason,
                BuiltInRegistries.BLOCK.getKey(block),
                pos,
                state.getValue(NightEmissiveBlockBase.LIT),
                level.getDayTime(),
                delay
        );
    }


    public static void logSkippedNonServerSchedule(Level level, BlockPos pos, Block block, String reason) {
        if (!isEnabled()) {
            return;
        }

        DreamsDimensions.LOGGER.info(
                "[NightEmissive] skipped schedule (non-server level): reason={} block={} pos={} levelClass={} dim={}",
                reason,
                BuiltInRegistries.BLOCK.getKey(block),
                pos,
                level.getClass().getName(),
                level.dimension().identifier()
        );
    }

    public static void logClock(Level level, String source) {
        if (!isEnabled()) {
            return;
        }

        String dimKey = level.dimension().identifier().toString();
        if (!shouldLog("clock:" + dimKey, CLOCK_THROTTLE_MS) && !isVerbose()) {
            return;
        }

        long absolute = level.getDayTime();
        long modulo = Math.floorMod(absolute, NightTime.DAY_TICKS);
        boolean isNight = NightTime.computeNight(modulo);

        DreamsDimensions.LOGGER.info(
                "[NightEmissive/Clock] src={} dim={} dayTimeAbs={} dayTimeMod={} isNight={} window={}..{} fixedTime={} hasSkyLight={}",
                source,
                dimKey,
                absolute,
                modulo,
                isNight,
                NightTime.NIGHT_START,
                NightTime.NIGHT_END,
                level.dimensionType().hasFixedTime(),
                level.dimensionType().hasSkyLight()
        );
    }

    public static void logScheduledTick(BlockState state, ServerLevel level, BlockPos pos, Block block, boolean shouldBeLit, int nextDelay, int flags, boolean changed) {
        if (!isEnabled()) {
            return;
        }
        if (!changed && !isVerbose() && !shouldLogPosition(pos, "scheduledTick.nochange")) {
            return;
        }

        logClock(level, "scheduledTick");
        DreamsDimensions.LOGGER.info(
                "[NightEmissive/Tick] block={} pos={} lit={} shouldBeLit={} changed={} nextDelay={}t flags={} state={}",
                BuiltInRegistries.BLOCK.getKey(block),
                pos,
                state.getValue(NightEmissiveBlockBase.LIT),
                shouldBeLit,
                changed,
                nextDelay,
                flags,
                state
        );

        if (shouldBeLit || changed || isVerbose()) {
            logExpectedVariant(BuiltInRegistries.BLOCK.getKey(block), state.setValue(NightEmissiveBlockBase.LIT, shouldBeLit));
        }
    }

    public static void logSkippedWorldgenUpdate(ServerLevel level, BlockPos pos, Identifier blockId, String chunkStatus, String reason, int nextDelay) {
        if (!isEnabled()) {
            return;
        }

        DreamsDimensions.LOGGER.info(
                "[NightEmissive] skipped update (worldgen or unsafe chunk): reason={} block={} pos={} chunkStatus={} nextDelay={}t dim={}",
                reason,
                blockId,
                pos,
                chunkStatus,
                nextDelay,
                level.dimension().identifier()
        );
    }

    public static void logSetBlockResult(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        if (!isEnabled()) {
            return;
        }

        BlockState afterSet = level.getBlockState(pos);
        DreamsDimensions.LOGGER.info(
                "[NightEmissive/SetBlock] pos={} flags={} old={} new={} afterSet={} matched={}",
                pos,
                flags,
                oldState,
                newState,
                afterSet,
                afterSet.equals(newState)
        );
    }

    public static void logRandomTick(ServerLevel level, BlockPos pos, BlockState state, Block block) {
        if (!isEnabled() || !shouldLogPosition(pos, "randomTick")) {
            return;
        }

        DreamsDimensions.LOGGER.info(
                "[NightEmissive/RandomTick] block={} pos={} lit={} dayTime={}",
                BuiltInRegistries.BLOCK.getKey(block),
                pos,
                state.getValue(NightEmissiveBlockBase.LIT),
                level.getDayTime()
        );
    }

    public static void logClientProbe(Level level, BlockPos pos, BlockState state, Identifier blockId) {
        if (!isEnabled() || level == null) {
            return;
        }
        if (!shouldLog("client.probe", CLIENT_THROTTLE_MS) && !isVerbose()) {
            return;
        }
        if (!TARGET_BLOCKS.contains(blockId) || !state.hasProperty(NightEmissiveBlockBase.LIT)) {
            return;
        }

        long absolute = level.getDayTime();
        long modulo = Math.floorMod(absolute, NightTime.DAY_TICKS);
        boolean expectedNight = NightTime.computeNight(modulo);

        DreamsDimensions.LOGGER.info(
                "[NightEmissive/ClientProbe] block={} pos={} lit(client)={} expectedNight={} dayTimeAbs={} dayTimeMod={} dim={}",
                blockId,
                pos,
                state.getValue(NightEmissiveBlockBase.LIT),
                expectedNight,
                absolute,
                modulo,
                level.dimension().identifier()
        );
    }

    private static boolean shouldLogPosition(BlockPos pos, String kind) {
        return isVerbose() || shouldLog(kind + ":" + pos.asLong(), POSITION_THROTTLE_MS);
    }

    private static boolean shouldLog(String key, long throttleMs) {
        long now = System.currentTimeMillis();
        Long previous = THROTTLE_MAP.put(key, now);
        return previous == null || now - previous >= throttleMs;
    }

    private static void logExpectedVariant(Identifier blockId, BlockState state) {
        String variant;
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            variant = "axis=" + state.getValue(RotatedPillarBlock.AXIS).getName() + ",lit=" + state.getValue(NightEmissiveBlockBase.LIT);
        } else {
            variant = "lit=" + state.getValue(NightEmissiveBlockBase.LIT);
        }

        DreamsDimensions.LOGGER.info(
                "[NightEmissive/Assets] runtime block={} blockstate=assets/{}/blockstates/{}.json expectedVariant='{}'",
                blockId,
                DreamsDimensions.MODID,
                blockId.getPath(),
                variant
        );
    }

    private static void validateBlockState(BlockAssetSpec spec) {
        String path = "assets/" + DreamsDimensions.MODID + "/blockstates/" + spec.blockName() + ".json";
        JsonObject root = readJson(path);
        if (root == null) {
            DreamsDimensions.LOGGER.error("[NightEmissive/Assets] BLOQUEANTE: blockstate ausente em {}", path);
            return;
        }

        JsonObject variants = root.has("variants") ? root.getAsJsonObject("variants") : null;
        if (variants == null) {
            DreamsDimensions.LOGGER.error("[NightEmissive/Assets] BLOQUEANTE: {} sem objeto 'variants'.", path);
            return;
        }

        for (String variant : spec.expectedVariants()) {
            if (!variants.has(variant)) {
                DreamsDimensions.LOGGER.error("[NightEmissive/Assets] BLOQUEANTE: {} sem variant '{}'.", path, variant);
                continue;
            }

            JsonObject variantData = variants.getAsJsonObject(variant);
            String model = variantData.has("model") ? variantData.get("model").getAsString() : "<sem model>";
            DreamsDimensions.LOGGER.info("[NightEmissive/Assets] block={} variant='{}' -> model={}", spec.blockName(), variant, model);
        }
    }

    private static void validateModel(String modelName, boolean expectEmissive) {
        String path = "assets/" + DreamsDimensions.MODID + "/models/block/" + modelName + ".json";
        JsonObject root = readJson(path);
        if (root == null) {
            DreamsDimensions.LOGGER.error("[NightEmissive/Assets] BLOQUEANTE: model ausente em {}", path);
            return;
        }

        JsonArray elements = root.has("elements") ? root.getAsJsonArray("elements") : new JsonArray();
        int emissiveEntries = 0;
        int maxEmission = 0;

        for (JsonElement element : elements) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject elementObj = element.getAsJsonObject();
            if (elementObj.has("light_emission")) {
                int value = elementObj.get("light_emission").getAsInt();
                if (value > 0) {
                    emissiveEntries++;
                    maxEmission = Math.max(maxEmission, value);
                }
            }

            JsonObject faces = elementObj.has("faces") ? elementObj.getAsJsonObject("faces") : null;
            if (faces == null) {
                continue;
            }

            for (Map.Entry<String, JsonElement> faceEntry : faces.entrySet()) {
                if (!faceEntry.getValue().isJsonObject()) {
                    continue;
                }

                JsonObject face = faceEntry.getValue().getAsJsonObject();
                if (face.has("light_emission")) {
                    int value = face.get("light_emission").getAsInt();
                    if (value > 0) {
                        emissiveEntries++;
                        maxEmission = Math.max(maxEmission, value);
                    }
                }
            }
        }

        if (expectEmissive && emissiveEntries == 0) {
            DreamsDimensions.LOGGER.error("[NightEmissive/Assets] BLOQUEANTE: modelo ON {} sem light_emission > 0.", path);
        } else {
            DreamsDimensions.LOGGER.info(
                    "[NightEmissive/Assets] model={} expectEmissive={} emissiveEntries={} maxLightEmission={}",
                    path,
                    expectEmissive,
                    emissiveEntries,
                    maxEmission
            );
        }
    }

    private static JsonObject readJson(String classpathPath) {
        try (InputStream stream = NightEmissiveDebug.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (stream == null) {
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception exception) {
            DreamsDimensions.LOGGER.error("[NightEmissive/Assets] Falha lendo {}: {}", classpathPath, exception.getMessage());
            return null;
        }
    }

    private record BlockAssetSpec(String blockName, String modelOff, String modelOn, List<String> expectedVariants) {
    }
}
