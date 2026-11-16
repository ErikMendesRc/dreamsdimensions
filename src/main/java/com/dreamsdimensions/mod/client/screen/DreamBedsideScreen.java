package com.dreamsdimensions.mod.client.screen;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.menu.DreamBedsideMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Vanilla+ incubation screen. Input and output icons are synchronized,
 * read-only previews. Buttons execute atomic actions on the server.
 */
public final class DreamBedsideScreen extends AbstractContainerScreen<DreamBedsideMenu> {
    private static final Identifier BACKGROUND =
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID,
                    "textures/gui/container/dream_bedside_gui.png");

    public DreamBedsideScreen(DreamBedsideMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelX = 8;
        this.titleLabelY = 8;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 74;
    }

    private void sendAction(int action) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, action);
        }
    }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(
                Component.translatable("container.dreamsdimensions.bedside.add"),
                button -> sendAction(0)).bounds(leftPos + 9, topPos + 57, 51, 16).build());
        addRenderableWidget(Button.builder(
                Component.translatable("container.dreamsdimensions.bedside.collect"),
                button -> sendAction(1)).bounds(leftPos + 63, topPos + 57, 51, 16).build());
        addRenderableWidget(Button.builder(
                Component.translatable("container.dreamsdimensions.bedside.cancel"),
                button -> sendAction(2)).bounds(leftPos + 117, topPos + 57, 51, 16).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics,
            int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                leftPos, topPos, 0, 0, 176, 166, 176, 166);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xFF453328, false);
        graphics.text(font, playerInventoryTitle,
                inventoryLabelX, inventoryLabelY, 0xFF534235, false);
        graphics.text(font,
                Component.translatable("container.dreamsdimensions.bedside.input"),
                12, 22, 0xFFE9D4AA, false);
        graphics.text(font,
                Component.translatable("container.dreamsdimensions.bedside.output"),
                116, 22, 0xFFE9D4AA, false);

        String kind = switch (menu.room()) {
            case 1 -> "serene";
            case 2 -> "silence";
            case 3 -> "ember";
            case 4 -> "deep";
            case 5 -> "garden";
            case 6 -> "ambiguous";
            default -> "none";
        };
        graphics.text(font, Component.translatable(
                menu.hasBed()
                        ? "container.dreamsdimensions.bedside.room." + kind
                        : "container.dreamsdimensions.bedside.no_bed"),
                13, 48, 0xFFF3D9A1, false);
        graphics.text(font, Component.translatable(
                "container.dreamsdimensions.bedside.counts",
                menu.pending(), menu.capacity(), menu.ready()),
                80, 48, 0xFFFFE3A9, false);
    }
}
