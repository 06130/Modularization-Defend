package org.lingZero.m_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lingZero.m_defend.Blocks.MultiblockFrame.BaseTurretBlockEntity;
import org.lingZero.m_defend.DataComponents.TargetFilterData;
import org.lingZero.m_defend.DataComponents.TurretCoreData;
import org.lingZero.m_defend.DataComponents.TurretType;
import org.lingZero.m_defend.Items.TurretCore.frame.TurretCore;
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
     * 自定义 tick 逻辑
     * 更新实体追踪器状态
     */
    @Override
    protected void onCustomTick() {
        if (targetTracker != null) {
            targetTracker.update();
        }
    }
    
    /**
     * 计时器触发回调
     * 根据 triggerInterval 设置的间隔自动调用
     */
    @Override
    protected void onTimerTrigger() {
        DebugLogger.debug("[Turret1] onTimerTrigger 被调用, isActive=%s", isActive());
        
        // 检查是否激活
        if (!isActive()) {
            DebugLogger.debug("[Turret1] 炮塔未激活，跳过攻击");
            return;
        }
        
        // 如果没有追踪器或丢失目标，尝试锁定新目标
        if (targetTracker == null || !targetTracker.isTracking()) {
            DebugLogger.debug("[Turret1] 尝试锁定新目标...");
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
        DebugLogger.debug("[Turret1] lockNewTarget 被调用");
        
        // 从目标选择器槽位获取过滤器
        EntityFilter filter = getTargetFilter();
        
        // 如果没有安装过滤器，不锁定任何目标
        if (filter == null) {
            DebugLogger.debug("未安装目标选择器，无法锁定目标");
            return;
        }
        
        DebugLogger.debug("[Turret1] 开始搜索目标, 半径=30, 高度=15");
        
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
     * 使用持久化的过滤器数据创建对应的 EntityFilter
     * 
     * @return 实体过滤器，如果未安装或无效则返回 null
     */
    private EntityFilter getTargetFilter() {
        // 获取缓存的过滤器数据
        TargetFilterData filterData = getCachedFilterData();
        
        if (filterData == null) {
            return null;
        }
        
        // 根据过滤器类型创建对应的 EntityFilter
        return switch (filterData.filterType()) {
            case HOSTILE -> org.lingZero.m_defend.entity.EntityTrace.EntityFilters.hostileMobs();
            case NEUTRAL -> org.lingZero.m_defend.entity.EntityTrace.EntityFilters.neutralMobs();
            case FRIENDLY -> org.lingZero.m_defend.entity.EntityTrace.EntityFilters.friendlyMobs();
            case PLAYER -> org.lingZero.m_defend.entity.EntityTrace.EntityFilters.players();
            case ENTITY_ID -> {
                // 实体ID过滤器，需要从 Optional 中获取 ID
                if (filterData.entityId().isPresent()) {
                    yield org.lingZero.m_defend.entity.EntityTrace.EntityFilters.byEntityId(
                        filterData.entityId().get()
                    );
                } else {
                    yield null;
                }
            }
        };
    }
    
    /**
     * 执行攻击逻辑
     * 
     * @param target 攻击目标
     */
    private void performAttack(Entity target) {
        DebugLogger.debug("[Turret1] performAttack 被调用, 目标=%s", target.getType().getDescriptionId());
        
        // 获取核心槽位的物品
        var coreStack = coreItem(null);
        
        if (coreStack.isEmpty()) {
            DebugLogger.debug("炮塔核心槽位为空，无法攻击");
            return;
        }
        
        DebugLogger.debug("[Turret1] 核心物品: %s", coreStack.getItem().getDescriptionId());

        // 检查是否为炮塔核心
        if (!(coreStack.getItem() instanceof TurretCore turretCore)) {
            DebugLogger.debug("核心槽位物品不是有效的炮塔核心");
            return;
        }

        // 获取炮塔类型（由炮塔方块决定）
        TurretType turretType = getTurretTypeFromBlock();
        DebugLogger.debug("[Turret1] 炮塔类型: %s", turretType.getSerializedName());
        
        // 获取核心数据
        TurretCoreData coreData =
            org.lingZero.m_defend.Items.TurretCore.frame.TurretCore.getData(coreStack);
        
        // 创建临时的核心数据副本，设置正确的炮塔类型
       TurretCoreData modifiedData =
            coreData.withTurretType(turretType);
        
        // 计算炮塔位置和目标位置
        Vec3 sourcePos = getBlockPos().getCenter();
        Vec3 targetPos = target.position();
        
        DebugLogger.debug("[Turret1] 准备执行攻击, 起点=%s, 目标=%s", sourcePos, targetPos);
        
        // 执行攻击
        boolean success = turretCore.executeAttack(
            getLevel(),
            modifiedData,
            sourcePos,
            target instanceof net.minecraft.world.entity.LivingEntity livingTarget ? livingTarget : null,
            targetPos
        );

        if (success) {
            DebugLogger.debug("炮塔攻击成功");
        } else {
            DebugLogger.debug("炮塔攻击失败");
        }
    }
    
    /**
     * 从炮塔方块获取炮塔类型
     * 
     * @return 炮塔类型
     */
    private org.lingZero.m_defend.DataComponents.TurretType getTurretTypeFromBlock() {
        if (getLevel() != null) {
            var blockState = getLevel().getBlockState(getBlockPos());
            if (blockState.getBlock() instanceof org.lingZero.m_defend.Blocks.MultiblockFrame.BaseTurretBlock turretBlock) {
                return turretBlock.getTurretType();
            }
        }
        return org.lingZero.m_defend.DataComponents.TurretType.NONE;
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
