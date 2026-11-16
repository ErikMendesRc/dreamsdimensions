package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Creative tab do mod, registrada via {@link DeferredRegister} no MOD bus.
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DreamsDimensions.MODID);

    public static final Supplier<CreativeModeTab> DREAMS_DIMENSIONS_TAB = CREATIVE_MODE_TABS.register(
            "dreams_dimensions_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.DREAM_NECKLACE.get()))
                    .title(Component.translatable("itemGroup." + DreamsDimensions.MODID + ".dreams_dimensions_tab"))
                    .displayItems((params, output) -> {
                        // Functional ordering: travel gear, Overworld resources,
                        // construction materials, terrain, then nature/lighting.
                        output.accept(ModItems.DREAM_NECKLACE.get());
                        output.accept(ModItems.ONEIRIC_AWAKENER.get());
                        output.accept(ModItems.CRACKED_AWAKENER.get());
                        output.accept(ModItems.DREAM_DUST.get());
                        output.accept(ModItems.PLUMITE_SHARD.get());
                        output.accept(ModItems.REMINISCENCE.get());
                        output.accept(ModItems.SERENE_VIAL.get());
                        output.accept(ModItems.DREAM_THREAD.get());
                        output.accept(ModItems.DORMANT_EMBER.get());
                        output.accept(ModItems.DREAMY_HERB.get());
                        output.accept(ModItems.RESONANCE_PROBE.get());
                        output.accept(ModItems.INFUSION_ROUTE.get());
                        output.accept(ModItems.MARKED_LINING.get());
                        output.accept(ModItems.DREAM_JOURNAL.get());
                        output.accept(ModItems.INFUSION_LIGHTNESS.get());
                        output.accept(ModItems.INFUSION_VIGIL.get());
                        output.accept(ModItems.PLUME_LINING.get());
                        output.accept(ModItems.VIGIL_TETHER.get());
                        output.accept(ModItems.PLUME_BRIDGE.get());
                        output.accept(ModItems.RETURN_CHARGE.get());
                        output.accept(ModItems.VIGIL_HERB_LEAF.get());
                        output.accept(ModItems.TOASTED_VIGIL_HERB.get());
                        output.accept(ModItems.VIGIL_HERB_SEEDS.get());
                        output.accept(ModBlocks.VIGIL_HERB.get());

                        output.accept(ModBlocks.DREAM_ORE.get());
                        output.accept(ModBlocks.PLUMITE_ORE.get());
                        output.accept(ModBlocks.PLUMITE_BLOCK.get());
                        output.accept(ModBlocks.ONEIRIC_KETTLE.get());
                        output.accept(ModBlocks.DREAM_BEDSIDE.get());
                        output.accept(ModBlocks.DREAMCATCHER.get());
                        output.accept(ModBlocks.DEEP_SLEEP_STONE.get());
                        output.accept(ModBlocks.TRAIL_MARKER.get());
                        output.accept(ModBlocks.DEEPSLATE_DREAM_ORE.get());
                        output.accept(ModBlocks.MARKED_STONE.get());
                        output.accept(ModBlocks.CUT_MARKED_STONE.get());

                        output.accept(ModBlocks.SERENE_STONE_BLOCK.get());
                        output.accept(ModBlocks.DREAM_INFUSED_STONE.get());
                        output.accept(ModBlocks.ONEIRIC_CORE_BLOCK.get());

                        output.accept(ModBlocks.BLUE_DREAM_STONE.get());
                        output.accept(ModBlocks.BLUE_DREAM_COBBLESTONE.get());
                        output.accept(ModBlocks.BLUE_DREAM_GRASS.get());
                        output.accept(ModBlocks.BLUE_DREAM_DIRT.get());

                        output.accept(ModBlocks.DREAM_GRASS_BLOCK.get());
                        output.accept(ModBlocks.DREAM_DIRT_BLOCK.get());
                        output.accept(ModBlocks.DREAM_SAND_BLOCK.get());

                        output.accept(ModBlocks.DREAM_LOG_BLOCK.get());
                        output.accept(ModBlocks.DREAM_LEAVES_BLOCK.get());
                        output.accept(ModBlocks.DREAM_FLOWER_BLOCK.get());

                        output.accept(ModBlocks.DREAM_GLOW_MOSS.get());
                        output.accept(ModBlocks.DREAM_SHIMMER_BLOCK.get());
                    })
                    .build()
    );

    private ModCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
