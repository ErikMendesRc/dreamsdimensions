package com.dreamsdimensions.mod; // Seu pacote base

import com.dreamsdimensions.mod.event.SleepTeleportHandler;
import com.dreamsdimensions.mod.init.ModBlocks;
import com.dreamsdimensions.mod.init.ModCreativeTabs;
import com.dreamsdimensions.mod.init.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;


@Mod(DreamsDimensions.MODID)
public class DreamsDimensions {
    public static final String MODID = "dreamsdimensions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DreamsDimensions(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Dreams Dimensions Mod está carregando!");

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new SleepTeleportHandler());

        LOGGER.info("Registros e listeners de Dreams Dimensions configurados.");
    }

    /**
     * Método chamado durante a fase de setup comum (Common Setup).
     * Executado após o registro de conteúdo, mas antes do carregamento completo do mundo.
     * Bom lugar para registrar capacidades, enviar mensagens IMC para outros mods, etc.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Executando Common Setup para Dreams Dimensions...");
        // Coloque aqui código de setup que precisa rodar em ambos os lados (cliente e servidor)
        // e que dependa de registros já existentes.
        // Exemplo: Registrar funcionalidade de compostagem, interação com outros mods.

        // O logging de exemplo da config foi removido daqui, pois a config será tratada separadamente.
        LOGGER.info("Common Setup de Dreams Dimensions concluído.");
    }


    // --- EXEMPLO DE MANIPULADOR DE EVENTO DO JOGO (Não estático) ---
    /**
     * Método chamado quando o servidor está iniciando.
     * Anotado com @SubscribeEvent. Como este método NÃO é estático, a linha
     * 'NeoForge.EVENT_BUS.register(this);' no construtor é necessária para que ele seja chamado.
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Faça algo quando o servidor iniciar.
        // Exemplo: Registrar comandos customizados.
        LOGGER.info("Servidor iniciando - Olá do Dreams Dimensions!");
    }

    // --- MANIPULADORES DE EVENTOS DO LADO DO CLIENTE ---
    // Usa EventBusSubscriber para registrar automaticamente métodos ESTÁTICOS
    // anotados com @SubscribeEvent que pertencem ao barramento de eventos do MOD
    // e são específicos do lado do CLIENTE.
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        /**
         * Método chamado durante a fase de setup do cliente (Client Setup).
         * Executado apenas no lado do cliente. Bom lugar para registrar renderers,
         * key bindings, telas (screens) customizadas, etc.
         */
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Executando Client Setup para Dreams Dimensions...");
            // Coloque aqui código de setup específico do cliente.
            // Exemplo: ClientRegistry.bindTileEntityRenderer(...), RenderingRegistry.registerEntityRenderingHandler(...)
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DREAM_FLOWER_BLOCK.get(), RenderType.cutout());

            // Exemplo de log que só funciona no cliente:
            LOGGER.info("Nome do Usuário Minecraft (Cliente): {}", Minecraft.getInstance().getUser().getName());
            LOGGER.info("Client Setup de Dreams Dimensions concluído.");
        }
    }
}