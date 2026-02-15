package com.dreamsdimensions.mod.client.renderer.blockentity;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.block.entity.OwLuminaFlowerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class OwLuminaFlowerBlockEntityRenderer implements BlockEntityRenderer<OwLuminaFlowerBlockEntity, OwLuminaFlowerBlockEntityRenderer.State> {
    private static final Identifier EMISSIVE_SPRITE =
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "block/ow_lumina_flower_emissive");
    private static final RenderType EMISSIVE_RENDER_TYPE = RenderTypes.entityCutoutNoCullZOffset(TextureAtlas.LOCATION_BLOCKS);

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
        renderState.blockState = blockEntity.getBlockState();
        renderState.blockPos = blockEntity.getBlockPos();
        renderState.offset = renderState.blockState.getOffset(renderState.blockPos);

        TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(EMISSIVE_SPRITE);
        renderState.u0 = sprite.getU0();
        renderState.u1 = sprite.getU1();
        renderState.v0 = sprite.getV0();
        renderState.v1 = sprite.getV1();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        final float u0 = state.u0;
        final float u1 = state.u1;
        final float v0 = state.v0;
        final float v1 = state.v1;
        final Vec3 offset = state.offset;

        submitNodeCollector.submitCustomGeometry(poseStack, EMISSIVE_RENDER_TYPE, (pose, consumer) -> {
            PoseStack.Pose adjustedPose = pose.copy();
            adjustedPose.pose().translate((float) offset.x, (float) offset.y, (float) offset.z);
            renderCrossPlane(consumer, adjustedPose, 0.0F, 0.0F, 1.0F, 1.0F, u0, u1, v0, v1);
            renderCrossPlane(consumer, adjustedPose, 1.0F, 0.0F, 0.0F, 1.0F, u0, u1, v0, v1);
        });
    }

    private static void renderCrossPlane(VertexConsumer consumer, PoseStack.Pose pose,
                                         float x1, float z1, float x2, float z2,
                                         float u0, float u1, float v0, float v1) {
        addVertex(consumer, pose, x1, 1.0F, z1, u1, v0);
        addVertex(consumer, pose, x1, 0.0F, z1, u1, v1);
        addVertex(consumer, pose, x2, 0.0F, z2, u0, v1);
        addVertex(consumer, pose, x2, 1.0F, z2, u0, v0);

        addVertex(consumer, pose, x2, 1.0F, z2, u1, v0);
        addVertex(consumer, pose, x2, 0.0F, z2, u1, v1);
        addVertex(consumer, pose, x1, 0.0F, z1, u0, v1);
        addVertex(consumer, pose, x1, 1.0F, z1, u0, v0);
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
        private BlockState blockState;
        private BlockPos blockPos;
        private Vec3 offset = Vec3.ZERO;
        private float u0;
        private float u1;
        private float v0;
        private float v1;
    }
}
