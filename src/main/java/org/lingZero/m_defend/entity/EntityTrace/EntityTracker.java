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
 */
public class EntityTracker implements IEntityTracker {

    private final Level level;
    private final BlockPos sourcePos;
    private final double searchRadius;
    private final double searchHeight;
    private final EntityFilter filter;
    private final boolean fastMode;

    // 性能优化：预计算的值
    private final float searchRadiusSq;
    private final AABB searchBox;
    private final float sourceCenterX;
    private final float sourceCenterY;
    private final float sourceCenterZ;

    // 追踪状态
    private UUID trackedEntityUUID;
    private Entity cachedEntity;
    private TrackingState state;


    // 回调监听器
    private Consumer<String> targetLostCallback;

    public EntityTracker(
            @NotNull Level level,
            @NotNull BlockPos sourcePos,
            double searchRadius,
            double searchHeight,
            @NotNull EntityFilter filter) {
        this(level, sourcePos, searchRadius, searchHeight, filter, false);
    }

    public EntityTracker(
            @NotNull Level level,
            @NotNull BlockPos sourcePos,
            double searchRadius,
            double searchHeight,
            @NotNull EntityFilter filter,
            boolean fastMode) {

        this.level = level;
        this.sourcePos = sourcePos;
        this.searchRadius = searchRadius;
        this.searchHeight = searchHeight;
        this.filter = filter;
        this.fastMode = fastMode;
        this.trackedEntityUUID = null;
        this.cachedEntity = null;
        this.state = TrackingState.UNLOCKED;
        this.targetLostCallback = null;

        this.searchRadiusSq = (float) (searchRadius * searchRadius);
        this.searchBox = new AABB(
                sourcePos.getX() - searchRadius,
                sourcePos.getY() - searchHeight,
                sourcePos.getZ() - searchRadius,
                sourcePos.getX() + searchRadius + 1,
                sourcePos.getY() + searchHeight + 1,
                sourcePos.getZ() + searchRadius + 1
        );
        Vec3 center = sourcePos.getCenter();
        this.sourceCenterX = (float) center.x;
        this.sourceCenterY = (float) center.y;
        this.sourceCenterZ = (float) center.z;
    }

    /**
     * 尝试锁定最近的符合条件的实体
     * @return true 如果已在追踪或成功锁定新目标
     */
    public boolean tryLock() {
        if (isTracking()) {
            return true;
        }

        EntitySearchResult result = fastMode
                ? EntitySearchUtil.findNearestEntityFast(level, sourcePos, searchRadius, searchHeight, filter)
                : EntitySearchUtil.findNearestEntity(level, sourcePos, searchRadius, searchHeight, filter);

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

    public void lockOn(@NotNull Entity entity) {
        if (!filter.test(entity)) {
            DebugLogger.warn("尝试锁定不符合过滤条件的实体: %s", entity.getType().getDescriptionId());
            return;
        }
        this.trackedEntityUUID = entity.getUUID();
        this.cachedEntity = entity;
        this.state = TrackingState.TRACKING;
        DebugLogger.debug("实体追踪器已锁定: %s", entity.getType().getDescriptionId());
    }

    @Override
    @Nullable
    public Entity getTrackedEntity() {
        if (state != TrackingState.TRACKING) {
            return null;
        }
        // 缓存有效则直接返回
        if (cachedEntity != null && cachedEntity.isAlive()
                && cachedEntity.getUUID().equals(trackedEntityUUID)) {
            return cachedEntity;
        }
        // 缓存失效，通过 UUID 重新查找
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
        switch (state) {
            case INVALID -> { return TrackingState.INVALID; }
            case UNLOCKED -> { return TrackingState.UNLOCKED; }
            // TRACKING 和 LOST 需要进一步检查
            default -> { }
        }

        Entity currentEntity = getTrackedEntity();

        if (currentEntity != null && currentEntity.isAlive()) {
            float dx = (float) currentEntity.getX() - sourceCenterX;
            float dy = (float) currentEntity.getY() - sourceCenterY;
            float dz = (float) currentEntity.getZ() - sourceCenterZ;

            if (dx * dx + dy * dy + dz * dz > searchRadiusSq) {
                triggerTargetLostCallback("out_of_range");
                state = TrackingState.LOST;
                return TrackingState.LOST;
            }

            state = TrackingState.TRACKING;
            return TrackingState.TRACKING;
        } else {
            triggerTargetLostCallback("not_found");
            state = TrackingState.LOST;
            return TrackingState.LOST;
        }
    }

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
        return state == TrackingState.TRACKING
                && cachedEntity != null
                && cachedEntity.isAlive();
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
        // 保留回调注册——release() 之后可能被重用
        DebugLogger.debug("实体追踪器已释放");
    }

    @Override
    public void onTargetLost(Consumer<String> callback) {
        this.targetLostCallback = callback;
    }

    @Override
    public boolean forceRelock() {
        DebugLogger.debug("实体追踪器：手动触发重新锁定");
        // 只清除追踪状态和缓存，保留回调和过滤器配置
        this.trackedEntityUUID = null;
        this.cachedEntity = null;
        this.state = TrackingState.UNLOCKED;
        return tryLock();
    }

    @Nullable
    private Entity findEntityByUUID() {
        if (trackedEntityUUID == null) {
            return null;
        }
        for (Entity entity : level.getEntities(null, searchBox)) {
            if (entity.isAlive() && entity.getUUID().equals(trackedEntityUUID)) {
                return entity;
            }
        }
        return null;
    }

}