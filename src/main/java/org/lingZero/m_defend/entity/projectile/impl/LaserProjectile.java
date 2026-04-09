package org.lingZero.m_defend.entity.projectile.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.entity.projectile.config.ProjectileParameters;
import org.lingZero.m_defend.entity.projectile.core.AbstractProjectileEntity;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 激光子弹实体（新架构）
 * 特点：直线飞行、不受重力影响、无空气阻力、寿命短、穿透所有方块
 */
public class LaserProjectile extends AbstractProjectileEntity {
    
    private static final float LASER_DAMAGE = 8.0F;
    private static final int LASER_LIFETIME = 40;
    
    /**
     * 构造函数
     * 
     * @param entityType 实体类型
     * @param level 世界实例
     * @param params 参数配置
     */
    public LaserProjectile(EntityType<? extends LaserProjectile> entityType, 
                          Level level, 
                          ProjectileParameters params) {
        super(entityType, level, params);
    }
    
    /**
     * 便捷构造方法 - 从位置和方向创建
     * 
     * @param entityType 实体类型
     * @param level 世界实例
     * @param startPos 起始位置
     * @param direction 方向向量（会自动归一化）
     * @param speed 速度
     * @return 新创建的激光弹
     */
    public static LaserProjectile create(EntityType<? extends LaserProjectile> entityType,
                                        Level level,
                                        Vec3 startPos,
                                        Vec3 direction,
                                        double speed) {
        ProjectileParameters params = ProjectileParameters.builder()
                .source(BlockPos.containing(startPos))
                .speed(speed)
                .lifetime(LASER_LIFETIME)
                .build();
        
        LaserProjectile projectile = new LaserProjectile(entityType, level, params);
        projectile.setPos(startPos);
        
        // 关键：设置初始速度并标记为有冲量
        Vec3 velocity = direction.normalize().multiply(speed, speed, speed);
        projectile.setDeltaMovement(velocity);
        projectile.hasImpulse = true;  // 重要：确保 Minecraft 处理移动
        
        // 初始化运动控制器的方向
        projectile.getMotionController().initializeDirection(direction);
        
        return projectile;
    }
    
    @Override
    protected void onCustomTick() {
        // 激光可以在这里添加粒子效果
        if (level().isClientSide) {
            // TODO: 生成激光粒子效果
        }
    }
    
    @Override
    protected void onHitEntity(@NotNull Entity entity) {
        // 对实体造成伤害
        float damage = LASER_DAMAGE;
        Entity owner = getOwner();
        LivingEntity livingOwner = owner instanceof LivingEntity
                ? (LivingEntity) owner : null;
        entity.hurt(entity.damageSources().mobProjectile(this, livingOwner), damage);
        
        if (DebugLogger.isDebugEnabled()) {
            DebugLogger.info("激光击中实体: %s, 造成 %.1f 伤害", 
                    entity.getType().getDescriptionId(), damage);
        }
    }
    
    @Override
    protected void onHitBlock(@NotNull BlockHitResult hitResult) {
        // 激光穿透所有方块（包括模组方块），不销毁
        if (DebugLogger.isDebugEnabled()) {
            DebugLogger.debug("激光穿透方块: %s", 
                    level().getBlockState(hitResult.getBlockPos()).getBlock().getDescriptionId());
        }
    }
    
    @Override
    protected boolean shouldDestroyOnHit() {
        // 激光只在击中实体时销毁，击中方块不销毁
        return getLastHitResult() != null && getLastHitResult().getHitEntity().isPresent();
    }
}
