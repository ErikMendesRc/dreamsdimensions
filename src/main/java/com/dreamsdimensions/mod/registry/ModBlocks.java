package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.block.DreamOreBlock;
import com.dreamsdimensions.mod.block.OneiricCoreBlock;
import com.dreamsdimensions.mod.block.VigilHerbBlock;
import com.dreamsdimensions.mod.block.PlumiteBlock;
import com.dreamsdimensions.mod.block.OneiricKettleBlock;
import com.dreamsdimensions.mod.block.TemporaryPlumeBridgeBlock;
import com.dreamsdimensions.mod.block.DreamLeavesBlock;
import com.dreamsdimensions.mod.block.DreamBedsideBlock;
import com.dreamsdimensions.mod.block.TrailMarkerBlock;
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
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.GRASS)
    );

    public static final DeferredBlock<Block> BLUE_DREAM_GRASS = BLOCKS.registerSimpleBlock(
            "blue_dream_grass",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
    );

    public static final DeferredBlock<Block> DREAM_DIRT_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_dirt_block",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .mapColor(MapColor.DIRT)
    );

    public static final DeferredBlock<Block> BLUE_DREAM_DIRT = BLOCKS.registerSimpleBlock(
            "blue_dream_dirt",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
    );

    public static final DeferredBlock<Block> DREAM_SAND_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_sand_block",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
                    .mapColor(MapColor.SAND)
    );

    public static final DeferredBlock<Block> SERENE_STONE_BLOCK = BLOCKS.registerSimpleBlock(
            "serene_stone",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.STONE)
    );

    public static final DeferredBlock<Block> BLUE_DREAM_STONE = BLOCKS.registerSimpleBlock(
            "blue_dream_stone",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> BLUE_DREAM_COBBLESTONE = BLOCKS.registerSimpleBlock(
            "blue_dream_cobblestone",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .requiresCorrectToolForDrops()
    );

    /**
     * F0a / Task 7.3: buildable Overworld vestige stone. No natural generation
     * until Task 7.4; drops only itself when mined with stone-tier pickaxe+.
     */
    public static final DeferredBlock<Block> MARKED_STONE = BLOCKS.registerSimpleBlock(
            "marked_stone",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
                    .mapColor(MapColor.STONE)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
    );

    /** F0b: architectural cut variant; produced 1:1 in a vanilla stonecutter. */
    public static final DeferredBlock<Block> CUT_MARKED_STONE = BLOCKS.registerSimpleBlock(
            "cut_marked_stone",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
                    .mapColor(MapColor.STONE)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DREAM_SHIMMER_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_shimmer_block",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DREAM_GLOW_MOSS = BLOCKS.registerSimpleBlock(
            "dream_glow_moss",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK)
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .lightLevel(state -> 8)
    );

    public static final DeferredBlock<Block> DREAM_ORE = BLOCKS.registerBlock(
            "dream_ore",
            (properties) -> new DreamOreBlock(properties, UniformInt.of(1, 3)),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DEEPSLATE_DREAM_ORE = BLOCKS.registerBlock(
            "deepslate_dream_ore",
            (properties) -> new DreamOreBlock(properties, UniformInt.of(1, 3)),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
                    .strength(4.5F, 3.0F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DREAM_INFUSED_STONE = BLOCKS.registerSimpleBlock(
            "dream_infused_stone",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<OneiricCoreBlock> ONEIRIC_CORE_BLOCK = BLOCKS.registerBlock(
            "oneiric_core_block",
            OneiricCoreBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> DREAM_FLOWER_BLOCK = BLOCKS.registerSimpleBlock(
            "dream_flower_block",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION)
                    .noCollision()
                    .instabreak()
    );

    public static final DeferredBlock<RotatedPillarBlock> DREAM_LOG_BLOCK = BLOCKS.registerBlock(
            "dream_log_block",
            RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
                    .strength(2.0f)
    );

    public static final DeferredBlock<DreamLeavesBlock> DREAM_LEAVES_BLOCK = BLOCKS.registerBlock(
            "dream_leaves_block",
            DreamLeavesBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .noOcclusion()
                    .strength(0.2f)
    );

    public static final DeferredBlock<VigilHerbBlock> VIGIL_HERB = BLOCKS.registerBlock(
            "vigil_herb", VigilHerbBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION)
                    .randomTicks().noCollision().instabreak()
                    .lightLevel(VigilHerbBlock::lightLevel)
    );

    public static final DeferredBlock<Block> PLUMITE_ORE = BLOCKS.registerSimpleBlock(
            "plumite_ore",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                    .mapColor(MapColor.STONE).strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<PlumiteBlock> PLUMITE_BLOCK = BLOCKS.registerBlock(
            "plumite_block", PlumiteBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
                    .strength(2.4f, 6.0f).requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<OneiricKettleBlock> ONEIRIC_KETTLE =
            BLOCKS.registerBlock("oneiric_kettle", OneiricKettleBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .strength(2.5f,6.0f).noOcclusion());

    public static final DeferredBlock<TemporaryPlumeBridgeBlock> TEMPORARY_PLUME_BRIDGE =
            BLOCKS.registerBlock("temporary_plume_bridge", TemporaryPlumeBridgeBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
                            .strength(-1.0f,3600000.0f).noLootTable());

    public static final DeferredBlock<DreamBedsideBlock> DREAM_BEDSIDE =
            BLOCKS.registerBlock("dream_bedside",DreamBedsideBlock::new,
                    ()->BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                            .strength(2.0f,3.0f).noOcclusion());

    public static final DeferredBlock<Block> DREAMCATCHER =
            BLOCKS.registerSimpleBlock("dreamcatcher",
                    ()->BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION)
                            .noCollision().instabreak().noOcclusion());

    public static final DeferredBlock<Block> DEEP_SLEEP_STONE =
            BLOCKS.registerSimpleBlock("deep_sleep_stone",
                    ()->BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                            .requiresCorrectToolForDrops().strength(3.0f,6.0f));

    public static final DeferredBlock<TrailMarkerBlock> TRAIL_MARKER =
            BLOCKS.registerBlock("trail_marker",TrailMarkerBlock::new,
                    ()->BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                            .strength(2.5f,6.0f).lightLevel(state->9).noOcclusion());

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
