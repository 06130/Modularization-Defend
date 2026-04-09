package org.lingZero.m_defend.entity.projectile.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.Event.ProjectileChunkHandler;
import org.lingZero.m_defend.entity.projectile.config.ProjectileParameters;
import org.lingZero.m_defend.entity.projectile.controller.CollisionDetector;
import org.lingZero.m_defend.entity.projectile.controller.GuidanceSystem;
import org.lingZero.m_defend.entity.projectile.controller.MotionController;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 抽象子弹实体基类
 * 新的重构架构，整合所有控制器组件
 * 
 * 设计原则：
 * 1. 组合模式：使用 MotionController、GuidanceSystem、CollisionDetector 分离职责
 * 2. 仅处理实体运动和超时检测
 * 3. 自动绕过模组方块
 * 4. 支持制导追踪
 * 5. 标准化结果返回
 */
public abstract class AbstractProjectileEntity extends Projectile {
    
    // 控制器组件
    protected final MotionController motionController;
    protected final GuidanceSystem guidanceSystem;
    protected final CollisionDetector collisionDetector;
    
    // 区块追踪
    @Nullable
    private ChunkPos currentChunkPos;
    
    // 命中结果
    @Nullable
    private ProjectileHitResult lastHitResult;
    
    // 性能优化：tick计数器，用于制导系统
    private int tickCount = 0;
    
    /**
     * 构造函数
     * 
     * @param entityType 实体类型
     * @param level 世界实例
     * @param params 子弹参数配置
     */
    protected AbstractProjectileEntity(EntityType<? extends AbstractProjectileEntity> entityType, 
                                       Level level, 
                                       ProjectileParameters params) {
        super(entityType, level);
        
        // 初始化控制器
        this.motionController = new MotionController(params);
        this.guidanceSystem = new GuidanceSystem();
        this.collisionDetector = new CollisionDetector(level, this);
        
        // 设置制导目标（如果有）
        if (params.hasGuidance()) {
            guidanceSystem.setTarget(params.getGuidedTarget());
        }
        
        // 注册到区块追踪
        this.currentChunkPos = chunkPosition();
        ProjectileChunkHandler.registerProjectileAsAbstract(this);
        
        if (DebugLogger.isDebugEnabled()) {
            DebugLogger.debug("AbstractProjectileEntity 创建: %s, 参数=%s", 
                    getClass().getSimpleName(), params.toString());
        }
    }
    
    /**
     * 实体的 tick 更新
     */
    @Override
    public void tick() {
        // 只在服务端执行
        if (level().isClientSide || level() == null) {
            return;
        }
        
        // 更新碰撞检测器的tick计数
        collisionDetector.incrementTick();
        
        // 检查是否超时
        if (motionController.tick()) {
            if (DebugLogger.isDebugEnabled()) {
                DebugLogger.debug("子弹超时，销毁实体");
            }
            cleanupAndDiscard();
            return;
        }
        
        // 检查是否在已加载的区块中
        if (!isInLoadedChunk()) {
            if (DebugLogger.isDebugEnabled()) {
                DebugLogger.debug("子弹进入未加载区块，销毁实体");
            }
            cleanupAndDiscard();
            return;
        }
        
        // 更新区块追踪
        updateChunkTracking();
        
        // 获取当前位置和速度
        Vec3 currentPos = position();
        Vec3 currentMotion = getDeltaMovement();
        
        if (DebugLogger.isDebugEnabled() && tickCount <= 3) {
            DebugLogger.debug("Tick %d: 当前位置=%s, 当前速度=%s", tickCount, currentPos.toString(), currentMotion.toString());
        }
        
        // 增加tick计数
        tickCount++;
        
        // 应用制导逻辑（传入tick计数以支持缓存）
        if (guidanceSystem.shouldApplyGuidance()) {
            currentMotion = guidanceSystem.applyGuidance(currentMotion, currentPos, tickCount);
        }
        
        // 计算新的运动向量
        Vec3 newMotion = motionController.calculateMovement(currentMotion);
        
        // 更新运动方向
        motionController.updateDirection(newMotion);
        
        // 计算目标位置
        Vec3 targetPos = currentPos.add(newMotion);
        
        if (DebugLogger.isDebugEnabled() && tickCount <= 3) {
            DebugLogger.debug("Tick %d: 新速度=%s, 目标位置=%s", tickCount, newMotion.toString(), targetPos.toString());
        }
        
        // 检测碰撞
        HitResult hitResult = collisionDetector.detectCollision(currentPos, targetPos);
        
        // 处理碰撞并获取最终位置
        Vec3 finalPos = collisionDetector.handleCollision(hitResult, currentPos, targetPos);
        
        // 先设置速度，再设置位置（参考 Iron's Spells 的顺序）
        setDeltaMovement(newMotion);
        setPos(finalPos.x, finalPos.y, finalPos.z);
        
        // 检查是否有有效碰撞
        if (collisionDetector.hasValidCollision(hitResult)) {
            handleHit(hitResult);
        }
        
        // 调用子类的自定义tick逻辑
        onCustomTick();
    }
    
    /**
     * 处理命中事件
     * 
     * @param hitResult 命中结果
     */
    protected void handleHit(HitResult hitResult) {
        long flightTime = motionController.getFlightTime();
        
        switch (hitResult.getType()) {
            case ENTITY -> {
                if (hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                    Entity entity = entityHitResult.getEntity();
                    lastHitResult = ProjectileHitResult.hitEntity(position(), entity, flightTime);
                    onHitEntity(entity);
                }
            }
            case BLOCK -> {
                if (hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHitResult) {
                    var blockState = level().getBlockState(blockHitResult.getBlockPos());
                    lastHitResult = ProjectileHitResult.hitBlock(position(), blockState, flightTime);
                    onHitBlock(blockHitResult);
                }
            }
            case MISS -> {
                lastHitResult = ProjectileHitResult.miss(flightTime);
            }
        }
        
        // 命中后通常销毁子弹（子类可以重写此行为）
        if (shouldDestroyOnHit()) {
            cleanupAndDiscard();
        }
    }
    
    /**
     * 自定义 tick 逻辑
     * 子类可以重写此方法添加自定义行为
     */
    protected void onCustomTick() {
        // 默认实现为空
    }
    
    /**
     * 击中实体时的回调
     * 子类应重写此方法处理击中逻辑
     * 
     * @param entity 被击中的实体
     */
    protected void onHitEntity(@NotNull Entity entity) {
        DebugLogger.debug("子弹击中实体: %s", entity.getType().getDescriptionId());
    }
    
    /**
     * 击中方块时的回调
     * 子类应重写此方法处理击中逻辑
     * 
     * @param hitResult 击中结果
     */
    protected void onHitBlock(@NotNull net.minecraft.world.phys.BlockHitResult hitResult) {
        DebugLogger.debug("子弹击中方块");
    }
    
    /**
     * 检查是否应该在命中后销毁
     * 
     * @return true 如果应该销毁
     */
    protected boolean shouldDestroyOnHit() {
        return true;
    }
    
    /**
     * 更新区块追踪
     */
    private void updateChunkTracking() {
        if (currentChunkPos == null) return;
        
        ChunkPos newChunkPos = chunkPosition();
        if (!currentChunkPos.equals(newChunkPos)) {
            ProjectileChunkHandler.updateProjectileChunkAsAbstract(this, currentChunkPos);
            currentChunkPos = newChunkPos;
        }
    }
    
    /**
     * 清理并销毁实体
     */
    protected void cleanupAndDiscard() {
        // 从区块追踪中移除
        if (currentChunkPos != null) {
            ProjectileChunkHandler.unregisterProjectileAsAbstract(this);
            currentChunkPos = null;
        }
        
        // 销毁实体
        discard();
    }
    
    /**
     * 检查实体是否在已加载的区块中
     * 
     * @return true 如果在已加载区块中
     */
    protected boolean isInLoadedChunk() {
        if (level() == null) {
            return false;
        }
        
        int chunkX = (int) Math.floor(getX()) >> 4;
        int chunkZ = (int) Math.floor(getZ()) >> 4;
        
        return level().hasChunk(chunkX, chunkZ);
    }
    
    /**
     * 获取最后的命中结果
     * 
     * @return 命中结果，若未命中则返回 null
     */
    @Nullable
    public ProjectileHitResult getLastHitResult() {
        return lastHitResult;
    }
    
    /**
     * 获取运动控制器
     * 
     * @return 运动控制器
     */
    public MotionController getMotionController() {
        return motionController;
    }
    
    /**
     * 获取制导系统
     * 
     * @return 制导系统
     */
    public GuidanceSystem getGuidanceSystem() {
        return guidanceSystem;
    }
    
    /**
     * 获取碰撞检测器
     * 
     * @return 碰撞检测器
     */
    public CollisionDetector getCollisionDetector() {
        return collisionDetector;
    }
    
    /**
     * 定义同步数据
     * Entity 基类要求实现此方法
     */
    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        // 基础子弹不需要额外的同步数据
        // 子类可以重写此方法添加自己的同步数据
    }
    
    /**
     * 写入 NBT 数据
     */
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("RemainingLifetime", motionController.getRemainingLifetime());
    }
    
    /**
     * 读取 NBT 数据
     */
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("RemainingLifetime")) {
            // 注意：这里只是示例，实际可能需要更复杂的逻辑
            int remaining = compound.getInt("RemainingLifetime");
            // 可以通过反射或其他方式恢复状态
        }
    }
}
