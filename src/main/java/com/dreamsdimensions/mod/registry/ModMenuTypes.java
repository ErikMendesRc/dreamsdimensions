package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.menu.OneiricCoreMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** A per-player, no-block-entity crafting menu for the original core block. */
public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, DreamsDimensions.MODID);

    public static final Supplier<MenuType<OneiricCoreMenu>> ONEIRIC_CORE =
            MENUS.register("oneiric_core", () ->
                    new MenuType<>(OneiricCoreMenu::new, FeatureFlags.VANILLA_SET));

    private ModMenuTypes() {}

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
