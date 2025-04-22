package com.dreamsdimensions.mod.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BooleanSupplier;

@OnlyIn(Dist.CLIENT)
public class DreamTransitionScreen extends ReceivingLevelScreen {
    private static final int MIN_TICKS = 100;

    private int fadeTicks = 0;

    public DreamTransitionScreen(BooleanSupplier levelReceived, Reason reason) {
        super(levelReceived, reason);
    }

    @Override
    public void tick() {
        fadeTicks++;

        if (fadeTicks >= MIN_TICKS) {
            super.tick();
        }
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        // desenha o pano de fundo padrão (panorama/blur etc)
        super.renderBackground(gg, mouseX, mouseY, partialTick);

        // overlay de fade
        float progress = Math.min(1.0f, fadeTicks / (float) MIN_TICKS);
        int alpha = (int)(progress * 255) << 24;
        gg.fill(0, 0, width, height, alpha);

        // seu texto
        gg.drawCenteredString(
                this.font,
                Component.literal("Você está sonhando..."),
                width  / 2,
                height / 2,
                0xFFFFFF
        );
    }
}