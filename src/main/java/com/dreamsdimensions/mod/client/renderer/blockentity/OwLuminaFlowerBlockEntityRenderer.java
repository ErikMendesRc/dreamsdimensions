package com.dreamsdimensions.mod.client.renderer.blockentity;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.block.entity.OwLuminaFlowerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * Renderer emissivo da Lumina Flower.
 */
public class OwLuminaFlowerBlockEntityRenderer implements BlockEntityRenderer<OwLuminaFlowerBlockEntity, OwLuminaFlowerBlockEntityRenderer.State> {
    private static final Identifier EMISSIVE_TEXTURE =
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "textures/block/ow_lumina_flower_emissive.png");

    private static final RenderType EMISSIVE_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(EMISSIVE_TEXTURE);

    public OwLuminaFlowerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(OwLuminaFlowerBlockEntity blockEntity, State renderState, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay breakOverlayProgress) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, breakOverlayProgress);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        submitNodeCollector.submitCustomGeometry(poseStack, EMISSIVE_RENDER_TYPE, OwLuminaFlowerBlockEntityRenderer::renderGeometry);
    }

    private static void renderGeometry(PoseStack.Pose pose, VertexConsumer consumer) {
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

    public static final class State extends BlockEntityRenderState {
    }
}
