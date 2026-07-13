package org.lingZero.modularization_defend.Block.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.Block.example.LevelEditorBlockEntity;

/**
 * 关卡编辑器区域的可视化渲染器。
 * 在 15x15x15 建造区域周围绘制蓝色半透明线框。
 */
@OnlyIn(Dist.CLIENT)
public class LevelEditorRenderer implements BlockEntityRenderer<LevelEditorBlockEntity> {

    public LevelEditorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull LevelEditorBlockEntity be) {
        return true;
    }

    @Override
    public void render(@NotNull LevelEditorBlockEntity be, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        AABB zone = be.getZoneAABB();
        BlockPos blockPos = be.getBlockPos();

        double x0 = zone.minX - blockPos.getX();
        double y0 = zone.minY - blockPos.getY();
        double z0 = zone.minZ - blockPos.getZ();
        double x1 = zone.maxX - blockPos.getX();
        double y1 = zone.maxY - blockPos.getY();
        double z1 = zone.maxZ - blockPos.getZ();

        poseStack.pushPose();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        float r = 0.2f, g = 0.5f, b = 1.0f, a = 0.6f;

        drawBoxEdges(poseStack, consumer, (float) x0, (float) y0, (float) z0,
                (float) x1, (float) y1, (float) z1, r, g, b, a);

        poseStack.popPose();
    }

    private static void drawBoxEdges(PoseStack poseStack, VertexConsumer consumer,
                                      float x0, float y0, float z0, float x1, float y1, float z1,
                                      float r, float g, float b, float a) {
        var matrix = poseStack.last().pose();
        var normal = poseStack.last();

        // 底面四条边 (y = y0)
        line(matrix, normal, consumer, x0, y0, z0, x1, y0, z0, r, g, b, a);
        line(matrix, normal, consumer, x1, y0, z0, x1, y0, z1, r, g, b, a);
        line(matrix, normal, consumer, x1, y0, z1, x0, y0, z1, r, g, b, a);
        line(matrix, normal, consumer, x0, y0, z1, x0, y0, z0, r, g, b, a);

        // 顶面四条边 (y = y1)
        line(matrix, normal, consumer, x0, y1, z0, x1, y1, z0, r, g, b, a);
        line(matrix, normal, consumer, x1, y1, z0, x1, y1, z1, r, g, b, a);
        line(matrix, normal, consumer, x1, y1, z1, x0, y1, z1, r, g, b, a);
        line(matrix, normal, consumer, x0, y1, z1, x0, y1, z0, r, g, b, a);

        // 四条垂直边
        line(matrix, normal, consumer, x0, y0, z0, x0, y1, z0, r, g, b, a);
        line(matrix, normal, consumer, x1, y0, z0, x1, y1, z0, r, g, b, a);
        line(matrix, normal, consumer, x1, y0, z1, x1, y1, z1, r, g, b, a);
        line(matrix, normal, consumer, x0, y0, z1, x0, y1, z1, r, g, b, a);
    }

    private static void line(org.joml.Matrix4f matrix, PoseStack.Pose normal, VertexConsumer consumer,
                              float x1, float y1, float z1, float x2, float y2, float z2,
                              float r, float g, float b, float a) {
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(normal, 0, 1, 0);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(normal, 0, 1, 0);
    }
}
