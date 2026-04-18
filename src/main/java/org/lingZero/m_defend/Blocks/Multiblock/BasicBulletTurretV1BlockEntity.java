package org.lingZero.m_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.m_defend.Blocks.MultiblockFrame.BaseTurretBlock;
import org.lingZero.m_defend.Blocks.MultiblockFrame.BaseTurretBlockEntity;
import org.lingZero.m_defend.Config;
import org.lingZero.m_defend.DataComponents.TargetFilterData;
import org.lingZero.m_defend.DataComponents.TurretCoreData;
import org.lingZero.m_defend.DataComponents.TurretType;
import org.lingZero.m_defend.Items.TurretCore.frame.TurretCore;
import org.lingZero.m_defend.Register.ModBlockEntities;
import org.lingZero.m_defend.entity.EntityTrace.*;

public class BasicBulletTurretV1BlockEntity extends BaseTurretBlockEntity {
    
    // 实体追踪器
    private EntityTracker targetTracker;
    
    // 重新搜索冷却（tick）
    private long lastSearchTick = 0;
    private static final int SEARCH_COOLDOWN = 40; // 2秒
    
    public BasicBulletTurretV1BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_BULLET_TURRET_V1_BLOCK_ENTITY.get(), pos, state);
        // 从配置读取基础射速，转换为 tick 间隔
        int baseInterval = (int) (20.0 / Config.TURRET.basicBulletV1.fireRate.get());
        fireSystem.setBaseFireInterval(Math.max(1, baseInterval));
    }
    
    /**
     * 自定义 tick 逻辑
     * 更新实体追踪器状态，并在目标丢失时自动重锁定（仅一次）
     */
    @Override
    protected void onCustomTick() {
        if (targetTracker == null) {
            return;
        }
        
        IEntityTracker.TrackingState state = targetTracker.update();
        
        // 如果目标丢失，立即尝试重新锁定（只触发一次）
        if (state == IEntityTracker.TrackingState.LOST) {
            boolean relocked = targetTracker.tryLock();
            
            // 如果重新锁定失败，释放追踪器，等待下次计时器触发时重新搜索
            if (!relocked) {
                targetTracker.release();
                targetTracker = null;
            }
        }
    }
    
    /**
     * 计时器触发回调
     * 负责目标搜索和攻击执行
     */
    @Override
    protected void onTimerTrigger() {
        if (!isActive()) {
            return;
        }
        
        // 如果没有追踪器，尝试锁定新目标（自动锁定跟随计时器）
        if (targetTracker == null) {
            tryLockNewTarget();
            return;
        }
        
        // 如果未追踪到目标，尝试重新搜索
        if (!targetTracker.isTracking()) {
            tryLockNewTarget();
            return;
        }
        
        // 执行攻击
        Entity target = targetTracker.getTrackedEntity();
        if (target != null) {
            double distance = target.distanceToSqr(getBlockPos().getCenter());
            int range = Config.TURRET.basicBulletV1.range.get();
            
            if (distance <= range * range) {
                performAttack(target);
            } else {
                // 目标超出射程，释放锁定
                targetTracker.release();
                targetTracker = null;
            }
        } else {
            // 实体引用丢失
            targetTracker = null;
        }
    }
    
    /**
     * 尝试锁定新目标（带冷却检查）
     */
    private void tryLockNewTarget() {
        long currentTick = level != null ? level.getGameTime() : 0;
        if (currentTick - lastSearchTick < SEARCH_COOLDOWN) {
            return;
        }
        
        EntityFilter filter = getTargetFilter();
        if (filter == null) {
            return;
        }
        
        targetTracker = IEntitySearch.createAndLockTracker(
                getLevel(),
                getBlockPos(),
                Config.TURRET.basicBulletV1.range.get(),
                Config.TURRET.basicBulletV1.searchHeight.get(),
                filter
        );
        
        if (level != null) {
            lastSearchTick = level.getGameTime();
        }
        
        if (targetTracker != null) {
            targetTracker.onTargetLost(this::onTargetLost);
        }
    }
    
    /**
     * 从目标选择器槽位获取实体过滤器
     */
    private EntityFilter getTargetFilter() {
        TargetFilterData filterData = getCachedFilterData();
        if (filterData == null) {
            return null;
        }
        
        return switch (filterData.filterType()) {
            case HOSTILE -> EntityFilters.hostileMobs();
            case NEUTRAL -> EntityFilters.neutralMobs();
            case FRIENDLY -> EntityFilters.friendlyMobs();
            case PLAYER -> EntityFilters.players();
            case ENTITY_ID -> filterData.entityId()
                    .map(EntityFilters::byEntityId)
                    .orElse(null);
        };
    }
    
    /**
     * 执行攻击逻辑
     */
    private void performAttack(Entity target) {
        var coreStack = coreItem(null);
        if (coreStack.isEmpty() || !(coreStack.getItem() instanceof TurretCore turretCore)) {
            return;
        }

        TurretType turretType = getTurretTypeFromBlock();
        TurretCoreData modifiedData = TurretCore.getData(coreStack).withTurretType(turretType);
        
        turretCore.executeAttack(
            getLevel(),
            modifiedData,
            getBlockPos().getCenter(),
            target instanceof LivingEntity livingTarget ? livingTarget : null,
            target.position()
        );
    }
    
    /**
     * 从炮塔方块获取炮塔类型
     */
    private TurretType getTurretTypeFromBlock() {
        if (getLevel() != null) {
            var blockState = getLevel().getBlockState(getBlockPos());
            if (blockState.getBlock() instanceof BaseTurretBlock turretBlock) {
                return turretBlock.getTurretType();
            }
        }
        return TurretType.LASER;
    }
    
    /**
     * 重写激活状态变化回调
     */
    @Override
    protected void onActiveStateChanged(boolean active) {
        super.onActiveStateChanged(active);
        
        if (!active) {
            // 停用时释放追踪器
            if (targetTracker != null) {
                targetTracker.release();
                targetTracker = null;
            }
        }
        // 不在这里自动搜索，等待计时器触发时再搜索
    }
    
    /**
     * 重写目标丢失回调处理
     * 注意：不要将 targetTracker 设为 null，让 tryLock() 处理状态转换
     */
    @Override
    protected void onTargetLost(String reason) {
        fireSystem.releaseTarget();
        // 不调用 targetTracker = null，保持追踪器对象以便重新锁定
    }
}
