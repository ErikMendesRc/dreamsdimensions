package com.dreamsdimensions.mod.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DreamTransitionScreen extends LevelLoadingScreen {
    private static final int MIN_TICKS = 100;
    private int fadeTicks = 0;

    public DreamTransitionScreen(LevelLoadTracker loadTracker, Reason reason) {
        super(loadTracker, reason);
    }

    @Override
    public void tick() {
        fadeTicks++;
        if (fadeTicks >= MIN_TICKS) {
            super.tick();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gg, mouseX, mouseY, partialTick);

        float progress = Math.min(1.0f, fadeTicks / (float) MIN_TICKS);
        int alpha = ((int) (progress * 255)) << 24;
        gg.fill(0, 0, width, height, alpha);

        gg.drawCenteredString(
                this.font,
                Component.literal("Você está sonhando..."),
                width / 2,
                height / 2,
                0xFFFFFF
        );
    }
}