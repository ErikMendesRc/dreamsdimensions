package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.worldgen.decorator.DreamsLeaveVineDecorator;
import com.dreamsdimensions.mod.worldgen.decorator.DreamsTrunkVineDecorator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTreeDecorators {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, DreamsDimensions.MODID);

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<DreamsTrunkVineDecorator>> DREAMS_TRUNK_VINE =
            TREE_DECORATORS.register("dreams_trunk_vine", () -> new TreeDecoratorType<>(DreamsTrunkVineDecorator.CODEC));

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<DreamsLeaveVineDecorator>> DREAMS_LEAVE_VINE =
            TREE_DECORATORS.register("dreams_leave_vine", () -> new TreeDecoratorType<>(DreamsLeaveVineDecorator.CODEC));

    private ModTreeDecorators() {
    }

    public static void register(IEventBus eventBus) {
        TREE_DECORATORS.register(eventBus);
    }
}
