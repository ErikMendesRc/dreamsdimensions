package com.dreamsdimensions.mod.client.screen;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.network.ReadyToTeleportPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class DreamTransitionScreen extends Screen {
    private int ticks = 0;

    public DreamTransitionScreen() {
        super(Component.literal("Sonhando..."));
    }

    @Override
    public void tick() {
        super.tick();
        ticks++;
        if (ticks == 1) {
            DreamsDimensions.LOGGER.info("DreamTransitionScreen aberta, iniciando contagem de ticks");
        }
        if (ticks >= 60) {
            DreamsDimensions.LOGGER.info("DreamTransitionScreen atingiu {} ticks, enviando ReadyToTeleportPacket", ticks);
            PacketDistributor.sendToServer(new ReadyToTeleportPacket());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int alpha = Math.min(255, ticks * 5);
        graphics.fill(0, 0, width, height, (alpha << 24));

        graphics.drawCenteredString(this.font, Component.literal("Você está sonhando..."), width / 2, height / 2, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}