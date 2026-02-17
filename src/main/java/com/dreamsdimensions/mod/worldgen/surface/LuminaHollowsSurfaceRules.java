package com.dreamsdimensions.mod.worldgen.surface;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.worldgen.biomes.LuminaHollowsEntry;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.api.SurfaceRuleManager;

/**
 * Surface rule do bioma Lumina Hollows no Overworld.
 */
public final class LuminaHollowsSurfaceRules {
    private LuminaHollowsSurfaceRules() {
    }

    public static void register() {
        SurfaceRules.RuleSource luminaTopRule = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(LuminaHollowsEntry.BIOME_KEY),
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.state(ModBlocks.OW_DREAM_GLOW_MOSS.get().defaultBlockState())
                )
        );

        SurfaceRuleManager.addSurfaceRules(
                SurfaceRuleManager.RuleCategory.OVERWORLD,
                DreamsDimensions.MODID,
                luminaTopRule
        );
    }
}
