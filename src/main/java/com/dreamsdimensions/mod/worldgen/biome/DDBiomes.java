package com.dreamsdimensions.mod.worldgen.biome;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class DDBiomes {

    public static final ResourceKey<Biome> LUMINA_HOLLOWS = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "lumina_hollows")
    );

    public static void bootstrap(BootstrapContext<Biome> context) {
        context.register(LUMINA_HOLLOWS, luminaHollows(context));
    }

    public static void globalOverworldGeneration(BiomeGenerationSettings.Builder builder) {
        BiomeDefaultFeatures.addDefaultCarversAndLakes(builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(builder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(builder);
        BiomeDefaultFeatures.addDefaultSprings(builder);
        BiomeDefaultFeatures.addSurfaceFreezing(builder);
    }

    public static Biome luminaHollows(BootstrapContext<Biome> context) {
        // Spawns
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(spawnBuilder);
        BiomeDefaultFeatures.commonSpawns(spawnBuilder);

        // Generation
        BiomeGenerationSettings.Builder genBuilder =
                new BiomeGenerationSettings.Builder(
                        context.lookup(Registries.PLACED_FEATURE),
                        context.lookup(Registries.CONFIGURED_CARVER)
                );

        // Mesma ordem vanilla
        globalOverworldGeneration(genBuilder);
        BiomeDefaultFeatures.addMossyStoneBlock(genBuilder);
        BiomeDefaultFeatures.addFerns(genBuilder);
        BiomeDefaultFeatures.addForestFlowers(genBuilder);

        genBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_PLAINS);

        BiomeDefaultFeatures.addDefaultMushrooms(genBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(genBuilder, true);

        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .waterColor(0xE82E3B)                 // obrigatório
                // opcionais (use só se quiser forçar cor fixa):
                .foliageColorOverride(0x84A57D)
                .dryFoliageColorOverride(0x7EA07A)
                .grassColorOverride(0x7EA07A)
                .grassColorModifier(BiomeSpecialEffects.GrassColorModifier.NONE)
                .build();

        // Biome final (PRECISA buildar)
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.7f)
                .downfall(0.8f)
                .specialEffects(effects)
                .generationSettings(genBuilder.build())
                .mobSpawnSettings(spawnBuilder.build())
                .build();
    }
}