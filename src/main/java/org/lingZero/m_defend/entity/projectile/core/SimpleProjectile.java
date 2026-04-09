package org.lingZero.m_defend.entity.projectile.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
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
 */
public abstract class SimpleProjectile extends Projectile implements IEntityWithComplexSpawn {
    
    protected static final int EXPIRE_TIME = 40; // 2秒
    
    protected float damage = 0;
    @Nullable
    protected UUID homingTargetUUID;
    @Nullable
    protected Entity cachedHomingTarget;
    
    public SimpleProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }
    
    /**
     * 设置初始速度
     */
    public void shoot(Vec3 direction, double speed) {
        setDeltaMovement(direction.normalize().scale(speed));
        hasImpulse = true;
    }
    
    @Override
    public void tick() {
        // 先调用父类tick
        super.tick();
        
        // 客户端只处理粒子效果
        if (level().isClientSide) {
            onClientTick();
            return;
        }
        
        // 服务端处理逻辑
        // 超时检查
        if (tickCount > EXPIRE_TIME) {
            discard();
            return;
        }
        
        // 制导逻辑（如果有目标）
        handleHoming();
        
        // 碰撞检测 - 使用 Iron's Spells 的方式
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && 
            !NeoForge.EVENT_BUS.post(new ProjectileImpactEvent(this, hitresult)).isCanceled()) {
            onHit(hitresult);
        }
        
        // 关键：手动更新位置（参考 Iron's Spells 的 travel() 方法）
        setPos(position().add(getDeltaMovement()));
        
        // 自定义tick逻辑
        onServerTick();
    }
    
    /**
     * 客户端tick（用于粒子效果等）
     */
    protected void onClientTick() {
        // 子类可以重写
    }
    
    /**
     * 服务端tick（自定义逻辑）
     */
    protected void onServerTick() {
        // 子类可以重写
    }
    
    /**
     * 处理制导
     */
    protected void handleHoming() {
        if (homingTargetUUID == null) {
            return;
        }
        
        var target = getHomingTarget();
        if (target == null || target.isRemoved()) {
            homingTargetUUID = null;
            cachedHomingTarget = null;
            return;
        }
        
        // 简单的制导逻辑：朝目标方向调整
        Vec3 toTarget = target.position().subtract(position());
        double distance = toTarget.length();
        
        if (distance < 0.5) {
            // 非常接近目标，直接飞向中心
            setDeltaMovement(toTarget.normalize().scale(getDeltaMovement().length()));
        } else {
            // 平滑转向目标
            Vec3 currentMotion = getDeltaMovement();
            double speed = currentMotion.length();
            Vec3 wantedDirection = toTarget.normalize();
            
            // 简单的线性插值转向
            double turnStrength = 0.15; // 转向强度
            Vec3 newDirection = currentMotion.normalize()
                .add(wantedDirection.scale(turnStrength))
                .normalize();
            
            setDeltaMovement(newDirection.scale(speed));
        }
    }
    
    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        
        if (!level().isClientSide) {
            switch (hitResult.getType()) {
                case ENTITY -> {
                    if (hitResult instanceof EntityHitResult entityHitResult) {
                        onHitEntity(entityHitResult);
                    }
                }
                case BLOCK -> {
                    if (hitResult instanceof BlockHitResult blockHitResult) {
                        onHitBlock(blockHitResult);
                    }
                }
                default -> {}
            }
        }
    }
    
    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        Entity entity = result.getEntity();
        DebugLogger.debug("SimpleProjectile 击中实体: %s", entity.getType().getDescriptionId());
        
        // 子类应该重写此方法处理伤害
    }
    
    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        DebugLogger.debug("SimpleProjectile 击中方块");
        
        // 子类应该重写此方法
    }
    
    @Override
    protected boolean canHitEntity(@NotNull Entity target) {
        Entity owner = getOwner();
        return super.canHitEntity(target) && 
               target != owner && 
               (owner == null || !owner.isAlliedTo(target));
    }
    
    @Override
    public void checkDespawn() {
        // 不在已加载区块时销毁
        if (level().hasChunk(chunkPosition().x, chunkPosition().z)) {
            return;
        }
        discard();
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        // 基础实现不需要额外数据
    }
    
    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", damage);
        if (homingTargetUUID != null) {
            tag.putUUID("HomingTarget", homingTargetUUID);
        }
        tag.putInt("Age", tickCount);
    }
    
    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("Damage");
        if (tag.contains("HomingTarget")) {
            homingTargetUUID = tag.getUUID("HomingTarget");
        }
        tickCount = tag.getInt("Age");
    }
    
    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        var owner = getOwner();
        buffer.writeInt(owner == null ? 0 : owner.getId());
        var homingTarget = getHomingTarget();
        buffer.writeInt(homingTarget == null ? 0 : homingTarget.getId());
    }
    
    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        Entity owner = level().getEntity(additionalData.readInt());
        if (owner != null) {
            setOwner(owner);
        }
        Entity homingTarget = level().getEntity(additionalData.readInt());
        if (homingTarget != null) {
            cachedHomingTarget = homingTarget;
            homingTargetUUID = homingTarget.getUUID();
        }
    }
    
    @Nullable
    public Entity getHomingTarget() {
        if (cachedHomingTarget != null && !cachedHomingTarget.isRemoved()) {
            return cachedHomingTarget;
        } else if (homingTargetUUID != null && level() instanceof net.minecraft.server.level.ServerLevel) {
            cachedHomingTarget = ((net.minecraft.server.level.ServerLevel) level()).getEntity(homingTargetUUID);
            return cachedHomingTarget;
        }
        return null;
    }
    
    public void setHomingTarget(LivingEntity entity) {
        homingTargetUUID = entity.getUUID();
        cachedHomingTarget = entity;
    }
    
    public void setDamage(float damage) {
        this.damage = damage;
    }
    
    public float getDamage() {
        return damage;
    }
}
