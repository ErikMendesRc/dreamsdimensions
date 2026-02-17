package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creative tab do mod, registrada via {@link DeferredRegister} no MOD bus.
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DreamsDimensions.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DREAMS_DIMENSIONS_TAB = CREATIVE_MODE_TABS.register(
            "dreams_dimensions_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.DREAM_DUST.get()))
                    .title(Component.translatable("itemGroup." + DreamsDimensions.MODID + ".dreams_dimensions_tab"))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.DREAM_DUST.get());
                        output.accept(ModItems.ONEIRIC_AWAKENER.get());
                        output.accept(ModItems.OW_ONEIRIC_RESIDUE.get());
                        output.accept(ModItems.OW_REFINED_ONEIRIC_POWDER.get());
                        output.accept(ModItems.OW_STABILIZED_DREAM_FRAGMENT.get());
                        output.accept(ModItems.OW_CONDENSED_DREAM_CRYSTAL.get());
                        output.accept(ModItems.OW_DREAM_BINDING_THREAD.get());
                        output.accept(ModItems.OW_DREAM_CATALYST.get());
                        output.accept(ModItems.OW_STABILIZING_ESSENCE.get());

                        output.accept(ModBlocks.DREAM_GRASS_BLOCK.get());
                        output.accept(ModBlocks.BLUE_DREAM_GRASS.get());
                        output.accept(ModBlocks.DREAM_DIRT_BLOCK.get());
                        output.accept(ModBlocks.BLUE_DREAM_DIRT.get());
                        output.accept(ModBlocks.DREAM_SAND_BLOCK.get());
                        output.accept(ModBlocks.SERENE_STONE_BLOCK.get());
                        output.accept(ModBlocks.BLUE_DREAM_STONE.get());
                        output.accept(ModBlocks.BLUE_DREAM_COBBLESTONE.get());
                        output.accept(ModBlocks.DREAM_SHIMMER_BLOCK.get());
                        output.accept(ModBlocks.OW_DREAM_GLOW_MOSS.get());
                        output.accept(ModBlocks.DREAM_ORE.get());
                        output.accept(ModBlocks.DEEPSLATE_DREAM_ORE.get());
                        output.accept(ModBlocks.DREAM_INFUSED_STONE.get());
                        output.accept(ModBlocks.ONEIRIC_CORE_BLOCK.get());
                        output.accept(ModBlocks.OW_ANCHORING_TOTEM.get());
                        output.accept(ModBlocks.DREAM_FLOWER_BLOCK.get());
                        output.accept(ModBlocks.OW_LUMINA_FLOWER.get());
                        output.accept(ModBlocks.OW_SOMNIFLORA.get());
                        output.accept(ModBlocks.DREAM_LOG_BLOCK.get());
                        output.accept(ModBlocks.OW_SOMNIBARK_LOG.get());
                        output.accept(ModBlocks.DREAM_LEAVES_BLOCK.get());
                        output.accept(ModBlocks.OW_SOMNIBARK_LEAVES.get());
                    })
                    .build()
    );

    private ModCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
