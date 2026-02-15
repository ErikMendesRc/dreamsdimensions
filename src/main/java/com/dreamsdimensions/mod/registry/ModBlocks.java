package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.block.DreamOreBlock;
import com.dreamsdimensions.mod.block.OwLuminaFlowerBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireflyBushBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
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
            "ds_dream_grass",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.GRASS)
    );

    public static final DeferredBlock<Block> BLUE_DREAM_GRASS = BLOCKS.registerSimpleBlock(
            "az_dream_grass",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
    );

    public static final DeferredBlock<Block> DREAM_DIRT_BLOCK = BLOCKS.registerSimpleBlock(
            "ds_dream_dirt",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .mapColor(MapColor.DIRT)
    );

    public static final DeferredBlock<Block> BLUE_DREAM_DIRT = BLOCKS.registerSimpleBlock(
            "az_dream_dirt",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
    );

    public static final DeferredBlock<Block> DREAM_SAND_BLOCK = BLOCKS.registerSimpleBlock(
            "ds_dream_sand",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
                    .mapColor(MapColor.SAND)
    );

    public static final DeferredBlock<Block> SERENE_STONE_BLOCK = BLOCKS.registerSimpleBlock(
            "ds_serene_stone",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.STONE)
    );

    public static final DeferredBlock<Block> BLUE_DREAM_STONE = BLOCKS.registerSimpleBlock(
            "az_dream_stone",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> BLUE_DREAM_COBBLESTONE = BLOCKS.registerSimpleBlock(
            "az_dream_cobblestone",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DREAM_SHIMMER_BLOCK = BLOCKS.registerSimpleBlock(
            "ds_dream_shimmer_block",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DREAM_GLOW_MOSS = BLOCKS.registerSimpleBlock(
            "ds_dream_glow_moss",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .lightLevel(state -> 8)
    );

    public static final DeferredBlock<Block> DREAM_ORE = BLOCKS.registerBlock(
            "ow_dream_ore",
            (properties) -> new DreamOreBlock(properties, UniformInt.of(1, 3)),
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DEEPSLATE_DREAM_ORE = BLOCKS.registerBlock(
            "ow_deepslate_dream_ore",
            (properties) -> new DreamOreBlock(properties, UniformInt.of(1, 3)),
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
                    .strength(4.5F, 3.0F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DREAM_INFUSED_STONE = BLOCKS.registerSimpleBlock(
            "ow_dream_infused_stone",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> ONEIRIC_CORE_BLOCK = BLOCKS.registerSimpleBlock(
            "ow_oneiric_core_block",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> OW_ANCHORING_TOTEM = BLOCKS.registerSimpleBlock(
            "ow_anchoring_totem",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DREAM_FLOWER_BLOCK = BLOCKS.registerSimpleBlock(
            "ds_dream_flower",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION)
                    .instabreak()
    );

    public static final DeferredBlock<OwLuminaFlowerBlock> OW_LUMINA_FLOWER = BLOCKS.registerBlock(
            "ow_lumina_flower",
            OwLuminaFlowerBlock::new,
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION)
                    .instabreak()
                    .noCollision()
                    .sound(SoundType.GRASS)
    );

    public static final DeferredBlock<FireflyBushBlock> OW_SOMNIFLORA = BLOCKS.registerBlock(
            "ow_somniflora",
            FireflyBushBlock::new,
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.FIREFLY_BUSH)
    );

    public static final DeferredBlock<RotatedPillarBlock> DREAM_LOG_BLOCK = BLOCKS.registerBlock(
            "ds_dream_log",
            RotatedPillarBlock::new,
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                    .strength(2.0f)
    );

    public static final DeferredBlock<RotatedPillarBlock> OW_SOMNIBARK_LOG = BLOCKS.registerBlock(
            "ow_somnibark_log",
            RotatedPillarBlock::new,
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                    .strength(2.0f)
    );

    public static final DeferredBlock<Block> DREAM_LEAVES_BLOCK = BLOCKS.registerSimpleBlock(
            "ds_dream_leaves",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .noOcclusion()
                    .strength(0.2f)
    );

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
