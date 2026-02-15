package com.dreamsdimensions.mod.client.renderer.blockentity;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.block.entity.OwLuminaFlowerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Renderer da camada emissiva da Lumina Flower em FULL_BRIGHT.
 */
public class OwLuminaFlowerBlockEntityRenderer implements BlockEntityRenderer<OwLuminaFlowerBlockEntity> {
    private static final ResourceLocation EMISSIVE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(DreamsDimensions.MODID, "block/ow_lumina_flower_emissive");

    public OwLuminaFlowerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(OwLuminaFlowerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(EMISSIVE_TEXTURE);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
        poseStack.pushPose();

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        renderCrossPlane(consumer, pose, normal, sprite, 0.0F, 0.0F, 1.0F, 1.0F);
        renderCrossPlane(consumer, pose, normal, sprite, 1.0F, 0.0F, 0.0F, 1.0F);

        poseStack.popPose();
    }

    private static void renderCrossPlane(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
                                         TextureAtlasSprite sprite, float x1, float z1, float x2, float z2) {
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        addVertex(consumer, pose, normal, x1, 1.0F, z1, maxU, minV, 0.0F, 1.0F, 0.0F);
        addVertex(consumer, pose, normal, x1, 0.0F, z1, maxU, maxV, 0.0F, 1.0F, 0.0F);
        addVertex(consumer, pose, normal, x2, 0.0F, z2, minU, maxV, 0.0F, 1.0F, 0.0F);
        addVertex(consumer, pose, normal, x2, 1.0F, z2, minU, minV, 0.0F, 1.0F, 0.0F);

        addVertex(consumer, pose, normal, x2, 1.0F, z2, maxU, minV, 0.0F, -1.0F, 0.0F);
        addVertex(consumer, pose, normal, x2, 0.0F, z2, maxU, maxV, 0.0F, -1.0F, 0.0F);
        addVertex(consumer, pose, normal, x1, 0.0F, z1, minU, maxV, 0.0F, -1.0F, 0.0F);
        addVertex(consumer, pose, normal, x1, 1.0F, z1, minU, minV, 0.0F, -1.0F, 0.0F);
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
                                  float x, float y, float z, float u, float v,
                                  float normalX, float normalY, float normalZ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(normal, normalX, normalY, normalZ);
    }
}
