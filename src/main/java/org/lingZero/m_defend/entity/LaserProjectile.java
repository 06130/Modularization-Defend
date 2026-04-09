package org.lingZero.m_defend.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 激光弹 - 简化版本，完全参考 Iron's Spells 实现
 */
public class LaserProjectile extends Projectile {
    
    /** 激光弹默认伤害值 */
    private static final float LASER_DAMAGE = 8.0f;
    
    public LaserProjectile(EntityType<? extends LaserProjectile> entityType, Level level) {
        super(entityType, level);
    }
    
    /**
     * 便捷构造方法
     * 
     * @param entityType 实体类型
     * @param level 世界实例
     * @param startPos 起始位置
     * @param direction 方向向量（会自动归一化）
     * @param speed 速度大小（建议值：3.0）
     * @return 新创建的激光弹实体
     */
    public static LaserProjectile create(
            EntityType<? extends LaserProjectile> entityType,
            Level level,
            Vec3 startPos,
            Vec3 direction,
            double speed) {
        
        LaserProjectile projectile = new LaserProjectile(entityType, level);
        projectile.setPos(startPos);
        projectile.shoot(direction, speed);  // 使用传入的速度参数
        projectile.setDamage(LASER_DAMAGE);  // 使用默认伤害
        
        return projectile;
    }
    
    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        Entity entity = result.getEntity();
        
        if (entity instanceof LivingEntity livingEntity) {
            float damage = getDamage();
            livingEntity.hurt(livingEntity.damageSources().magic(), damage);
            
            DebugLogger.debug("激光击中实体: %s, 造成 %.1f 伤害", 
                entity.getType().getDescriptionId(), damage);
        }
        
        // 击中后销毁
        discard();
    }
    
    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        BlockPos pos = result.getBlockPos();
        DebugLogger.debug("激光击中方块: %s at %s", 
            level().getBlockState(pos).getBlock().getDescriptionId(),
            pos.toString());
        
        // 击中后销毁
        discard();
    }
}
