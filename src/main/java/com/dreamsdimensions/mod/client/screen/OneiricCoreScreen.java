package com.dreamsdimensions.mod.client.screen;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.menu.OneiricCoreMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Restrained stone/copper/pale-cyan UI, built as pixel art for vanilla GUI scale. */
public final class OneiricCoreScreen extends AbstractContainerScreen<OneiricCoreMenu> {
    private static final Identifier BACKGROUND =
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID,
                    "textures/gui/container/oneiric_core_gui.png");

    public OneiricCoreScreen(OneiricCoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelX = 9;
        this.titleLabelY = 9;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 73;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics,
            int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                this.leftPos, this.topPos, 0, 0, 176, 166, 176, 166);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // Keep title and inventory labels consistent with vanilla fonts.
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF3F5555, false);
        graphics.text(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, 0xFF596260, false);
    }
}
