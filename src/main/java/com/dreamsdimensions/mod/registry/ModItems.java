package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.item.DreamDustItem;
import com.dreamsdimensions.mod.item.OneiricAwakenerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro de itens do mod via {@link DeferredRegister}.
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(DreamsDimensions.MODID);

    /**
     * Poeira dos sonhos usada na receita do Oneiric Awakener e dropada do minério ow_dream_ore.
     */
    public static final DeferredItem<DreamDustItem> DREAM_DUST = ITEMS.registerItem(
            "ow_dream_dust",
            DreamDustItem::new,
            Item.Properties::new
    );

    public static final DeferredItem<OneiricAwakenerItem> ONEIRIC_AWAKENER = ITEMS.registerItem(
            "ow_oneiric_awakener",
            OneiricAwakenerItem::new,
            () -> new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)
                    .useCooldown(OneiricAwakenerItem.COOLDOWN_TICKS)
    );

    public static final DeferredItem<Item> OW_ONEIRIC_RESIDUE = ITEMS.registerSimpleItem(
            "ow_oneiric_residue"
    );

    public static final DeferredItem<Item> OW_REFINED_ONEIRIC_POWDER = ITEMS.registerSimpleItem(
            "ow_refined_oneiric_powder"
    );

    public static final DeferredItem<Item> OW_STABILIZED_DREAM_FRAGMENT = ITEMS.registerSimpleItem(
            "ow_stabilized_dream_fragment"
    );

    public static final DeferredItem<Item> OW_CONDENSED_DREAM_CRYSTAL = ITEMS.registerSimpleItem(
            "ow_condensed_dream_crystal"
    );

    public static final DeferredItem<Item> OW_DREAM_BINDING_THREAD = ITEMS.registerSimpleItem(
            "ow_dream_binding_thread"
    );

    public static final DeferredItem<Item> OW_DREAM_CATALYST = ITEMS.registerSimpleItem(
            "ow_dream_catalyst"
    );

    public static final DeferredItem<Item> OW_STABILIZING_ESSENCE = ITEMS.registerSimpleItem(
            "ow_stabilizing_essence"
    );

    public static final DeferredItem<BlockItem> DREAM_GRASS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_GRASS_BLOCK
    );

    public static final DeferredItem<BlockItem> BLUE_DREAM_GRASS_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.BLUE_DREAM_GRASS
    );

    public static final DeferredItem<BlockItem> DREAM_DIRT_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_DIRT_BLOCK
    );

    public static final DeferredItem<BlockItem> BLUE_DREAM_DIRT_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.BLUE_DREAM_DIRT
    );

    public static final DeferredItem<BlockItem> DREAM_SAND_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_SAND_BLOCK
    );

    public static final DeferredItem<BlockItem> DREAM_FLOWER_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_FLOWER_BLOCK
    );


    public static final DeferredItem<BlockItem> OW_LUMINA_FLOWER_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.OW_LUMINA_FLOWER
    );

    public static final DeferredItem<BlockItem> OW_SOMNIFLORA_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.OW_SOMNIFLORA
    );

    public static final DeferredItem<BlockItem> DREAM_LOG_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_LOG_BLOCK
    );

    public static final DeferredItem<BlockItem> DREAM_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_LEAVES_BLOCK
    );

    public static final DeferredItem<BlockItem> SERENE_STONE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.SERENE_STONE_BLOCK
    );

    public static final DeferredItem<BlockItem> BLUE_DREAM_STONE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.BLUE_DREAM_STONE
    );

    public static final DeferredItem<BlockItem> BLUE_DREAM_COBBLESTONE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.BLUE_DREAM_COBBLESTONE
    );

    public static final DeferredItem<BlockItem> DREAM_SHIMMER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_SHIMMER_BLOCK
    );

    public static final DeferredItem<BlockItem> DREAM_GLOW_MOSS_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_GLOW_MOSS
    );

    public static final DeferredItem<BlockItem> DREAM_ORE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_ORE
    );

    public static final DeferredItem<BlockItem> DEEPSLATE_DREAM_ORE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DEEPSLATE_DREAM_ORE
    );

    public static final DeferredItem<BlockItem> DREAM_INFUSED_STONE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_INFUSED_STONE
    );

    public static final DeferredItem<BlockItem> ONEIRIC_CORE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.ONEIRIC_CORE_BLOCK
    );

    public static final DeferredItem<BlockItem> OW_ANCHORING_TOTEM_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.OW_ANCHORING_TOTEM
    );

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
