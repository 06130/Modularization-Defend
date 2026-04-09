package org.lingZero.m_defend.entity.projectile.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.entity.projectile.config.ProjectileParameters;
import org.lingZero.m_defend.entity.projectile.core.AbstractProjectileEntity;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 导弹子弹实体（新架构）
 * 特点：制导追踪、受较小重力、高伤害、中等寿命
 */
public class MissileProjectile extends AbstractProjectileEntity {
    
    private static final float MISSILE_DAMAGE = 20.0F;
    private static final int MISSILE_LIFETIME = 100;
    private static final double MISSILE_GRAVITY = 0.03;
    
    /**
     * 构造函数
     * 
     * @param entityType 实体类型
     * @param level 世界实例
     * @param params 参数配置
     */
    public MissileProjectile(EntityType<? extends MissileProjectile> entityType, 
                            Level level, 
                            ProjectileParameters params) {
        super(entityType, level, params);
    }
    
    /**
     * 便捷构造方法 - 创建带制导的导弹
     * 
     * @param entityType 实体类型
     * @param level 世界实例
     * @param startPos 起始位置
     * @param direction 初始方向
     * @param speed 速度
     * @param target 制导目标
     * @return 新创建的导弹
     */
    public static MissileProjectile createGuided(EntityType<? extends MissileProjectile> entityType,
                                                Level level,
                                                Vec3 startPos,
                                                Vec3 direction,
                                                double speed,
                                                Entity target) {
        ProjectileParameters params = ProjectileParameters.builder()
                .source(BlockPos.containing(startPos))
                .speed(speed)
                .withGravity(MISSILE_GRAVITY)
                .guidedTarget(target)
                .lifetime(MISSILE_LIFETIME)
                .build();
        
        MissileProjectile projectile = new MissileProjectile(entityType, level, params);
        projectile.setPos(startPos);
        
        // 设置初始速度并标记为有冲量
        Vec3 velocity = direction.normalize().multiply(speed, speed, speed);
        projectile.setDeltaMovement(velocity);
        projectile.hasImpulse = true;
        
        // 初始化运动控制器的方向
        projectile.getMotionController().initializeDirection(direction);
        
        DebugLogger.debug("创建制导导弹，目标=%s", target.getType().getDescriptionId());
        return projectile;
    }
    
    /**
     * 便捷构造方法 - 创建无制导的导弹
     * 
     * @param entityType 实体类型
     * @param level 世界实例
     * @param startPos 起始位置
     * @param direction 初始方向
     * @param speed 速度
     * @return 新创建的导弹
     */
    public static MissileProjectile createUnguided(EntityType<? extends MissileProjectile> entityType,
                                                  Level level,
                                                  Vec3 startPos,
                                                  Vec3 direction,
                                                  double speed) {
        ProjectileParameters params = ProjectileParameters.builder()
                .source(BlockPos.containing(startPos))
                .speed(speed)
                .withGravity(MISSILE_GRAVITY)
                .lifetime(MISSILE_LIFETIME)
                .build();
        
        MissileProjectile projectile = new MissileProjectile(entityType, level, params);
        projectile.setPos(startPos);
        
        // 设置初始速度并标记为有冲量
        Vec3 velocity = direction.normalize().multiply(speed, speed, speed);
        projectile.setDeltaMovement(velocity);
        projectile.hasImpulse = true;
        
        // 初始化运动控制器的方向
        projectile.getMotionController().initializeDirection(direction);
        
        return projectile;
    }
    
    @Override
    protected void onCustomTick() {
        // 添加尾迹粒子效果
        if (level().isClientSide) {
            // TODO: 生成导弹尾迹粒子
        }
    }
    
    @Override
    protected void onHitEntity(@NotNull Entity entity) {
        // 导弹造成高额伤害
        float damage = MISSILE_DAMAGE;
        Entity owner = getOwner();
        net.minecraft.world.entity.LivingEntity livingOwner = owner instanceof net.minecraft.world.entity.LivingEntity 
                ? (net.minecraft.world.entity.LivingEntity) owner : null;
        entity.hurt(entity.damageSources().mobProjectile(this, livingOwner), damage);
        
        DebugLogger.info("导弹击中实体: %s, 造成 %.1f 伤害", 
                entity.getType().getDescriptionId(), damage);
        
        // TODO: 爆炸效果
    }
    
    @Override
    protected void onHitBlock(@NotNull BlockHitResult hitResult) {
        DebugLogger.debug("导弹击中方块，准备爆炸");
        // TODO: 爆炸效果
    }
}
