package org.lingZero.modularization_defend.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.Blocks.ElectricityRepeater.ElectricityRepeaterBlockEntity;
import org.lingZero.modularization_defend.GeoModel.ElectricityRepeaterModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 电力中继器方块渲染器
 * 使用 GeckoLib 渲染 3D 模型
 * 只有主方块（控制器）会渲染模型，其他结构方块不渲染
 */
public class ElectricityRepeaterRenderer extends GeoBlockRenderer<ElectricityRepeaterBlockEntity> {
    public ElectricityRepeaterRenderer(BlockEntityRendererProvider.Context context) {
        super(new ElectricityRepeaterModel());
    }
    
    @Override
    public void render(ElectricityRepeaterBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 检查是否为控制器，只有控制器才渲染模型
        boolean isController = blockEntity.isControllerViaComponent();
        
        if (!isController) {
            return;
        }
        
        // 如果是控制器，正常渲染
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }
    
    /**
     * 渲染前处理，设置光照
     */
    @Override
    public void preRender(PoseStack poseStack, ElectricityRepeaterBlockEntity animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
    
    /**
     * 实际渲染方法，处理光照和发光部件
     */
    @Override
    public void actuallyRender(PoseStack poseStack, ElectricityRepeaterBlockEntity animatable, BakedGeoModel model,
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
    public void renderRecursively(PoseStack poseStack, ElectricityRepeaterBlockEntity animatable, GeoBone bone,
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
