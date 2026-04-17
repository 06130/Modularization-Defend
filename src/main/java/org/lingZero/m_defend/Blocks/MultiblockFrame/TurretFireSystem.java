package org.lingZero.m_defend.Blocks.MultiblockFrame;

import net.minecraft.world.level.Level;

/**
 * 炮塔射击系统管理器
 * 封装所有射击相关的逻辑，包括射击间隔计算、目标锁定状态管理、射击触发等
 * 
 * <p>使用示例：
 * <pre>{@code
 * // 在BlockEntity中创建实例
 * private final TurretFireSystem fireSystem = new TurretFireSystem(this);
 * 
 * // 在tick中调用
 * fireSystem.tick();
 * }</pre>
 */
public class TurretFireSystem {
    
    private final BaseTurretBlockEntity turret;
    
    // 射击状态
    private long lastFireTick = 0;                // 上次射击的游戏刻
    private int baseFireInterval = 40;            // 基础射击间隔（tick），默认2秒
    private boolean hasTarget = false;            // 是否锁定目标
    private boolean fireTimerEnabled = true;      // 射击计时器是否启用
    
    // 性能优化：缓存实际射击间隔，避免每tick重复计算
    private int cachedFireInterval = 40;          // 缓存的实际射击间隔
    private boolean intervalDirty = true;         // 标记是否需要重新计算间隔
    
    /**
     * 构造函数
     * 
     * @param turret 所属的炮塔BlockEntity
     */
    public TurretFireSystem(BaseTurretBlockEntity turret) {
        this.turret = turret;
    }
    
    /**
     * Tick更新
     * 应在BlockEntity的tick方法中调用
     * 优化：未启用射击计时器时直接返回，避免不必要的计算
     */
    public void tick() {
        // 快速失败：未启用射击计时器时跳过所有检查
        if (!fireTimerEnabled) {
            return;
        }
        
        tryFire();
    }
    
    /**
     * 检查是否可以射击
     * 基于射击间隔和目标锁定状态判断
     * 优化：将最廉价的检查放在前面，快速失败
     * 
     * @return true 如果可以射击
     */
    public boolean canFire() {
        // 快速失败：先检查最简单的boolean条件
        if (!fireTimerEnabled || !hasTarget) {
            return false;
        }
        
        // 检查炮塔激活状态（可能涉及额外计算）
        if (!turret.isActive()) {
            return false;
        }
        
        // 获取level（可能为null）
        net.minecraft.world.level.Level level = turret.getLevel();
        if (level == null) {
            return false;
        }
        
        // 最后才进行时间计算（相对昂贵）
        long currentTick = level.getGameTime();
        int actualInterval = getActualFireInterval();
        
        return currentTick - lastFireTick >= actualInterval;
    }
    
    /**
     * 尝试射击
     * 如果满足条件则执行射击
     * 优化：减少重复的level获取
     */
    public void tryFire() {
        if (!canFire()) {
            return;
        }
        
        // 只需要获取一次level
        net.minecraft.world.level.Level level = turret.getLevel();
        if (level != null) {
            lastFireTick = level.getGameTime();
        }
        
        // 触发攻击
        onFire();
    }
    
    /**
     * 射击回调方法
     * 子类应重写此方法以实现具体的攻击逻辑
     */
    protected void onFire() {
        turret.onFire();
    }
    
    /**
     * 计算实际射击间隔（应用增益系统）
     * 使用缓存机制，仅在配置变化时重新计算
     * 
     * @return 实际射击间隔（tick）
     */
    protected int getActualFireInterval() {
        // 如果缓存有效，直接返回
        if (!intervalDirty) {
            return cachedFireInterval;
        }
        
        // 重新计算并缓存
        cachedFireInterval = turret.getActualFireInterval();
        intervalDirty = false;
        return cachedFireInterval;
    }
    
    /**
     * 标记射击间隔需要重新计算
     * 在修改baseFireInterval或核心数据变化时调用
     */
    private void markIntervalDirty() {
        intervalDirty = true;
    }
    
    /**
     * 设置目标锁定状态
     * 优化：自动启用/禁用射击计时器，无目标时跳过射击检查
     * 
     * @param locked 是否锁定目标
     */
    public void setTargetLocked(boolean locked) {
        if (this.hasTarget == locked) {
            return; // 状态未变化，无需处理
        }
        
        this.hasTarget = locked;
        
        if (locked) {
            // 锁定目标时启用射击计时器
            this.fireTimerEnabled = true;
            // 重置射击计时器，立即可以射击
            resetFireTimer();
        } else {
            // 失去目标时禁用射击计时器，避免不必要的tick检查
            this.fireTimerEnabled = false;
        }
    }
    
    /**
     * 检查是否锁定目标
     * 
     * @return 是否锁定目标
     */
    public boolean hasTarget() {
        return this.hasTarget;
    }
    
    // ==================== Getter/Setter ====================
    
    /**
     * 获取基础射击间隔
     * 
     * @return 基础射击间隔（tick）
     */
    public int getBaseFireInterval() {
        return baseFireInterval;
    }
    
    /**
     * 设置基础射击间隔
     * 
     * @param interval 新的基础射击间隔（tick），必须大于 0
     */
    public void setBaseFireInterval(int interval) {
        if (interval > 0 && this.baseFireInterval != interval) {
            this.baseFireInterval = interval;
            markIntervalDirty(); // 标记需要重新计算
            turret.setChanged();
        }
    }
    
    /**
     * 获取上次射击的游戏刻
     * 
     * @return 上次射击的刻数
     */
    public long getLastFireTick() {
        return lastFireTick;
    }
    
    /**
     * 检查射击计时器是否启用
     * 
     * @return 是否启用
     */
    public boolean isFireTimerEnabled() {
        return fireTimerEnabled;
    }
    
    /**
     * 设置射击计时器启用状态
     * 
     * @param enabled 是否启用
     */
    public void setFireTimerEnabled(boolean enabled) {
        this.fireTimerEnabled = enabled;
        if (!enabled) {
            Level level = turret.getLevel();
            if (level != null) {
                lastFireTick = level.getGameTime(); // 禁用时重置为当前时间
            }
        }
        turret.setChanged();
    }
    
    /**
     * 手动重置射击计时器
     * 将下次射击时间设置为当前时间
     */
    public void resetFireTimer() {
        Level level = turret.getLevel();
        if (level != null) {
            lastFireTick = level.getGameTime();
        }
    }
    
    /**
     * 立即触发一次射击
     * 不会重置计数器，也不会检查间隔
     */
    public void fireNow() {
        onFire();
    }
    
    // ==================== NBT 持久化 ====================
    
    /**
     * 从NBT读取数据
     * 
     * @param tag NBT标签
     */
    public void readFromNBT(net.minecraft.nbt.CompoundTag tag) {
        if (tag.contains("BaseFireInterval")) {
            int newInterval = tag.getInt("BaseFireInterval");
            if (this.baseFireInterval != newInterval) {
                this.baseFireInterval = newInterval;
                markIntervalDirty(); // 标记需要重新计算
            }
        }
        if (tag.contains("FireTimerEnabled")) {
            this.fireTimerEnabled = tag.getBoolean("FireTimerEnabled");
        }
        // lastFireTick 和 hasTarget 不持久化，因为它们是运行时状态
    }
    
    /**
     * 写入NBT数据
     * 
     * @param tag NBT标签
     */
    public void writeToNBT(net.minecraft.nbt.CompoundTag tag) {
        tag.putInt("BaseFireInterval", baseFireInterval);
        tag.putBoolean("FireTimerEnabled", fireTimerEnabled);
        // lastFireTick 和 hasTarget 不持久化
    }
}
