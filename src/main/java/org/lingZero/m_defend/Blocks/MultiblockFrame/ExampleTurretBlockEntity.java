package org.lingZero.m_defend.Blocks.MultiblockFrame;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.m_defend.entity.EntityTrace.EntityFilters;
import org.lingZero.m_defend.entity.EntityTrace.EntityTracker;
import org.lingZero.m_defend.entity.EntityTrace.IEntitySearch;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 炮塔方块实体示例
 * 演示如何使用 BaseTurretBlockEntity 的计时器功能
 */
public class ExampleTurretBlockEntity extends BaseTurretBlockEntity {
    
    private EntityTracker targetTracker;
    
    public ExampleTurretBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    /**
     * 方块实体的 tick 方法
     * 每个游戏刻都会调用
     */
    @Override
    public void serverTick() {
        super.serverTick();
        
        // 更新追踪器
        if (targetTracker != null) {
            targetTracker.update();
        }
        
        // 更新计时器（当激活时会自动触发 onTimerTrigger）
        updateTimer();
    }
    
    /**
     * 计时器触发回调
     * 根据 triggerInterval 设置的间隔自动调用
     * 例如：triggerInterval = 20 表示每 20 ticks（1秒）触发一次
     */
    @Override
    protected void onTimerTrigger() {
        // 检查是否激活
        if (!isActive()) {
            return;
        }
        
        // 示例1：如果没有追踪器，尝试锁定目标
        if (targetTracker == null || !targetTracker.isTracking()) {
            targetTracker = IEntitySearch.createAndLockTracker(
                    getLevel(), 
                    getBlockPos(), 
                    30.0,  // 搜索半径30格
                    15.0,  // 搜索高度15格
                    EntityFilters.hostileMobs()  // 只追踪敌对生物
            );
            
            if (targetTracker.isTracking()) {
                DebugLogger.info("炮塔锁定新目标");
            }
            return;
        }
        
        // 示例2：如果正在追踪目标，执行攻击
        if (targetTracker.isTracking()) {
            var target = targetTracker.getTrackedEntity();
            if (target != null) {
                double distance = target.distanceToSqr(getBlockPos().getCenter());
                
                // 检查目标是否在射程内（假设射程20格）
                if (distance <= 400.0) {
                    DebugLogger.debug("炮塔射击！目标距离: %.2f", Math.sqrt(distance));
                    // 执行射击逻辑...
                    performAttack(target);
                } else {
                    DebugLogger.debug("目标超出射程，释放锁定");
                    targetTracker.release();
                }
            }
        }
    }
    
    /**
     * 执行攻击逻辑
     * 
     * @param target 攻击目标
     */
    private void performAttack(net.minecraft.world.entity.Entity target) {
        // TODO: 实现具体的攻击逻辑
        // 例如：生成投射物、造成伤害、播放音效等
    }
    
    /**
     * 设置射击频率
     * 
     * @param shotsPerSecond 每秒射击次数
     */
    public void setShotsPerSecond(double shotsPerSecond) {
        if (shotsPerSecond > 0) {
            // 将每秒射击次数转换为 tick 间隔
            int interval = (int) (20.0 / shotsPerSecond);
            setTriggerInterval(Math.max(1, interval)); // 至少1 tick
            DebugLogger.info("设置射击频率: %.1f 发/秒 (间隔: %d ticks)", shotsPerSecond, interval);
        }
    }
    
    /**
     * 获取当前射击频率
     * 
     * @return 每秒射击次数
     */
    public double getShotsPerSecond() {
        return 20.0 / getTriggerInterval();
    }
    
    /**
     * 手动开火（立即触发一次攻击）
     */
    public void manualFire() {
        if (isActive() && targetTracker != null && targetTracker.isTracking()) {
            var target = targetTracker.getTrackedEntity();
            if (target != null) {
                DebugLogger.info("手动开火！");
                performAttack(target);
            }
        }
    }
    
    /**
     * 停止追踪并重置
     */
    public void stopAndReset() {
        if (targetTracker != null) {
            targetTracker.release();
            targetTracker = null;
        }
        resetTimer();
        DebugLogger.info("炮塔已停止并重置");
    }
}
