package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 实体追踪器接口
 * 用于持续追踪已锁定的实体，支持状态管理和自动更新
 */
public interface IEntityTracker {
    
    /**
     * 获取当前追踪的实体
     * 
     * @return 正在追踪的实体，如果未锁定或丢失则返回 null
     */
    @Nullable
    Entity getTrackedEntity();
    
    /**
     * 获取被追踪实体的 UUID
     * 即使实体暂时不可见，也可以通过 UUID 重新查找
     * 
     * @return 实体 UUID，未锁定时返回 null
     */
    @Nullable
    java.util.UUID getTrackedEntityUUID();
    
    /**
     * 更新追踪状态
     * 应在每个 tick 或需要时调用，以检查目标是否仍然有效
     * 
     * @return 追踪状态
     */
    @NotNull
    TrackingState update();
    
    /**
     * 检查是否正在追踪实体
     * 
     * @return true 如果已锁定且实体有效
     */
    boolean isTracking();
    
    /**
     * 检查是否丢失了追踪目标
     * 
     * @return true 如果曾经锁定但现在无法找到目标
     */
    boolean isLost();
    
    /**
     * 检查是否从未锁定过目标
     * 
     * @return true 如果还未锁定任何目标
     */
    boolean isUnlocked();
    
    /**
     * 释放当前追踪的目标
     * 重置追踪器状态
     */
    void release();
    
    /**
     * 获取追踪状态枚举
     */
    enum TrackingState {
        /**
         * 未锁定 - 还未选择追踪目标
         */
        UNLOCKED,
        
        /**
         * 追踪中 - 已成功锁定并正在追踪目标
         */
        TRACKING,
        
        /**
         * 丢失 - 曾经锁定但当前无法找到目标
         */
        LOST,
        
        /**
         * 无效 - 追踪器已释放或失效
         */
        INVALID
    }
}
