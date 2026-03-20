package org.lingZero.modularization_defend.GeoModel.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.AbstractMultiblockBlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 多方块方块渲染器抽象基类
 * 提取了多方块渲染的通用逻辑，包括：
 * - 离屏渲染支持
 * - 自定义渲染判断
 * - 扩展渲染边界框
 * - 发光部件处理
 * 
 * @param <T> 方块实体类型，必须是 AbstractMultiblockBlockEntity 和 GeoAnimatable 的子类
 */
public abstract class AbstractMultiblockRenderer<T extends AbstractMultiblockBlockEntity & GeoAnimatable> extends GeoBlockRenderer<T> {
    
    /**
     * 构造方法
     * @param model GeckoLib 模型
     */
    public AbstractMultiblockRenderer(Object model) {
        super((software.bernie.geckolib.model.GeoModel<T>) model);
    }
    
    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack,
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
    public boolean shouldRenderOffScreen(T pBlockEntity) {
        return true;
    }
    
    /**
     * 自定义渲染判断逻辑，只要多方块成型就始终渲染
     * 参考 Mekanism 的多方块渲染实现
     */
    @Override
    public boolean shouldRender(T pBlockEntity, Vec3 pCamera) {
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
     * 
     * 子类需要实现此方法来定义自己的边界框大小
     * 
     * @param pBlockEntity 方块实体
     * @return 渲染边界框
     */
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(T pBlockEntity) {
        if (pBlockEntity.isController()) {
            // 调用抽象方法，由子类提供具体的边界框
            return createMultiblockRenderBoundingBox(pBlockEntity);
        }
        return super.getRenderBoundingBox(pBlockEntity);
    }
    
    /**
     * 创建多方块渲染边界框
     * 子类需要实现此方法来定义自己的边界框大小
     * 
     * @param blockEntity 方块实体
     * @return 覆盖整个多方块结构的边界框
     */
    protected abstract net.minecraft.world.phys.AABB createMultiblockRenderBoundingBox(T blockEntity);
    
    /**
     * 渲染前处理，设置光照
     */
    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
    
    /**
     * 实际渲染方法，处理光照和发光部件
     */
    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model,
                               @Nullable RenderType renderType, MultiBufferSource bufferSource,
                               @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, int colour) {
        // 使用方块的实际光照值
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }
    
    /**
     * 递归渲染骨骼，处理发光部件
     * 
     * 子类可以通过重写此方法来添加自定义的骨骼渲染逻辑
     */
    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        // 检查是否是发光部件
        if (isGlowingBone(bone)) {
            // 为发光部件使用更高的光照值
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender,
                    partialTick, LightTexture.FULL_BRIGHT, packedOverlay, colour);
        } else {
            // 正常渲染其他部件
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender,
                    partialTick, packedLight, packedOverlay, colour);
        }
    }
    
    /**
     * 判断骨骼是否是发光部件
     * 子类可以重写此方法来自定义发光部件的判断逻辑
     * 
     * @param bone 骨骼
     * @return 是否发光
     */
    protected boolean isGlowingBone(GeoBone bone) {
        return "发光部件".equals(bone.getName());
    }
}
