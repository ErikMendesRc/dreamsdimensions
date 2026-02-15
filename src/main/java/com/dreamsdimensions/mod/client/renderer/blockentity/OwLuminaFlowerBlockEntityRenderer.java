package com.dreamsdimensions.mod.client.renderer.blockentity;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.block.entity.OwLuminaFlowerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Renderer da camada emissiva da Lumina Flower em FULL_BRIGHT.
 */
public class OwLuminaFlowerBlockEntityRenderer implements BlockEntityRenderer<OwLuminaFlowerBlockEntity> {
    private static final Identifier EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    DreamsDimensions.MODID,
                    "textures/block/ow_lumina_flower_emissive.png"
            );


    public OwLuminaFlowerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(OwLuminaFlowerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(EMISSIVE_TEXTURE));
        PoseStack.Pose pose = poseStack.last();

        renderCrossPlane(consumer, pose, 0.0F, 0.0F, 1.0F, 1.0F);
        renderCrossPlane(consumer, pose, 1.0F, 0.0F, 0.0F, 1.0F);
    }

    private static void renderCrossPlane(VertexConsumer consumer, PoseStack.Pose pose,
                                         float x1, float z1, float x2, float z2) {
        addVertex(consumer, pose, x1, 1.0F, z1, 1.0F, 0.0F);
        addVertex(consumer, pose, x1, 0.0F, z1, 1.0F, 1.0F);
        addVertex(consumer, pose, x2, 0.0F, z2, 0.0F, 1.0F);
        addVertex(consumer, pose, x2, 1.0F, z2, 0.0F, 0.0F);

        addVertex(consumer, pose, x2, 1.0F, z2, 1.0F, 0.0F);
        addVertex(consumer, pose, x2, 0.0F, z2, 1.0F, 1.0F);
        addVertex(consumer, pose, x1, 0.0F, z1, 0.0F, 1.0F);
        addVertex(consumer, pose, x1, 1.0F, z1, 0.0F, 0.0F);
    }

    private static void addVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                  float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
