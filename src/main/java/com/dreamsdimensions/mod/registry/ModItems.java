package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.item.OneiricAwakenerItem;
import com.dreamsdimensions.mod.item.DreamInfusionItem;
import com.dreamsdimensions.mod.item.VigilTetherItem;
import com.dreamsdimensions.mod.item.PlumeBridgeItem;
import com.dreamsdimensions.mod.item.ResonanceProbeItem;
import com.dreamsdimensions.mod.item.DreamJournalItem;
import com.dreamsdimensions.mod.item.CrackedAwakenerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemLore;
import java.util.List;
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
    public static final DeferredItem<Item> DREAM_DUST = ITEMS.registerItem(
            "dream_dust",
            Item::new,
            Item.Properties::new
    );

    public static final DeferredItem<Item> DREAM_NECKLACE = ITEMS.registerItem(
            "dream_necklace",
            Item::new,
            () -> new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
                    .component(DataComponents.LORE, new ItemLore(List.of(
                            Component.translatable("tooltip.dreamsdimensions.dream_necklace.line1")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.dreamsdimensions.dream_necklace.line2")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.dreamsdimensions.dream_necklace.line3")
                                    .withStyle(ChatFormatting.DARK_PURPLE)
                    )))
    );

    public static final DeferredItem<OneiricAwakenerItem> ONEIRIC_AWAKENER = ITEMS.registerItem(
            "oneiric_awakener",
            OneiricAwakenerItem::new,
            () -> new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)
                    .useCooldown(OneiricAwakenerItem.COOLDOWN_TICKS / 20.0F)
    );

    /** Emergency return: found only in recovery ruin chests, never crafted. */
    public static final DeferredItem<CrackedAwakenerItem> CRACKED_AWAKENER = ITEMS.registerItem(
            "cracked_awakener",
            CrackedAwakenerItem::new,
            () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
                    .useCooldown(OneiricAwakenerItem.COOLDOWN_TICKS / 20.0F)
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

    public static final DeferredItem<BlockItem> MARKED_STONE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.MARKED_STONE
    );

    public static final DeferredItem<BlockItem> CUT_MARKED_STONE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.CUT_MARKED_STONE
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

    public static final DeferredItem<BlockItem> VIGIL_HERB_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VIGIL_HERB
    );

    public static final DeferredItem<com.dreamsdimensions.mod.item.VigilHerbSeedsItem> VIGIL_HERB_SEEDS = ITEMS.registerItem(
            "vigil_herb_seeds", com.dreamsdimensions.mod.item.VigilHerbSeedsItem::new,
            Item.Properties::new
    );

    public static final DeferredItem<Item> VIGIL_HERB_LEAF = ITEMS.registerItem(
            "vigil_herb_leaf", Item::new, Item.Properties::new
    );

    public static final DeferredItem<Item> TOASTED_VIGIL_HERB = ITEMS.registerItem(
            "toasted_vigil_herb", Item::new, Item.Properties::new
    );

    public static final DeferredItem<Item> RETURN_CHARGE = ITEMS.registerItem(
            "return_charge", Item::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<BlockItem> PLUMITE_ORE_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.PLUMITE_ORE);

    public static final DeferredItem<BlockItem> PLUMITE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.PLUMITE_BLOCK);

    public static final DeferredItem<Item> PLUMITE_SHARD = ITEMS.registerItem(
            "plumite_shard", Item::new, Item.Properties::new);

    /** Exclusive return from dream exploration, never the first Awakener. */
    public static final DeferredItem<Item> REMINISCENCE = ITEMS.registerItem(
            "reminiscence", Item::new,
            () -> new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<BlockItem> ONEIRIC_KETTLE_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.ONEIRIC_KETTLE);

    public static final DeferredItem<DreamInfusionItem> INFUSION_LIGHTNESS = ITEMS.registerItem(
            "infusion_lightness", p -> new DreamInfusionItem(true,p),
            () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<DreamInfusionItem> INFUSION_VIGIL = ITEMS.registerItem(
            "infusion_vigil", p -> new DreamInfusionItem(false,p),
            () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> PLUME_LINING = ITEMS.registerItem(
            "plume_lining",Item::new,
            () -> new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<VigilTetherItem> VIGIL_TETHER = ITEMS.registerItem(
            "vigil_tether",VigilTetherItem::new,
            () -> new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<PlumeBridgeItem> PLUME_BRIDGE = ITEMS.registerItem(
            "plume_bridge",PlumeBridgeItem::new,
            () -> new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

    // Incubation materials: each silhouette represents the actual object.
    public static final DeferredItem<BlockItem> DREAM_BEDSIDE_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.DREAM_BEDSIDE);
    public static final DeferredItem<BlockItem> DREAMCATCHER_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.DREAMCATCHER);
    public static final DeferredItem<BlockItem> DEEP_SLEEP_STONE_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.DEEP_SLEEP_STONE);
    public static final DeferredItem<BlockItem> TRAIL_MARKER_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.TRAIL_MARKER);

    public static final DeferredItem<Item> SERENE_VIAL =
            ITEMS.registerItem("serene_vial",Item::new,
                    ()->new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> DREAM_THREAD =
            ITEMS.registerItem("dream_thread",Item::new,Item.Properties::new);
    public static final DeferredItem<Item> DORMANT_EMBER =
            ITEMS.registerItem("dormant_ember",Item::new,Item.Properties::new);
    public static final DeferredItem<Item> DREAMY_HERB =
            ITEMS.registerItem("dreamy_herb",Item::new,Item.Properties::new);

    public static final DeferredItem<ResonanceProbeItem> RESONANCE_PROBE =
            ITEMS.registerItem("resonance_probe",ResonanceProbeItem::new,
                    ()->new Item.Properties().durability(16).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> MARKED_LINING =
            ITEMS.registerItem("marked_lining",Item::new,
                    ()->new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<DreamJournalItem> DREAM_JOURNAL =
            ITEMS.registerItem("dream_journal",DreamJournalItem::new,
                    ()->new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<DreamInfusionItem> INFUSION_ROUTE =
            ITEMS.registerItem("infusion_route",p->new DreamInfusionItem(2,p),
                    ()->new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
