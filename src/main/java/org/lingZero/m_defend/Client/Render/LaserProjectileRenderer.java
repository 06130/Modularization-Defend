package org.lingZero.m_defend.Client.Render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.entity.projectile.impl.LaserProjectile;

/**
 * 激光子弹实体渲染器
 * 使用简单的发光点表示激光
 */
public class LaserProjectileRenderer extends EntityRenderer<LaserProjectile> {
    
    private static final ResourceLocation TEXTURE = 
        ResourceLocation.fromNamespaceAndPath("modularization_defend", "textures/entity/laser_projectile.png");
    
    public LaserProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(
            @NotNull LaserProjectile entity,
            float entityYaw,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight) {
        // 简单实现：不渲染任何内容，避免崩溃
        // 可以在这里添加粒子效果或简单的几何体渲染
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
    
    @Override
    @NotNull
    public ResourceLocation getTextureLocation(@NotNull LaserProjectile entity) {
        return TEXTURE;
    }
}
