package org.lingZero.modularization_defend.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.Blocks.AgreementCore.AgreementCoreBlockEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 协议核心方块渲染器
 * 使用 GeckoLib 渲染 3D 模型
 * 只有主方块（控制器）会渲染模型，其他结构方块不渲染
 */
public class AgreementCoreRenderer extends GeoBlockRenderer<AgreementCoreBlockEntity> {
    public AgreementCoreRenderer(BlockEntityRendererProvider.Context context) {
        super(new org.lingZero.modularization_defend.GeoModel.AgreementCoreModel());
    }
    
    @Override
    public void render(AgreementCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 检查是否为控制器，只有控制器才渲染模型
        if (!blockEntity.isController()) {
            return;
        }
        
        // 如果是控制器，正常渲染
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
    
    /**
     * 允许离屏渲染，这样即使主方块不在视野内也能渲染
     * 参考 Mekanism 的多方块渲染实现
     */
    @Override
    public boolean shouldRenderOffScreen(AgreementCoreBlockEntity pBlockEntity) {
        return true;
    }
    
    /**
     * 自定义渲染判断逻辑，只要多方块成型就始终渲染
     * 参考 Mekanism 的多方块渲染实现
     */
    @Override
    public boolean shouldRender(AgreementCoreBlockEntity pBlockEntity, net.minecraft.world.phys.Vec3 pCamera) {
        // 如果是控制器且多方块已成型，就渲染
        if (pBlockEntity.isController() && pBlockEntity.isMultiblockFormed()) {
            return true;
        }
        // 否则使用默认判断逻辑
        return super.shouldRender(pBlockEntity, pCamera);
    }
    
    /**
     * 获取渲染边界框，覆盖整个多方块结构
     * 参考 Mekanism 的多方块渲染实现
     */
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(AgreementCoreBlockEntity pBlockEntity) {
        if (pBlockEntity.isController()) {
            // 创建一个覆盖整个 5x5x1 底座 + 3x3x12 柱体结构的边界框
            return new net.minecraft.world.phys.AABB(
                pBlockEntity.getBlockPos().getX() - 64,
                pBlockEntity.getBlockPos().getY() - 64,
                pBlockEntity.getBlockPos().getZ() - 64,
                pBlockEntity.getBlockPos().getX() + 66,  // 2 + 64
                pBlockEntity.getBlockPos().getY() + 77,  // 13 + 64
                pBlockEntity.getBlockPos().getZ() + 66   // 2 + 64
            );
        }
        return super.getRenderBoundingBox(pBlockEntity);
    }
    
    /**
     * 渲染前处理，设置光照
     */
    @Override
    public void preRender(PoseStack poseStack, AgreementCoreBlockEntity animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
    
    /**
     * 实际渲染方法，处理光照和发光部件
     */
    @Override
    public void actuallyRender(PoseStack poseStack, AgreementCoreBlockEntity animatable, BakedGeoModel model,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, int colour) {
        // 使用方块的实际光照值
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }
    
    /**
     * 递归渲染骨骼，处理发光部件
     */
    @Override
    public void renderRecursively(PoseStack poseStack, AgreementCoreBlockEntity animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        // 检查是否是发光部件
        if ("发光部件".equals(bone.getName())) {
            // 为发光部件使用更高的光照值
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender,
                    partialTick, LightTexture.FULL_BRIGHT, packedOverlay, colour);
        } else {
            // 正常渲染其他部件
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender,
                    partialTick, packedLight, packedOverlay, colour);
        }
    }
}
