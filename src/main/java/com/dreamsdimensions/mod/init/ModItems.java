package com.dreamsdimensions.mod.init;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.item.OneiricAwakenerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(DreamsDimensions.MODID);

    // --- Itens Simples ---
    public static final DeferredItem<Item> DREAM_DUST = ITEMS.registerSimpleItem(
            "dream_dust",
            new Item.Properties()
    );

    // --- Itens Customizados ---
    public static final DeferredItem<OneiricAwakenerItem> ONEIRIC_AWAKENER = ITEMS.registerItem(
            "oneiric_awakener",
            OneiricAwakenerItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)
                    .useCooldown(100)
    );

    // --- BlockItems ---
    public static final DeferredItem<BlockItem> DREAM_GRASS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_GRASS_BLOCK
    );

    public static final DeferredItem<BlockItem> DREAM_DIRT_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_DIRT_BLOCK
    );


    // --- Flores ---
    public static final DeferredItem<BlockItem> DREAM_FLOWER_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_FLOWER_BLOCK
    );

    // --- Arvores ---

    public static final DeferredItem<BlockItem> DREAM_LOG_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_LOG_BLOCK
    );

    public static final DeferredItem<BlockItem> DREAM_LEAVES_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_LEAVES_BLOCK
    );

    public static final DeferredItem<BlockItem> SERENE_STONE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.SERENE_STONE_BLOCK
            //, new Item.Properties() // Opcional
    );

    public static final DeferredItem<BlockItem> DREAM_ORE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.DREAM_ORE
            //, new Item.Properties() // Opcional
    );


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}