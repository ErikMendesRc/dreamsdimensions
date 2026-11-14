package com.dreamsdimensions.mod.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.network.chat.Component;

import net.minecraft.client.multiplayer.LevelLoadTracker;

/**
 * Mensagem visual de entrada no sonho. O carregamento de nível nunca é bloqueado pelo fade.
 */
public class DreamTransitionScreen extends LevelLoadingScreen {
    private static final int FADE_TICKS = 100;
    private int fadeTicks = 0;

    public DreamTransitionScreen(LevelLoadTracker levelReceived, Reason reason) {
        super(levelReceived, reason);
    }

    @Override
    public void tick() {
        fadeTicks++;
        super.tick();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        float progress = Math.min(1.0f, fadeTicks / (float) FADE_TICKS);
        int alpha = (int) (progress * 200) << 24;
        graphics.fill(0, 0, width, height, alpha);
        graphics.centeredText(this.font, Component.literal("Você está sonhando..."),
                width / 2, height / 2, 0xFFFFFFFF);
    }
}
