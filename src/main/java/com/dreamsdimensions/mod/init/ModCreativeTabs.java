package com.dreamsdimensions.mod.init;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DreamsDimensions.MODID);

    public static final Supplier<CreativeModeTab> DREAMS_DIMENSIONS_TAB = CREATIVE_MODE_TABS.register("dreams_dimensions_tab", () ->
            CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.DREAM_DUST.get()))
                    .title(Component.translatable("itemGroup." + DreamsDimensions.MODID + ".dreams_dimensions_tab"))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.DREAM_DUST.get());
                        output.accept(ModItems.ONEIRIC_AWAKENER.get());

                        output.accept(ModBlocks.DREAM_GRASS_BLOCK.get());
                        output.accept(ModBlocks.SERENE_STONE_BLOCK.get());
                        output.accept(ModBlocks.DREAM_ORE.get());
                        output.accept(ModBlocks.DREAM_DIRT_BLOCK);
                        output.accept(ModBlocks.DREAM_FLOWER_BLOCK);
                        output.accept(ModBlocks.DREAM_LOG_BLOCK);
                        output.accept(ModBlocks.DREAM_LEAVES_BLOCK);

                        // Outros futuros itens/blocos
                    })
                    .build()
    );

    // 3. Registra o DeferredRegister no event bus
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}