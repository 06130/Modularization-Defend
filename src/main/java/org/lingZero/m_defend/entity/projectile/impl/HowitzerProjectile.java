package org.lingZero.m_defend.entity.projectile.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.entity.projectile.config.ProjectileParameters;
import org.lingZero.m_defend.entity.projectile.core.AbstractProjectileEntity;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 榴弹子弹实体（新架构）
 * 特点：抛物线轨迹、受较大重力、中等伤害、短寿命
 */
public class HowitzerProjectile extends AbstractProjectileEntity {
    
    private static final float HOWITZER_DAMAGE = 15.0F;
    private static final int HOWITZER_LIFETIME = 60;
    private static final double HOWITZER_GRAVITY = 0.08;
    
    /**
     * 构造函数
     * 
     * @param entityType 实体类型
     * @param level 世界实例
     * @param params 参数配置
     */
    public HowitzerProjectile(EntityType<? extends HowitzerProjectile> entityType, 
                             Level level, 
                             ProjectileParameters params) {
        super(entityType, level, params);
    }
    
    /**
     * 便捷构造方法 - 创建榴弹
     * 
     * @param entityType 实体类型
     * @param level 世界实例
     * @param startPos 起始位置
     * @param direction 初始方向（建议有向上的分量以形成抛物线）
     * @param speed 速度
     * @return 新创建的榴弹
     */
    public static HowitzerProjectile create(EntityType<? extends HowitzerProjectile> entityType,
                                           Level level,
                                           Vec3 startPos,
                                           Vec3 direction,
                                           double speed) {
        ProjectileParameters params = ProjectileParameters.builder()
                .source(BlockPos.containing(startPos))
                .speed(speed)
                .withGravity(HOWITZER_GRAVITY)
                .lifetime(HOWITZER_LIFETIME)
                .build();
        
        HowitzerProjectile projectile = new HowitzerProjectile(entityType, level, params);
        projectile.setPos(startPos);
        
        // 设置初始速度并标记为有冲量
        Vec3 velocity = direction.normalize().multiply(speed, speed, speed);
        projectile.setDeltaMovement(velocity);
        projectile.hasImpulse = true;
        
        // 初始化运动控制器的方向
        projectile.getMotionController().initializeDirection(direction);
        
        DebugLogger.debug("创建榴弹，初速度=%.2f, 方向=%s", speed, direction.toString());
        return projectile;
    }
    
    @Override
    protected void onCustomTick() {
        // 可以添加下坠粒子效果
        if (level().isClientSide) {
            // TODO: 生成榴弹下坠粒子
        }
    }
    
    @Override
    protected void onHitEntity(@NotNull Entity entity) {
        float damage = HOWITZER_DAMAGE;
        Entity owner = getOwner();
        net.minecraft.world.entity.LivingEntity livingOwner = owner instanceof net.minecraft.world.entity.LivingEntity 
                ? (net.minecraft.world.entity.LivingEntity) owner : null;
        entity.hurt(entity.damageSources().mobProjectile(this, livingOwner), damage);
        
        DebugLogger.info("榴弹击中实体: %s, 造成 %.1f 伤害", 
                entity.getType().getDescriptionId(), damage);
        
        // TODO: 爆炸效果
    }
    
    @Override
    protected void onHitBlock(@NotNull BlockHitResult hitResult) {
        DebugLogger.debug("榴弹击中方块，准备爆炸");
        // TODO: 爆炸效果
    }
}
