package com.dreamsdimensions.mod.worldgen.terrablender;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.worldgen.biome.DDBiomes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

/**
 * Region TerraBlender responsável por inserir lumina_hollows no source do Overworld.
 *
 * <p>Perfis disponíveis:</p>
 * <ul>
 *   <li>TESTING_EASY_SPAWN: parâmetros amplos para validar presença rapidamente.</li>
 *   <li>PRODUCTION_RARE: parâmetros estreitos para deixar o bioma raro.</li>
 * </ul>
 */
public class LuminaHollowsOverworldRegion extends Region {
    public static final Identifier LOCATION = Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "overworld_lumina_hollows");

    /**
     * Perfil ativo: deixe TESTING_EASY_SPAWN para validação inicial e depois troque para PRODUCTION_RARE.
     */
    private static final SpawnProfile ACTIVE_PROFILE = SpawnProfile.TESTING_EASY_SPAWN;

    public LuminaHollowsOverworldRegion(int weight) {
        super(LOCATION, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        DreamsDimensions.LOGGER.info("[TerraBlender] addBiomes executando para region={} perfil={}", LOCATION, ACTIVE_PROFILE);

        switch (ACTIVE_PROFILE) {
            case TESTING_EASY_SPAWN -> registerTestingProfile(mapper);
            case PRODUCTION_RARE -> registerRareProfile(mapper);
        }
    }

    private void registerTestingProfile(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // Perfil amplo: facilita encontrar o bioma em mundo de teste.
        addBiome(
                mapper,
                ParameterUtils.Temperature.span(ParameterUtils.Temperature.COOL, ParameterUtils.Temperature.WARM),
                ParameterUtils.Humidity.span(ParameterUtils.Humidity.NEUTRAL, ParameterUtils.Humidity.HUMID),
                ParameterUtils.Continentalness.span(ParameterUtils.Continentalness.COAST, ParameterUtils.Continentalness.FAR_INLAND),
                ParameterUtils.Erosion.span(ParameterUtils.Erosion.EROSION_1, ParameterUtils.Erosion.EROSION_5),
                ParameterUtils.Weirdness.FULL_RANGE.parameter(),
                ParameterUtils.Depth.SURFACE.parameter(),
                0.0F,
                DDBiomes.LUMINA_HOLLOWS
        );
    }

    private void registerRareProfile(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // Perfil estreito: raro, porém ainda plausível em seeds comuns.
        addBiome(
                mapper,
                ParameterUtils.Temperature.NEUTRAL.parameter(),
                ParameterUtils.Humidity.HUMID.parameter(),
                ParameterUtils.Continentalness.span(ParameterUtils.Continentalness.MID_INLAND, ParameterUtils.Continentalness.FAR_INLAND),
                ParameterUtils.Erosion.EROSION_3.parameter(),
                ParameterUtils.Weirdness.MID_SLICE_NORMAL_ASCENDING.parameter(),
                ParameterUtils.Depth.SURFACE.parameter(),
                0.0F,
                DDBiomes.LUMINA_HOLLOWS
        );
    }

    public enum SpawnProfile {
        TESTING_EASY_SPAWN,
        PRODUCTION_RARE
    }
}
