package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.entity.FragmentoErranteEntity;
import com.dreamsdimensions.mod.entity.VigiaAdormecidoEntity;
import com.dreamsdimensions.mod.entity.VigiaIslandHabitat;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import com.dreamsdimensions.mod.entity.FragmentoIslandHabitat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

/** Registers the Fragmento Errante and its island-only natural spawn rule. */
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

    public static final Supplier<EntityType<VigiaAdormecidoEntity>> VIGIA_ADORMECIDO =
            ENTITIES.registerEntityType("vigia_adormecido", VigiaAdormecidoEntity::new,
                    MobCategory.CREATURE,
                    builder -> builder.sized(1.15F, 2.88F)
                            .eyeHeight(2.55F)
                            .clientTrackingRange(8)
                            .updateInterval(3));

    private ModEntities() {}

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(FRAGMENTO_ERRANTE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FragmentoIslandHabitat::maySpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(VIGIA_ADORMECIDO.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VigiaIslandHabitat::maySpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(FRAGMENTO_ERRANTE.get(), FragmentoErranteEntity.createAttributes().build());
        event.put(VIGIA_ADORMECIDO.get(), VigiaAdormecidoEntity.createAttributes().build());
    }
}
