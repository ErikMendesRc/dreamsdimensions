package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.block.entity.OwLuminaFlowerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DreamsDimensions.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OwLuminaFlowerBlockEntity>> OW_LUMINA_FLOWER =
            BLOCK_ENTITIES.register("ow_lumina_flower",
                    () -> new BlockEntityType<>(OwLuminaFlowerBlockEntity::new, ModBlocks.OW_LUMINA_FLOWER.get())
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
