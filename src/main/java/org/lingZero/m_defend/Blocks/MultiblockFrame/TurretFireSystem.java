package org.lingZero.m_defend.Blocks.MultiblockFrame;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/**
 * 炮塔射击系统管理器
 */
public class TurretFireSystem {
    
    private final BaseTurretBlockEntity turret;
    
    private long lastFireTick = 0;
    private int baseFireInterval = 40;
    private boolean hasTarget = false;
    private boolean fireTimerEnabled = true;
    
    private int cachedFireInterval = 40;
    private boolean intervalDirty = true;
    
    public TurretFireSystem(BaseTurretBlockEntity turret) {
        this.turret = turret;
    }
    
    /**
     * Tick更新
     */
    public void tick() {
        if (!fireTimerEnabled) {
            return;
        }
        tryFire();
    }
    
    /**
     * 检查是否可以射击
     */
    public boolean canFire() {
        if (!fireTimerEnabled || !hasTarget || !turret.isActive()) {
            return false;
        }
        
        Level level = turret.getLevel();
        if (level == null) {
            return false;
        }
        
        return level.getGameTime() - lastFireTick >= getActualFireInterval();
    }
    
    /**
     * 尝试射击
     */
    public void tryFire() {
        if (!canFire()) {
            return;
        }
        
        Level level = turret.getLevel();
        if (level != null) {
            lastFireTick = level.getGameTime();
        }
        onFire();
    }
    
    protected void onFire() {
        turret.onFire();
    }
    
    /**
     * 计算实际射击间隔
     */
    protected int getActualFireInterval() {
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
    
    /**
     * 设置目标锁定状态
     */
    public void setTargetLocked(boolean locked) {
        if (this.hasTarget == locked) {
            return;
        }
        
        this.hasTarget = locked;
        this.fireTimerEnabled = locked;
        
        if (locked) {
            resetFireTimer();
        }
    }
    
    /**
     * 检查是否锁定目标
     */
    public boolean hasTarget() {
        return hasTarget;
    }
    
    /**
     * 获取基础射击间隔
     */
    public int getBaseFireInterval() {
        return baseFireInterval;
    }
    
    /**
     * 设置基础射击间隔
     */
    public void setBaseFireInterval(int interval) {
        if (interval > 0 && this.baseFireInterval != interval) {
            this.baseFireInterval = interval;
            markIntervalDirty();
            turret.setChanged();
        }
    }
    
    /**
     * 检查射击计时器是否启用
     */
    public boolean isFireTimerEnabled() {
        return fireTimerEnabled;
    }
    
    /**
     * 设置射击计时器启用状态
     */
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
    
    /**
     * 手动重置射击计时器
     */
    public void resetFireTimer() {
        Level level = turret.getLevel();
        if (level != null) {
            lastFireTick = level.getGameTime();
        }
    }
    
    /**
     * 立即触发一次射击
     */
    public void fireNow() {
        onFire();
    }
    
    /**
     * 释放当前目标
     */
    public void releaseTarget() {
        setTargetLocked(false);
    }
    
    // ==================== NBT 持久化 ====================
    
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
    }
    
    public void writeToNBT(CompoundTag tag) {
        tag.putInt("BaseFireInterval", baseFireInterval);
        tag.putBoolean("FireTimerEnabled", fireTimerEnabled);
    }
}
