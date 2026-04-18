package org.lingZero.m_defend.util;

import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Photon FX 特效辅助工具类<p>
 * 提供统一的 FX 绑定和管理功能
 */
public final class FxHelper {
    
    private FxHelper() {
        // 工具类，禁止实例化
    }
    
    /**
     * 在客户端为实体绑定 Photon FX 特效<p>
     * 应在实体的 tick() 方法中调用（仅首次生成时）
     * 
     * @param entity 目标实体
     * @param fxLocation FX 资源位置
     * @param autoRotate 自动旋转模式
     * @param forceDeath 是否在实体死亡时强制销毁特效
     * @return true 如果成功绑定
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean bindEntityFx(
            Entity entity,
            ResourceLocation fxLocation,
            EntityEffectExecutor.AutoRotate autoRotate,
            boolean forceDeath) {
        
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.isClientSide()) {
            return false;
        }
        
        // 加载 FX 资源
        FX fx = FXHelper.getFX(fxLocation, false);
        if (fx == null) {
            DebugLogger.warn("无法加载 FX 资源: %s", fxLocation);
            return false;
        }
        
        // 检查是否已存在活跃的特效
        if (hasActiveEffect(entity)) {
            return true; // 已有特效，无需重复绑定
        }
        
        // 创建并启动特效
        EntityEffectExecutor effect = new EntityEffectExecutor(
            fx,
            level,
            entity,
            autoRotate
        );
        effect.setForcedDeath(forceDeath);
        effect.start();
        
        DebugLogger.debug("成功绑定 FX 到实体: %s at %s", fxLocation, entity.position());
        return true;
    }
    
    /**
     * 在客户端为实体绑定 Photon FX 特效（使用默认参数）<p>
     * 默认配置：FORWARD 旋转，forceDeath=true
     * 
     * @param entity 目标实体
     * @param fxLocation FX 资源位置
     * @return true 如果成功绑定
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean bindEntityFx(Entity entity, ResourceLocation fxLocation) {
        return bindEntityFx(entity, fxLocation, EntityEffectExecutor.AutoRotate.FORWARD, true);
    }
    
    /**
     * 检查实体是否有活跃的 Photon 特效
     * 
     * @param entity 待检查的实体
     * @return true 如果有活跃的特效
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean hasActiveEffect(Entity entity) {
        var existingEffects = EntityEffectExecutor.CACHE.get(entity);
        return existingEffects != null && !existingEffects.isEmpty();
    }
}
