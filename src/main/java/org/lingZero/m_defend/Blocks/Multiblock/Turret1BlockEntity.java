package org.lingZero.m_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.m_defend.Blocks.MultiblockFrame.BaseTurretBlockEntity;
import org.lingZero.m_defend.Register.ModBlockEntities;
import org.lingZero.m_defend.entity.EntityTrace.EntityFilter;
import org.lingZero.m_defend.entity.EntityTrace.EntityTracker;
import org.lingZero.m_defend.entity.EntityTrace.IEntitySearch;
import org.lingZero.m_defend.util.DebugLogger;

public class Turret1BlockEntity extends BaseTurretBlockEntity {
    
    // 实体追踪器
    private EntityTracker targetTracker;
    
    public Turret1BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET1_BLOCK_ENTITY.get(), pos, state);
        // 设置默认触发间隔为 20 ticks (1秒)
        setTriggerInterval(20);
    }
    
    /**
     * 服务端 tick 方法
     * 每个游戏刻调用一次
     */
    @Override
    public void serverTick() {
        super.serverTick();
        
        // 更新追踪器状态
        if (targetTracker != null) {
            targetTracker.update();
        }
        
        // 更新计时器（激活时会自动触发 onTimerTrigger）
        updateTimer();
    }
    
    /**
     * 计时器触发回调
     * 根据 triggerInterval 设置的间隔自动调用
     */
    @Override
    protected void onTimerTrigger() {
        // 检查是否激活
        if (!isActive()) {
            return;
        }
        
        // 如果没有追踪器或丢失目标，尝试锁定新目标
        if (targetTracker == null || !targetTracker.isTracking()) {
            lockNewTarget();
            return;
        }
        
        // 如果正在追踪目标，执行攻击
        if (targetTracker.isTracking()) {
            Entity target = targetTracker.getTrackedEntity();
            if (target != null) {
                double distance = target.distanceToSqr(getBlockPos().getCenter());
                
                // 检查目标是否在射程内（假设射程 30 格）
                if (distance <= 900.0) {
                    DebugLogger.debug("炮塔射击！目标: %s, 距离: %.2f", 
                            target.getType().getDescriptionId(), 
                            Math.sqrt(distance));
                    // TODO: 执行攻击逻辑
                    performAttack(target);
                } else {
                    DebugLogger.debug("目标超出射程 (%.2f > 30)，释放锁定", Math.sqrt(distance));
                    targetTracker.release();
                    targetTracker = null;
                }
            } else {
                // 实体引用丢失，重新搜索
                DebugLogger.debug("目标实体引用丢失，重新搜索...");
                targetTracker = null;
            }
        }
    }
    
    /**
     * 锁定新目标
     */
    private void lockNewTarget() {
        // 从目标选择器槽位获取过滤器
        EntityFilter filter = getTargetFilter();
        
        // 如果没有安装过滤器，不锁定任何目标
        if (filter == null) {
            DebugLogger.debug("未安装目标选择器，无法锁定目标");
            return;
        }
        
        targetTracker = IEntitySearch.createAndLockTracker(
                getLevel(),
                getBlockPos(),
                30.0,  // 搜索半径 30 格
                15.0,  // 搜索高度 15 格
                filter
        );
        
        if (targetTracker.isTracking()) {
            DebugLogger.info("炮塔锁定新目标: %s", 
                    targetTracker.getTrackedEntity().getType().getDescriptionId());
        } else {
            DebugLogger.debug("未找到可锁定的目标");
        }
    }
    
    /**
     * 从目标选择器槽位获取实体过滤器
     * TODO: 后续通过 DataComponent 从物品中读取过滤器配置
     * 
     * @return 实体过滤器，如果未安装或无效则返回 null
     */
    private EntityFilter getTargetFilter() {
        ItemStack selectorItem = targetSelectorItem(null);
        
        // 检查槽位是否为空
        if (selectorItem.isEmpty()) {
            return null;
        }
        
        // TODO: 从物品的 DataComponent 中读取过滤器配置
        // 例如：
        // TargetSelectorComponent component = selectorItem.get(ModDataComponents.TARGET_SELECTOR.get());
        // if (component != null) {
        //     return component.getFilter();
        // }
        
        // 目前返回 null，使用默认过滤器
        return null;
    }
    
    /**
     * 执行攻击逻辑
     * 
     * @param target 攻击目标
     */
    private void performAttack(Entity target) {
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
            setTriggerInterval(Math.max(1, interval)); // 至少 1 tick
            DebugLogger.info("设置射击频率: %.1f 发/秒 (间隔: %d ticks)", 
                    shotsPerSecond, interval);
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
            Entity target = targetTracker.getTrackedEntity();
            if (target != null) {
                DebugLogger.info("手动开火！目标: %s", 
                        target.getType().getDescriptionId());
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
