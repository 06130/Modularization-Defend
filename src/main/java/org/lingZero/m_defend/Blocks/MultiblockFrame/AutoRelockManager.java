package org.lingZero.m_defend.Blocks.MultiblockFrame;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.entity.EntityTrace.EntityTracker;
import org.lingZero.m_defend.entity.EntityTrace.IEntityTracker;

/**
 * 自动重锁定管理器
 * 封装实体追踪器的自动重锁定逻辑，提供即时无冷却重锁定和带冷却正常搜索两种模式
 * 
 * <h2>工作流程</h2>
 * <ol>
 *   <li>onCustomTick() 中检测到 LOST 状态 → 立即调用 tryLock()（无视冷却）</li>
 *   <li>成功 → 继续追踪</li>
 *   <li>失败 → 释放追踪器，由子类在计时器触发时执行带冷却的正常搜索</li>
 * </ol>
 * 
 * <h2>使用示例</h2>
 * <pre>{@code
 * public class MyTurretBlockEntity extends BaseTurretBlockEntity {
 *     // 1. 声明追踪器和重锁定管理器
 *     private EntityTracker targetTracker;
 *     private final AutoRelockManager relockManager = new AutoRelockManager();
 *     
 *     // 2. 在 onCustomTick() 中更新（自动处理丢失检测和即时无冷却重锁定）
 *     @Override
 *     protected void onCustomTick() {
 *         relockManager.update(() -> targetTracker);
 *     }
 *     
 *     // 3. 在计时器触发时检查并执行带冷却的搜索
 *     @Override
 *     protected void onTimerTrigger() {
 *         if (!relockManager.isTracking(() -> targetTracker)) {
 *             // 执行带冷却的搜索逻辑
 *             tryLockWithCooldown();
 *             return;
 *         }
 *         
 *         // 获取目标并攻击
 *         Entity target = relockManager.getTrackedEntity(() -> targetTracker);
 *         if (target != null) {
 *             performAttack(target);
 *         }
 *     }
 *     
 *     // 4. 停用时释放追踪器
 *     @Override
 *     protected void onActiveStateChanged(boolean active) {
 *         if (!active) {
 *             relockManager.release(t -> targetTracker = t, () -> targetTracker);
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <h2>优势</h2>
 * <ul>
 *   <li>✅ 代码复用：所有炮塔共享同一套重锁定逻辑</li>
 *   <li>✅ 职责清晰：框架层管理重锁定，子类只关注业务逻辑</li>
 *   <li>✅ 易于维护：修改重锁定策略只需改一处</li>
 *   <li>✅ 性能优化：无额外对象创建，使用方法引用零开销</li>
 * </ul>
 */
public class AutoRelockManager {
    
    /**
     * 更新重锁定状态
     * 应在实体的 onCustomTick() 中每 tick 调用
     * 
     * @param trackerGetter 获取当前追踪器的方法引用
     */
    public void update(@NotNull java.util.function.Supplier<EntityTracker> trackerGetter) {
        EntityTracker tracker = trackerGetter.get();
        if (tracker == null) {
            return;
        }
        
        IEntityTracker.TrackingState state = tracker.update();
        
        // 如果目标丢失，立即尝试重锁定（无视冷却）
        if (state == IEntityTracker.TrackingState.LOST) {
            boolean relocked = tracker.tryLock();
            
            // 重锁定失败，释放追踪器
            if (!relocked) {
                tracker.release();
            }
        }
    }
    
    /**
     * 检查是否正在追踪目标
     * 
     * @param trackerGetter 获取当前追踪器的方法引用
     * @return true 如果正在追踪有效目标
     */
    public boolean isTracking(@NotNull java.util.function.Supplier<EntityTracker> trackerGetter) {
        EntityTracker tracker = trackerGetter.get();
        return tracker != null && tracker.isTracking();
    }
    
    /**
     * 获取当前追踪的实体
     * 
     * @param trackerGetter 获取当前追踪器的方法引用
     * @return 追踪的实体，未追踪返回 null
     */
    @Nullable
    public Entity getTrackedEntity(@NotNull java.util.function.Supplier<EntityTracker> trackerGetter) {
        EntityTracker tracker = trackerGetter.get();
        return tracker != null ? tracker.getTrackedEntity() : null;
    }
    
    /**
     * 释放当前追踪器
     * 
     * @param trackerSetter 设置追踪器的方法引用（用于置 null）
     * @param trackerGetter 获取当前追踪器的方法引用
     */
    public void release(
            @NotNull java.util.function.Consumer<EntityTracker> trackerSetter,
            @NotNull java.util.function.Supplier<EntityTracker> trackerGetter) {
        EntityTracker tracker = trackerGetter.get();
        if (tracker != null) {
            tracker.release();
            trackerSetter.accept(null);
        }
    }
}
