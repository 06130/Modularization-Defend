package org.lingZero.m_defend.Client.Render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.ModularizationDefend;
import org.lingZero.m_defend.entity.LaserProjectile;
import org.lingZero.m_defend.util.FxHelper;

/**
 * 激光弹渲染器<p>
 * 职责：仅负责实体几何渲染和视觉回退效果<p>
 * FX 特效绑定已在 LaserProjectile.onClientTick() 中处理<p>
 * 如果 Photon FX 不可用，则使用简单粒子作为回退方案
 */
public class LaserProjectileRenderer extends EntityRenderer<LaserProjectile> {
    
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
        
        // 仅在客户端处理视觉效果
        if (entity.level().isClientSide()) {
            // 如果 Photon FX 未生效，使用简单粒子效果作为回退
            if (!FxHelper.hasActiveEffect(entity)) {
                spawnSimpleParticles(entity, entity.level());
            }
        }
        
        // 调用父类渲染（不渲染任何可见内容，避免冲突）
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
    
    /**
     * 生成简单的粒子效果（Photon FX 不可用时的回退方案）
     */
    private void spawnSimpleParticles(LaserProjectile entity, Level level) {
        // 在实体位置生成火焰粒子
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        
        // 每 tick 生成少量粒子
        if (level.random.nextInt(3) == 0) {
            level.addParticle(
                ParticleTypes.FLAME,
                x + (level.random.nextDouble() - 0.5) * 0.3,
                y + (level.random.nextDouble() - 0.5) * 0.3,
                z + (level.random.nextDouble() - 0.5) * 0.3,
                0.0, 0.0, 0.0
            );
        }
    }
    
    @Override
    @NotNull
    public ResourceLocation getTextureLocation(@NotNull LaserProjectile entity) {
        // 不使用纹理，返回空资源位置
        return ResourceLocation.fromNamespaceAndPath(ModularizationDefend.MODID, "textures/entity/empty.png");
    }
}
