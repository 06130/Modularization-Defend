package org.lingZero.m_defend.Blocks.MultiblockFrame;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.entity.EntityTrace.EntityTracker;

/**
 * 炮塔射击系统管理器
 * 整合了目标搜索、冷却、重锁定以及射击判定
 */
public class TurretFireSystem {

    private final BaseTurretBlockEntity turret;

    // 射击冷却相关
    private long lastFireTick = 0;
    private int baseFireInterval = 40;          // 基础射击间隔(tick)
    private boolean fireTimerEnabled = true;    // 射击计时器总开关

    // 目标相关
    @Nullable
    private EntityTracker targetTracker;        // 当前追踪的目标
    private boolean hasTarget = false;          // 是否拥有有效目标

    // 搜索冷却
    private int searchCooldownTicks = 40;       // 搜索间隔(tick)
    private long lastSearchTick = 0;

    // 射击间隔缓存（避免每 tick 重复计算）
    private int cachedFireInterval = 40;
    private boolean intervalDirty = true;

    public TurretFireSystem(BaseTurretBlockEntity turret) {
        this.turret = turret;
    }

    // ==================== 每 Tick 更新 ====================

    public void tick() {
        if (!fireTimerEnabled || !turret.isActive()) {
            return;
        }

        Level level = turret.getLevel();
        if (level == null) {
            return;
        }

        // 1. 处理已有目标的丢失/重锁定
        if (hasTarget) {
            if (targetTracker == null || !targetTracker.isTracking()) {
                handleTargetLost();
            }
        }

        // 2. 如果没有目标，尝试搜索
        if (!hasTarget) {
            trySearch(level);
        }

        // 3. 射击判定
        tryFire(level);
    }

    // ==================== 目标管理 ====================

    /**
     * 处理目标丢失：尝试即时重锁定，失败则释放
     */
    private void handleTargetLost() {
        if (targetTracker != null) {
            boolean relocked = targetTracker.tryLock();
            if (!relocked) {
                // 重锁定失败 → 彻底释放
                targetTracker.release();
                targetTracker = null;
                setTargetLocked(false);
                // 通知炮塔（可用于音效等）
                turret.onTargetLost("target_lost_and_relock_failed");
            }
            // 如果 tryLock 成功，目标仍然有效，保持 hasTarget=true
        } else {
            setTargetLocked(false);
        }
    }

    /**
     * 带冷却的搜索新目标
     */
    private void trySearch(Level level) {
        long currentTick = level.getGameTime();
        if (currentTick - lastSearchTick < searchCooldownTicks) {
            return;
        }
        lastSearchTick = currentTick;

        EntityTracker newTracker = turret.acquireTarget();
        if (newTracker != null && newTracker.isTracking()) {
            this.targetTracker = newTracker;
            setTargetLocked(true);
        }
    }

    /**
     * 更新目标锁定状态
     */
    public void setTargetLocked(boolean locked) {
        if (this.hasTarget == locked) {
            return;
        }
        this.hasTarget = locked;
        if (locked) {
            resetFireTimer();   // 获得目标时重置射击计时器，立刻可以攻击
        }
    }

    /**
     * 彻底释放目标（被外部停用等情况调用）
     */
    public void releaseTarget() {
        if (targetTracker != null) {
            targetTracker.release();
            targetTracker = null;
        }
        setTargetLocked(false);
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    // ==================== 射击逻辑 ====================

    private void tryFire(Level level) {
        if (!canFire(level)) {
            return;
        }

        // 获取当前追踪实体
        Entity target = targetTracker != null ? targetTracker.getTrackedEntity() : null;
        if (target == null || target.isRemoved()) {
            // 实体无效，释放目标
            releaseTarget();
            return;
        }

        lastFireTick = level.getGameTime();
        turret.performFire(target);   // 委托给炮塔的具体攻击实现
    }

    private boolean canFire(Level level) {
        if (!hasTarget || !fireTimerEnabled || !turret.isActive()) {
            return false;
        }
        return level.getGameTime() - lastFireTick >= getActualFireInterval();
    }

    // ==================== 射击间隔 ====================

    private int getActualFireInterval() {
        if (!intervalDirty) {
            return cachedFireInterval;
        }
        cachedFireInterval = turret.getActualFireInterval();
        intervalDirty = false;
        return cachedFireInterval;
    }

    private void markIntervalDirty() {
        intervalDirty = true;
    }

    public int getBaseFireInterval() {
        return baseFireInterval;
    }

    public void setBaseFireInterval(int interval) {
        if (interval > 0 && this.baseFireInterval != interval) {
            this.baseFireInterval = interval;
            markIntervalDirty();
            turret.setChanged();
        }
    }

    // ==================== 搜索冷却配置 ====================

    public void setSearchCooldown(int ticks) {
        this.searchCooldownTicks = Math.max(1, ticks);
    }

    // ==================== 计时器控制 ====================

    public boolean isFireTimerEnabled() {
        return fireTimerEnabled;
    }

    public void setFireTimerEnabled(boolean enabled) {
        this.fireTimerEnabled = enabled;
        if (!enabled) {
            Level level = turret.getLevel();
            if (level != null) {
                lastFireTick = level.getGameTime();
            }
        }
        turret.setChanged();
    }

    public void resetFireTimer() {
        Level level = turret.getLevel();
        if (level != null) {
            lastFireTick = level.getGameTime();
        }
    }

    // ==================== 持久化 ====================

    public void readFromNBT(CompoundTag tag) {
        if (tag.contains("BaseFireInterval")) {
            int newInterval = tag.getInt("BaseFireInterval");
            if (this.baseFireInterval != newInterval) {
                this.baseFireInterval = newInterval;
                markIntervalDirty();
            }
        }
        if (tag.contains("FireTimerEnabled")) {
            this.fireTimerEnabled = tag.getBoolean("FireTimerEnabled");
        }
        // 目标追踪器和运行时状态不持久化，区块重载后重新搜索
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putInt("BaseFireInterval", baseFireInterval);
        tag.putBoolean("FireTimerEnabled", fireTimerEnabled);
    }
}