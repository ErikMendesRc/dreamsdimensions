package com.dreamsdimensions.mod.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BooleanSupplier;

/**
 * Tela de transição exibida ao entrar na dimensão Dreamscape.
 * <p>
 * É uma classe client-only (ver {@link OnlyIn} e {@link Dist#CLIENT}) e registrada via
 * {@link net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent}.
 * </p>
 */
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
        super.renderBackground(gg, mouseX, mouseY, partialTick);

        float progress = Math.min(1.0f, fadeTicks / (float) MIN_TICKS);
        int alpha = (int) (progress * 255) << 24;
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
