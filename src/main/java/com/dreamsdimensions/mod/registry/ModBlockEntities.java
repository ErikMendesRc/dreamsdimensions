package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.block.entity.OneiricKettleBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Persistent crafting state for the twenty-second kettle brew. */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DreamsDimensions.MODID);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<OneiricKettleBlockEntity>> KETTLE =
            TYPES.register("oneiric_kettle", () -> BlockEntityType.Builder.of(
                    OneiricKettleBlockEntity::new, ModBlocks.ONEIRIC_KETTLE.get()).build(null));
    private ModBlockEntities() {}
    public static void register(IEventBus bus) {TYPES.register(bus);}
}
