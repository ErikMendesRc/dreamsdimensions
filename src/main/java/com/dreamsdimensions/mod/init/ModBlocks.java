package com.dreamsdimensions.mod.init;

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

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(DreamsDimensions.MODID);

    // --- Blocos Simples ---
    public static final DeferredBlock<Block> DREAM_GRASS_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_grass_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.GRASS)
    );

    public static final DeferredBlock<Block> DREAM_DIRT_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_dirt_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .mapColor(MapColor.DIRT)
    );

    public static final DeferredBlock<Block> SERENE_STONE_BLOCK = BLOCKS.registerSimpleBlock(
            "serene_stone",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.STONE)
    );

    // --- Blocos Customizados ---
    public static final DeferredBlock<Block> DREAM_ORE = BLOCKS.registerBlock(
            "dream_ore",
            (properties) -> new DreamOreBlock(properties, UniformInt.of(1, 3)),
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
    );

    // --- Bloco de flor customizada ---
    public static final DeferredBlock<Block> DREAM_FLOWER_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_flower_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION)
                    .noCollission()
                    .instabreak()
    );

    // --- Tronco e folhas para árvore customizada ---
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

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}