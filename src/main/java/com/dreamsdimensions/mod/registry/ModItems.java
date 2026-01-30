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
     * Poeira dos sonhos usada na receita do Oneiric Awakener e dropada do minério dream_ore.
     */
    public static final DeferredItem<DreamDustItem> DREAM_DUST = ITEMS.registerItem(
            "dream_dust",
            DreamDustItem::new,
            new Item.Properties()
    );

    public static final DeferredItem<OneiricAwakenerItem> ONEIRIC_AWAKENER = ITEMS.registerItem(
            "oneiric_awakener",
            OneiricAwakenerItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)
                    .useCooldown(OneiricAwakenerItem.COOLDOWN_TICKS)
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

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
