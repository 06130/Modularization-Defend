package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.util.DebugLogger;

import java.util.UUID;

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
    
    // 追踪状态
    private UUID trackedEntityUUID;
    private Entity cachedEntity;
    private TrackingState state;
    private int lostTicks; // 丢失目标的 tick 数
    
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
        
        // 如果缓存的实体仍然有效，直接返回
        if (cachedEntity != null && cachedEntity.isAlive() && cachedEntity.getUUID().equals(trackedEntityUUID)) {
            return cachedEntity;
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
            // 实体仍然有效
            state = TrackingState.TRACKING;
            lostTicks = 0;
            return TrackingState.TRACKING;
        } else {
            // 实体丢失
            lostTicks++;
            state = TrackingState.LOST;
            
            DebugLogger.debug("实体追踪器丢失目标 (丢失 %d ticks)", lostTicks);
            
            // 如果丢失时间过长，自动释放
            if (lostTicks > MAX_LOST_TICKS) {
                DebugLogger.warn("实体追踪器丢失目标过久，自动释放");
                release();
                return TrackingState.INVALID;
            }
            
            return TrackingState.LOST;
        }
    }
    
    @Override
    public boolean isTracking() {
        return state == TrackingState.TRACKING && getTrackedEntity() != null;
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
        
        DebugLogger.debug("实体追踪器已释放");
    }
    
    /**
     * 通过 UUID 在世界中查找实体
     * 
     * @return 找到的实体，未找到返回 null
     */
    @Nullable
    private Entity findEntityByUUID() {
        if (trackedEntityUUID == null) {
            return null;
        }
        
        // 在搜索范围内查找具有匹配 UUID 的实体
        AABB searchBox = new AABB(
                sourcePos.getX() - searchRadius,
                sourcePos.getY() - searchHeight,
                sourcePos.getZ() - searchRadius,
                sourcePos.getX() + searchRadius + 1,
                sourcePos.getY() + searchHeight + 1,
                sourcePos.getZ() + searchRadius + 1
        );
        
        for (Entity entity : level.getEntities(null, searchBox)) {
            if (entity.getUUID().equals(trackedEntityUUID) && entity.isAlive()) {
                DebugLogger.debug("通过 UUID 重新找到实体: %s", entity.getType().getDescriptionId());
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
