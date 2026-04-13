package org.lingZero.m_defend.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.util.DebugLogger;

import java.util.UUID;

/**
 * 基础投射物实体 - 完全参考 Iron's Spells 'n Spellbooks 的实现
 * 
 * 关键特性：
 * 1. 调用 super.tick() 处理基本逻辑
 * 2. 手动调用 setPos(position().add(getDeltaMovement())) 更新位置
 * 3. 使用 ProjectileUtil.getHitResultOnMoveVector 进行碰撞检测
 * 4. 自动支持客户端平滑插值（基于 Minecraft 内置机制）
 * 
 * 使用说明：
 * - 子类只需重写 onHitEntity() 和 onHitBlock() 方法处理命中逻辑
 * - 通过 shoot() 方法设置初始速度和方向
 * - 可选：通过 setHomingTarget() 设置制导目标
 * 
 * 平滑移动机制：
 * - 服务端：每tick更新位置和速度，保存旧位置到 xo/yo/zo
 * - 客户端：Minecraft自动在旧位置和新位置之间插值渲染
 * - 无需额外代码，基类已处理所有细节
 */
public abstract class Projectile extends net.minecraft.world.entity.projectile.Projectile implements IEntityWithComplexSpawn {

    /** 投射物的最大存活时间（tick），默认40tick（2秒） */
    protected static final int EXPIRE_TIME = 40;
    
    /** 投射物造成的伤害值 */
    protected float damage = 0;
    
    /** 制导目标的UUID（用于网络同步） */
    @Nullable
    protected UUID homingTargetUUID;
    
    /** 制导目标的缓存引用（避免每tick查找） */
    @Nullable
    protected Entity cachedHomingTarget;
    
    /**
     * 旧的速度向量，用于客户端插值
     * 参考 Iron's Spells 的实现，防止第一帧闪烁
     */
    protected Vec3 deltaMovementOld = Vec3.ZERO;
    
    /**
     * 构造函数
     * 
     * @param entityType 实体类型
     * @param level 世界实例
     */
    public Projectile(EntityType<? extends net.minecraft.world.entity.projectile.Projectile> entityType, Level level) {
        super(entityType, level);
    }
    
    /**
     * 设置初始速度和方向
     * 
     * @param direction 方向向量（会自动归一化）
     * @param speed 速度大小
     * 
     * 注意：此方法会自动设置 hasImpulse = true，确保：
     * 1. Minecraft 正确同步实体位置
     * 2. 客户端能够进行平滑插值渲染
     * 3. 初始化 deltaMovementOld 以防止第一帧闪烁
     */
    public void shoot(Vec3 direction, double speed) {
        // 计算速度向量并设置
        Vec3 motion = direction.normalize().scale(speed);
        setDeltaMovement(motion);
        // 标记实体有冲量，确保Minecraft处理移动和同步
        // 这是客户端平滑插值的关键
        hasImpulse = true;
        
        // 关键：立即初始化 deltaMovementOld，防止第一帧插值错误
        // 参考 Iron's Spells 第96-98行的逻辑，但提前到创建时执行
        deltaMovementOld = motion;
    }
    
    /**
     * 实体的tick更新逻辑
     * 每游戏tick调用一次，处理移动、碰撞和生命周期
     * 
     * 平滑移动说明：
     * - super.tick() 会自动保存当前位置到 xo/yo/zo（旧位置）
     * - setPos() 更新新位置后，客户端会在两帧之间自动插值
     * - 公式：renderPos = oldPos + (newPos - oldPos) * partialTicks
     * - 无需任何额外代码，Minecraft 内置机制已处理
     * 
     * 重要：deltaMovementOld 已在 shoot() 中初始化，确保第一帧插值正确
     * 
     * 注意：制导逻辑已移除，子类如需制导请重写 handleHoming() 并在 tick() 中调用
     */
    @Override
    public void tick() {
        // 先调用父类tick处理基本逻辑（如重力、旋转等）
        super.tick();
        
        // 关键：防止第一帧闪烁（参考 Iron's Spells 第96-98行）
        if (tickCount == 1) {
            deltaMovementOld = getDeltaMovement();
        }
        
        // 超时检查
        if (tickCount > EXPIRE_TIME) {
            discard();
            return;
        }
        
        // 客户端只处理粒子效果
        if (level().isClientSide) {
            onClientTick();
            return;
        }
        
        // === 服务端逻辑 ===
        
        // 制导逻辑（可选）：子类可以重写 handleHoming() 并在此调用
        // handleHoming();  // ← 默认禁用，子类按需启用
        
        // 碰撞检测：使用 ProjectileUtil 检测前方是否有实体或方块
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        // 如果检测到碰撞且事件未被取消，处理命中
        if (hitresult.getType() != HitResult.Type.MISS && 
            !NeoForge.EVENT_BUS.post(new ProjectileImpactEvent(this, hitresult)).isCanceled()) {
            onHit(hitresult);
        }
        
        // 关键：手动更新位置（参考 Iron's Spells 的 travel() 方法）
        setPos(position().add(getDeltaMovement()));
        
        // 更新旧速度向量（参考 Iron's Spells 第111行）
        deltaMovementOld = getDeltaMovement();
        
        // 确保 hasImpulse 为 true，触发同步
        hasImpulse = true;
        
        // 调用子类的自定义tick逻辑
        onServerTick();
    }
    
    /**
     * 客户端tick回调
     * 用于处理粒子效果、音效等客户端专属逻辑
     * 子类可以重写此方法添加自定义行为
     */
    protected void onClientTick() {
        // 默认实现为空，子类按需重写
    }
    
    /**
     * 服务端tick回调
     * 用于处理自定义逻辑（如特殊效果、状态更新等）
     * 子类可以重写此方法添加自定义行为
     */
    protected void onServerTick() {
        // 默认实现为空，子类按需重写
    }
    
    /**
     * 处理制导逻辑（可选）
     * 如果设置了制导目标，会平滑地调整飞行方向朝向目标
     * 
     * 制导算法：
     * - 距离目标 < 0.5格：直接飞向目标中心
     * - 距离目标 >= 0.5格：使用线性插值平滑转向（turnStrength=0.15）
     * 
     * 使用方法：
     * 1. 子类在 tick() 中调用此方法以启用制导
     * 2. 通过 setHomingTarget() 设置制导目标
     * 
     * 示例：
     * <pre>{@code
     * @Override
     * public void tick() {
     *     super.tick();
     *     if (!level().isClientSide) {
     *         handleHoming();  // 启用制导
     *     }
     * }
     * }</pre>
     */
    protected void handleHoming() {
        // 如果没有设置制导目标，直接返回
        if (homingTargetUUID == null) {
            return;
        }
        
        // 获取制导目标实体
        var target = getHomingTarget();
        // 如果目标不存在或已被移除，清除制导状态
        if (target == null || target.isRemoved()) {
            homingTargetUUID = null;
            cachedHomingTarget = null;
            return;
        }
        
        // 计算到目标的方向向量
        Vec3 toTarget = target.position().subtract(position());
        double distance = toTarget.length();
        
        if (distance < 0.5) {
            // 非常接近目标，直接飞向中心（不再平滑转向）
            setDeltaMovement(toTarget.normalize().scale(getDeltaMovement().length()));
        } else {
            // 平滑转向目标
            Vec3 currentMotion = getDeltaMovement();
            double speed = currentMotion.length();  // 保持当前速度大小
            Vec3 wantedDirection = toTarget.normalize();  // 期望的方向
            
            // 简单的线性插值转向
            double turnStrength = 0.15;  // 转向强度：0.0-1.0，越大转向越快
            Vec3 newDirection = currentMotion.normalize()
                .add(wantedDirection.scale(turnStrength))  // 向目标方向偏移
                .normalize();  // 重新归一化
            
            // 应用新的速度向量（保持原速度大小）
            setDeltaMovement(newDirection.scale(speed));
        }
    }
    
    /**
     * 处理命中事件
     * 根据命中类型（实体/方块）分发到对应的处理方法
     * 
     * @param hitResult 命中结果
     */
    @Override
    protected void onHit(HitResult hitResult) {
        // 调用父类的命中处理
        super.onHit(hitResult);
        
        // 只在服务端处理命中逻辑
        if (!level().isClientSide) {
            switch (hitResult.getType()) {
                case ENTITY -> {
                    // 命中实体
                    if (hitResult instanceof EntityHitResult entityHitResult) {
                        onHitEntity(entityHitResult);
                    }
                }
                case BLOCK -> {
                    // 命中方块
                    if (hitResult instanceof BlockHitResult blockHitResult) {
                        onHitBlock(blockHitResult);
                    }
                }
                default -> {
                    // 其他情况（如MISS）不处理
                }
            }
        }
    }
    
    /**
     * 命中实体时的回调
     * 子类应该重写此方法来处理对实体的伤害或效果
     * 
     * @param result 实体命中结果，包含被命中的实体信息
     */
    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        Entity entity = result.getEntity();
        DebugLogger.debug("Projectile 击中实体: %s", entity.getType().getDescriptionId());
        
        // 注意：子类必须重写此方法来实际造成伤害
        // 示例：
        // if (entity instanceof LivingEntity livingEntity) {
        //     livingEntity.hurt(livingEntity.damageSources().magic(), damage);
        // }
    }
    
    /**
     * 命中方块时的回调
     * 子类应该重写此方法来处理对方块的交互（如破坏、放置等）
     * 
     * @param result 方块命中结果，包含被命中的方块位置和面信息
     */
    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        DebugLogger.debug("Projectile 击中方块");
        
        // 注意：子类必须重写此方法来处理方块交互
        // 示例：
        // BlockPos pos = result.getBlockPos();
        // level().destroyBlock(pos, false);
    }
    
    /**
     * 检查是否可以命中某个实体
     * 用于过滤掉不应该被命中的实体（如发射者自身、友方单位等）
     * 
     * @param target 待检查的目标实体
     * @return true 如果可以命中该实体
     */
    @Override
    protected boolean canHitEntity(@NotNull Entity target) {
        Entity owner = getOwner();  // 获取发射者
        // 不能命中：
        // 1. 父类判定不能命中的实体
        // 2. 发射者自身
        // 3. 发射者的友方单位（如果发射者存在）
        return super.canHitEntity(target) && 
               target != owner && 
               (owner == null || !owner.isAlliedTo(target));
    }
    
    /**
     * 检查是否应该销毁实体
     * 当实体进入未加载的区块时自动销毁，防止内存泄漏
     */
    @Override
    public void checkDespawn() {
        // 如果实体所在区块已加载，保留实体
        if (level().hasChunk(chunkPosition().x, chunkPosition().z)) {
            return;
        }
        // 否则销毁实体
        discard();
    }
    
    /**
     * 定义需要同步的实体数据
     * 用于客户端-服务端数据同步
     * 
     * @param builder 数据构建器
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        // 基础实现不需要额外的同步数据
        // 如果子类需要同步数据，在这里添加：
        // builder.define(DATA_ID, defaultValue);
    }
    
    /**
     * 将实体数据保存到NBT标签
     * 用于实体持久化和跨维度传输
     * 
     * @param tag NBT标签对象
     */
    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        // 保存伤害值
        tag.putFloat("Damage", damage);
        // 保存制导目标UUID（如果存在）
        if (homingTargetUUID != null) {
            tag.putUUID("HomingTarget", homingTargetUUID);
        }
        // 保存已存活的tick数
        tag.putInt("Age", tickCount);
    }
    
    /**
     * 从NBT标签读取实体数据
     * 用于恢复实体的状态
     * 
     * @param tag NBT标签对象
     */
    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // 读取伤害值
        damage = tag.getFloat("Damage");
        // 读取制导目标UUID（如果存在）
        if (tag.contains("HomingTarget")) {
            homingTargetUUID = tag.getUUID("HomingTarget");
        }
        // 读取已存活的tick数
        tickCount = tag.getInt("Age");
    }
    
    /**
     * 写入生成数据到网络缓冲区
     * 用于在实体首次生成时向客户端发送数据
     * 
     * @param buffer 网络缓冲区
     */
    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        // 写入发射者ID（0表示无发射者）
        var owner = getOwner();
        buffer.writeInt(owner == null ? 0 : owner.getId());
        
        // 写入制导目标ID（0表示无制导目标）
        var homingTarget = getHomingTarget();
        buffer.writeInt(homingTarget == null ? 0 : homingTarget.getId());
    }
    
    /**
     * 从网络缓冲区读取生成数据
     * 用于在客户端接收实体生成时的数据
     * 
     * @param additionalData 网络缓冲区
     */
    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        // 读取并发射者
        Entity owner = level().getEntity(additionalData.readInt());
        if (owner != null) {
            setOwner(owner);
        }
        
        // 读取并设置制导目标
        Entity homingTarget = level().getEntity(additionalData.readInt());
        if (homingTarget != null) {
            cachedHomingTarget = homingTarget;
            homingTargetUUID = homingTarget.getUUID();
        }
    }
    
    /**
     * 获取当前的制导目标实体
     * 优先使用缓存，如果缓存失效则从世界中查找
     * 
     * @return 制导目标实体，如果不存在则返回null
     */
    @Nullable
    public Entity getHomingTarget() {
        // 如果缓存有效且目标未被移除，直接返回缓存
        if (cachedHomingTarget != null && !cachedHomingTarget.isRemoved()) {
            return cachedHomingTarget;
        } 
        // 否则通过UUID从世界中查找（仅服务端）
        else if (homingTargetUUID != null && level() instanceof net.minecraft.server.level.ServerLevel) {
            cachedHomingTarget = ((net.minecraft.server.level.ServerLevel) level()).getEntity(homingTargetUUID);
            return cachedHomingTarget;
        }
        // 找不到目标，返回null
        return null;
    }
    
    /**
     * 设置制导目标
     * 设置后投射物会自动追踪该目标
     * 
     * @param entity 要追踪的目标实体
     */
    public void setHomingTarget(LivingEntity entity) {
        homingTargetUUID = entity.getUUID();  // 保存UUID用于网络同步
        cachedHomingTarget = entity;  // 缓存引用以提高性能
    }
    
    /**
     * 设置投射物造成的伤害值
     * 
     * @param damage 伤害值
     */
    public void setDamage(float damage) {
        this.damage = damage;
    }
    
    /**
     * 获取投射物造成的伤害值
     * 
     * @return 伤害值
     */
    public float getDamage() {
        return damage;
    }
}
