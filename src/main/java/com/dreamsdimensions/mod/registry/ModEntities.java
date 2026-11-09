package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.entity.FragmentoErranteEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

/** Registers the first oniric mob, without enabling natural spawning. */
@EventBusSubscriber(modid = DreamsDimensions.MODID)
public final class ModEntities {
    public static final DeferredRegister.Entities ENTITIES =
            DeferredRegister.createEntities(DreamsDimensions.MODID);

    public static final Supplier<EntityType<FragmentoErranteEntity>> FRAGMENTO_ERRANTE =
            ENTITIES.registerEntityType("fragmento_errante", FragmentoErranteEntity::new,
                    MobCategory.CREATURE,
                    builder -> builder.sized(0.85F, 1.55F)
                            .eyeHeight(1.30F)
                            .clientTrackingRange(8)
                            .updateInterval(3));

    private ModEntities() {}

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }

    @SubscribeEvent
    public static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(FRAGMENTO_ERRANTE.get(), FragmentoErranteEntity.createAttributes().build());
    }
}
