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

public class OwLuminaFlowerBlockEntityRenderer
        implements BlockEntityRenderer<OwLuminaFlowerBlockEntity, OwLuminaFlowerBlockEntityRenderer.State> {

    private static final Identifier EMISSIVE_SPRITE =
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "block/ow_lumina_flower_emissive");

    // minecraft:blocks (atlas lógico)
    private static final Identifier BLOCK_ATLAS_ID =
            Identifier.withDefaultNamespace("blocks");

    @SuppressWarnings("deprecation")
    private static final Identifier BLOCK_ATLAS_TEXTURE = TextureAtlas.LOCATION_BLOCKS;

    // ✅ emissivo "eyes"
    private static final RenderType EMISSIVE_RENDER_TYPE =
            RenderTypes.eyes(BLOCK_ATLAS_TEXTURE);

    // ✅ empurra a camada emissiva em direção à câmera (tira do depth do modelo base)
    // Ajuste fino:
    // - 0.0015f: bem discreto
    // - 0.0025f: quase sempre elimina 100% dos artefatos
    // - 0.0040f: mais agressivo (ainda geralmente imperceptível)
    private static final float CAMERA_PUSH_EPS = 0.0025f;

    // 1/sqrt(2) para normais diagonais
    private static final float DIAG_N = 0.70710677f;

    public OwLuminaFlowerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void extractRenderState(OwLuminaFlowerBlockEntity blockEntity,
                                   State renderState,
                                   float partialTick,
                                   Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay breakOverlayProgress) {

        BlockEntityRenderState.extractBase(blockEntity, renderState, breakOverlayProgress);

        renderState.blockState = blockEntity.getBlockState();
        renderState.blockPos = blockEntity.getBlockPos();
        renderState.offset = renderState.blockState.getOffset(renderState.blockPos);

        // guarda posição da câmera pra fazer o view-facing offset no submit
        renderState.cameraPos = cameraPos;

        TextureAtlas atlas = Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(BLOCK_ATLAS_ID);

        TextureAtlasSprite sprite = atlas.getSprite(EMISSIVE_SPRITE);
        renderState.u0 = sprite.getU0();
        renderState.u1 = sprite.getU1();
        renderState.v0 = sprite.getV0();
        renderState.v1 = sprite.getV1();
    }

    @Override
    public void submit(State state,
                       PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector,
                       CameraRenderState cameraRenderState) {

        final float u0 = state.u0, u1 = state.u1, v0 = state.v0, v1 = state.v1;
        final Vec3 offset = state.offset;
        final BlockPos pos = state.blockPos;
        final Vec3 cam = state.cameraPos;

        submitNodeCollector.submitCustomGeometry(poseStack, EMISSIVE_RENDER_TYPE, (pose, consumer) -> {
            PoseStack.Pose p = pose.copy();

            // aplica offset do blockstate (igual vanilla)
            p.pose().translate((float) offset.x, (float) offset.y, (float) offset.z);

            // ✅ view-facing offset: empurra a camada emissiva em direção à câmera
            // centro do bloco + offset do model
            Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                    .add(offset);

            Vec3 toCam = cam.subtract(center);
            double len = toCam.length();
            if (len > 1.0e-6) {
                Vec3 dir = toCam.scale(1.0 / len); // normalize
                p.pose().translate((float) (dir.x * CAMERA_PUSH_EPS),
                        (float) (dir.y * CAMERA_PUSH_EPS),
                        (float) (dir.z * CAMERA_PUSH_EPS));
            }

            // Plano diagonal 1 (normal ~ +X +Z)
            renderCrossPlaneDoubleSided(consumer, p,
                    0.0F, 0.0F, 1.0F, 1.0F,
                    u0, u1, v0, v1,
                    +DIAG_N, 0.0F, +DIAG_N);

            // Plano diagonal 2 (normal ~ -X +Z)
            renderCrossPlaneDoubleSided(consumer, p,
                    1.0F, 0.0F, 0.0F, 1.0F,
                    u0, u1, v0, v1,
                    -DIAG_N, 0.0F, +DIAG_N);
        });
    }

    /**
     * Double-sided garantido (frente + verso) sem depender do cull do pipeline EYES.
     * Aqui NÃO fazemos epsilon por normal (porque o camera-push já resolve o depth contra o base),
     * e isso evita reintroduzir briga entre frente/verso.
     */
    private static void renderCrossPlaneDoubleSided(VertexConsumer consumer, PoseStack.Pose pose,
                                                    float x1, float z1, float x2, float z2,
                                                    float u0, float u1, float v0, float v1,
                                                    float nx, float ny, float nz) {

        // FRONT (winding padrão)
        addVertex(consumer, pose, x1, 1.0F, z1, u1, v0, nx, ny, nz);
        addVertex(consumer, pose, x1, 0.0F, z1, u1, v1, nx, ny, nz);
        addVertex(consumer, pose, x2, 0.0F, z2, u0, v1, nx, ny, nz);
        addVertex(consumer, pose, x2, 1.0F, z2, u0, v0, nx, ny, nz);

        // BACK (winding invertido + normal invertida)
        addVertex(consumer, pose, x2, 1.0F, z2, u1, v0, -nx, -ny, -nz);
        addVertex(consumer, pose, x2, 0.0F, z2, u1, v1, -nx, -ny, -nz);
        addVertex(consumer, pose, x1, 0.0F, z1, u0, v1, -nx, -ny, -nz);
        addVertex(consumer, pose, x1, 1.0F, z1, u0, v0, -nx, -ny, -nz);
    }

    private static void addVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                  float x, float y, float z,
                                  float u, float v,
                                  float nx, float ny, float nz) {

        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }

    public static final class State extends BlockEntityRenderState {
        private BlockState blockState;
        private BlockPos blockPos;
        private Vec3 offset = Vec3.ZERO;

        // camera usada pro view-facing offset
        private Vec3 cameraPos = Vec3.ZERO;

        private float u0, u1, v0, v1;
    }
}
