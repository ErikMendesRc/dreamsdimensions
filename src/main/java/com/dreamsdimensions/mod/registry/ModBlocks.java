package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.block.DreamOreBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro de blocos usando {@link DeferredRegister}.
 * <p>
 * Segue o padrão recomendado nos javadocs de {@link DeferredRegister} e
 * {@link net.neoforged.neoforge.registries.DeferredBlock} para registro no MOD bus.
 * </p>
 */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(DreamsDimensions.MODID);

    public static final DeferredBlock<Block> DREAM_GRASS_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_grass_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.GRASS)
    );

    public static final DeferredBlock<Block> BLUE_DREAM_GRASS = BLOCKS.registerSimpleBlock(
            "blue_dream_grass",
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
    );

    public static final DeferredBlock<Block> DREAM_DIRT_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_dirt_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .mapColor(MapColor.DIRT)
    );

    public static final DeferredBlock<Block> BLUE_DREAM_DIRT = BLOCKS.registerSimpleBlock(
            "blue_dream_dirt",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
    );

    public static final DeferredBlock<Block> DREAM_SAND_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_sand_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
                    .mapColor(MapColor.SAND)
    );

    public static final DeferredBlock<Block> SERENE_STONE_BLOCK = BLOCKS.registerSimpleBlock(
            "serene_stone",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.STONE)
    );

    public static final DeferredBlock<Block> BLUE_DREAM_STONE = BLOCKS.registerSimpleBlock(
            "blue_dream_stone",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> BLUE_DREAM_COBBLESTONE = BLOCKS.registerSimpleBlock(
            "blue_dream_cobblestone",
            BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DREAM_SHIMMER_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_shimmer_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DREAM_GLOW_MOSS = BLOCKS.registerSimpleBlock(
            "dream_glow_moss",
            BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .lightLevel(state -> 8)
    );

    public static final DeferredBlock<Block> DREAM_ORE = BLOCKS.registerBlock(
            "dream_ore",
            (properties) -> new DreamOreBlock(properties, UniformInt.of(1, 3)),
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DREAM_FLOWER_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_flower_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION)
                    .noCollission()
                    .instabreak()
    );

    public static final DeferredBlock<RotatedPillarBlock> DREAM_LOG_BLOCK = BLOCKS.registerBlock(
            "dream_log_block",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                    .strength(2.0f)
    );

    public static final DeferredBlock<Block> DREAM_LEAVES_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_leaves_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .noOcclusion()
                    .strength(0.2f)
    );

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
