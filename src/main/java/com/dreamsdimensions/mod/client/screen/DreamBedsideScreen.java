package com.dreamsdimensions.mod.client.screen;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.menu.DreamBedsideMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Task 9.5.13: compact Vanilla+ GUI using actual vanilla inventory slots.
 * No buttons or custom item interaction packets; labels never overlap slots.
 */
public final class DreamBedsideScreen extends AbstractContainerScreen<DreamBedsideMenu> {
    private static final Identifier BACKGROUND =
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID,
                    "textures/gui/container/dream_bedside_gui.png");

    public DreamBedsideScreen(DreamBedsideMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelY = 7;
        this.inventoryLabelX = 9;
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
        // Center the heading rather than assuming its localized text width.
        graphics.text(font, title, (imageWidth - font.width(title)) / 2, titleLabelY,
                0xFFF3D8AA, false);
        Component input = Component.translatable("container.dreamsdimensions.bedside.input");
        Component output = Component.translatable("container.dreamsdimensions.bedside.output");
        graphics.text(font, input, 42 - font.width(input) / 2, 23,
                0xFFDBB67A, false);
        graphics.text(font, output, 134 - font.width(output) / 2, 23,
                0xFFDBB67A, false);

        String name = switch (menu.room()) {
            case 1 -> "serene";
            case 2 -> "silence";
            case 3 -> "ember";
            case 4 -> "deep";
            case 5 -> "garden";
            case 6 -> "ambiguous";
            default -> "none";
        };
        Component room = Component.translatable(
                menu.hasBed() ? "container.dreamsdimensions.bedside.room." + name
                        : "container.dreamsdimensions.bedside.no_bed");
        Component state = Component.translatable("container.dreamsdimensions.bedside.state",
                room, menu.pending(), menu.capacity(), menu.ready());

        int color = !menu.canEdit() ? 0xFFED9A88
                : !menu.hasBed() || menu.room() == 6 || menu.room() == 0
                        ? 0xFFF2AF70 : menu.ready() > 0 ? 0xFF8BE9C2 : 0xFFE9D5AC;
        graphics.text(font, state, (imageWidth - font.width(state)) / 2,
                60, color, false);
        graphics.text(font, playerInventoryTitle,
                inventoryLabelX, inventoryLabelY, 0xFFE7CC9D, false);
    }
}
