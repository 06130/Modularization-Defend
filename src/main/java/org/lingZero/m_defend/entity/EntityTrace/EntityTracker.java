package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.util.DebugLogger;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * 实体追踪器实现类
 * 提供持续的实体追踪功能，支持锁定、更新和状态管理
 * 
 * 使用场景：
 * - 炮塔锁定目标后持续追踪
 * - AI 持续跟随特定实体
 * - 防御系统监控特定威胁
 */
public class EntityTracker implements IEntityTracker {
    
    private final Level level;
    private final BlockPos sourcePos;
    private final double searchRadius;
    private final double searchHeight;
    private final EntityFilter filter;
    
    // 性能优化：预计算的值
    private final double searchRadiusSq;      // searchRadius 的平方，避免重复计算
    private final AABB searchBox;             // 缓存搜索框，避免重复创建
    private final double sourceCenterX;       // 缓存源点中心 X 坐标
    private final double sourceCenterY;       // 缓存源点中心 Y 坐标
    private final double sourceCenterZ;       // 缓存源点中心 Z 坐标
    
    // 追踪状态
    private UUID trackedEntityUUID;
    private Entity cachedEntity;
    private TrackingState state;
    private int lostTicks; // 丢失目标的 tick 数
    
    // 回调监听器
    private Consumer<String> targetLostCallback;
    
    // 配置参数
    private static final int MAX_LOST_TICKS = 100; // 最大丢失 tick 数，超过后自动释放
    
    /**
     * 构造函数
     * 
     * @param level 世界对象
     * @param sourcePos 搜索源点坐标
     * @param searchRadius 水平搜索半径
     * @param searchHeight 垂直搜索高度
     * @param filter 实体过滤器
     */
    public EntityTracker(
            @NotNull Level level,
            @NotNull BlockPos sourcePos,
            double searchRadius,
            double searchHeight,
            @NotNull EntityFilter filter) {
        
        this.level = level;
        this.sourcePos = sourcePos;
        this.searchRadius = searchRadius;
        this.searchHeight = searchHeight;
        this.filter = filter;
        this.trackedEntityUUID = null;
        this.cachedEntity = null;
        this.state = TrackingState.UNLOCKED;
        this.lostTicks = 0;
        this.targetLostCallback = null;
        
        // 预计算优化值
        this.searchRadiusSq = searchRadius * searchRadius;
        this.searchBox = new AABB(
                sourcePos.getX() - searchRadius,
                sourcePos.getY() - searchHeight,
                sourcePos.getZ() - searchRadius,
                sourcePos.getX() + searchRadius + 1,
                sourcePos.getY() + searchHeight + 1,
                sourcePos.getZ() + searchRadius + 1
        );
        Vec3 center = sourcePos.getCenter();
        this.sourceCenterX = center.x;
        this.sourceCenterY = center.y;
        this.sourceCenterZ = center.z;
    }
    
    /**
     * 尝试锁定最近的符合条件的实体
     * 如果当前未锁定或已丢失，会重新搜索并锁定目标
     * 
     * @return true 如果成功锁定或保持锁定
     */
    public boolean tryLock() {
        if (state == TrackingState.TRACKING && cachedEntity != null && cachedEntity.isAlive()) {
            // 已经在追踪且实体存活，无需重新锁定
            return true;
        }
        
        // 搜索最近的实体
        EntitySearchResult result = EntitySearchUtil.findNearestEntity(
                level, sourcePos, searchRadius, searchHeight, filter
        );
        
        Entity target = result.getNearestEntity();
        if (target != null && target.isAlive()) {
            lockOn(target);
            DebugLogger.debug("实体追踪器锁定目标: %s (UUID: %s)", 
                    target.getType().getDescriptionId(), target.getUUID());
            return true;
        }
        
        DebugLogger.debug("实体追踪器未能锁定目标");
        return false;
    }
    
    /**
     * 锁定指定实体
     * 
     * @param entity 要锁定的实体
     */
    public void lockOn(@NotNull Entity entity) {
        if (!filter.test(entity)) {
            DebugLogger.warn("尝试锁定不符合过滤条件的实体: %s", entity.getType().getDescriptionId());
            return;
        }
        
        this.trackedEntityUUID = entity.getUUID();
        this.cachedEntity = entity;
        this.state = TrackingState.TRACKING;
        this.lostTicks = 0;
        
        DebugLogger.debug("实体追踪器已锁定: %s", entity.getType().getDescriptionId());
    }
    
    @Override
    @Nullable
    public Entity getTrackedEntity() {
        if (state != TrackingState.TRACKING) {
            return null;
        }
        
        // 快速路径：缓存的实体仍然有效且 UUID 匹配
        if (cachedEntity != null && cachedEntity.isAlive()) {
            // 使用 == 先进行引用比较，避免不必要的 UUID 比较
            if (cachedEntity.getUUID() == trackedEntityUUID || 
                cachedEntity.getUUID().equals(trackedEntityUUID)) {
                return cachedEntity;
            }
        }
        
        // 缓存失效，尝试通过 UUID 重新查找
        cachedEntity = findEntityByUUID();
        return cachedEntity;
    }
    
    @Override
    @Nullable
    public UUID getTrackedEntityUUID() {
        return trackedEntityUUID;
    }
    
    @Override
    @NotNull
    public TrackingState update() {
        if (state == TrackingState.INVALID) {
            return TrackingState.INVALID;
        }
        
        if (state == TrackingState.UNLOCKED) {
            return TrackingState.UNLOCKED;
        }
        
        // 检查当前追踪的实体是否仍然有效
        Entity currentEntity = getTrackedEntity();
        
        if (currentEntity != null && currentEntity.isAlive()) {
            // 检查是否在搜索范围内（使用缓存的中心坐标）
            double dx = currentEntity.getX() - sourceCenterX;
            double dy = currentEntity.getY() - sourceCenterY;
            double dz = currentEntity.getZ() - sourceCenterZ;
            double distanceSq = dx * dx + dy * dy + dz * dz;
            
            if (distanceSq > searchRadiusSq) {
                // 实体离开范围
                DebugLogger.debug("实体追踪器：目标离开范围 (距离: %.2f > %.2f)", 
                        Math.sqrt(distanceSq), searchRadius);
                triggerTargetLostCallback("out_of_range");
                state = TrackingState.LOST;
                lostTicks++;
                return TrackingState.LOST;
            }
            
            // 实体仍然有效且在范围内
            state = TrackingState.TRACKING;
            lostTicks = 0;
            return TrackingState.TRACKING;
        } else {
            // 实体无法找到（UUID查找失败或已完全移除）
            // 立即触发回调，让炮塔重新搜索
            DebugLogger.debug("实体追踪器：目标无法找到，触发重新锁定");
            triggerTargetLostCallback("not_found");
            state = TrackingState.LOST;
            lostTicks = 0; // 重置计数，因为已经触发回调
            return TrackingState.LOST;
        }
    }
    
    /**
     * 触发目标丢失回调
     * 
     * @param reason 丢失原因
     */
    private void triggerTargetLostCallback(String reason) {
        if (targetLostCallback != null) {
            try {
                targetLostCallback.accept(reason);
            } catch (Exception e) {
                DebugLogger.error("目标丢失回调执行失败: %s", e.getMessage());
            }
        }
    }
    
    @Override
    public boolean isTracking() {
        // 快速路径：先检查状态，避免调用 getTrackedEntity()
        if (state != TrackingState.TRACKING) {
            return false;
        }
        
        // 缓存命中时直接返回，避免重复查找
        return cachedEntity != null && cachedEntity.isAlive();
    }
    
    @Override
    public boolean isLost() {
        return state == TrackingState.LOST;
    }
    
    @Override
    public boolean isUnlocked() {
        return state == TrackingState.UNLOCKED;
    }
    
    @Override
    public void release() {
        this.trackedEntityUUID = null;
        this.cachedEntity = null;
        this.state = TrackingState.INVALID;
        this.lostTicks = 0;
        this.targetLostCallback = null;
        
        DebugLogger.debug("实体追踪器已释放");
    }
    
    @Override
    public void onTargetLost(Consumer<String> callback) {
        this.targetLostCallback = callback;
    }
    
    @Override
    public boolean forceRelock() {
        DebugLogger.debug("实体追踪器：手动触发重新锁定");
        
        // 释放当前目标
        release();
        
        // 重置状态为 UNLOCKED，以便重新搜索
        this.state = TrackingState.UNLOCKED;
        
        // 立即尝试锁定新目标
        return tryLock();
    }
    
    /**
     * 通过 UUID 在世界中查找实体
     * 性能优化：遍历搜索框内的实体而非整个世界
     * 
     * @return 找到的实体，未找到返回 null
     */
    @Nullable
    private Entity findEntityByUUID() {
        if (trackedEntityUUID == null) {
            return null;
        }
        
        // 在缓存的搜索框内查找具有匹配 UUID 的实体
        for (Entity entity : level.getEntities(null, searchBox)) {
            if (entity.getUUID().equals(trackedEntityUUID) && entity.isAlive()) {
                return entity;
            }
        }
        
        return null;
    }
    
    /**
     * 获取追踪源的坐标
     * 
     * @return 源坐标
     */
    @NotNull
    public BlockPos getSourcePos() {
        return sourcePos;
    }
    
    /**
     * 获取搜索半径
     * 
     * @return 搜索半径
     */
    public double getSearchRadius() {
        return searchRadius;
    }
    
    /**
     * 获取搜索高度
     * 
     * @return 搜索高度
     */
    public double getSearchHeight() {
        return searchHeight;
    }
    
    /**
     * 获取丢失目标的 tick 数
     * 
     * @return 丢失 tick 数
     */
    public int getLostTicks() {
        return lostTicks;
    }
    
    /**
     * 重置丢失计数
     */
    public void resetLostTicks() {
        this.lostTicks = 0;
    }
}
