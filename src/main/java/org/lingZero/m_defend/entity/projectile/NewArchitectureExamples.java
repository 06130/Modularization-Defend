package org.lingZero.m_defend.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.lingZero.m_defend.entity.projectile.config.ProjectileParameters;
import org.lingZero.m_defend.entity.projectile.core.AbstractProjectileEntity;
import org.lingZero.m_defend.entity.projectile.core.ProjectileHitResult;
import org.lingZero.m_defend.entity.projectile.impl.HowitzerProjectile;
import org.lingZero.m_defend.entity.projectile.impl.LaserProjectile;
import org.lingZero.m_defend.entity.projectile.impl.MissileProjectile;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 新架构子弹系统使用示例
 * 
 * 展示了如何：
 * 1. 创建不同类型的子弹
 * 2. 配置参数
 * 3. 获取命中结果
 */
public class NewArchitectureExamples {
    
    /**
     * 示例1：创建简单的激光弹
     */
    public static void example1_Laser(EntityType<? extends LaserProjectile> entityType, 
                                      Level level,
                                      Vec3 startPos,
                                      Vec3 direction) {
        // 使用便捷方法创建激光弹
        LaserProjectile laser = LaserProjectile.create(
            entityType,
            level,
            startPos,
            direction,
            3.0  // 速度
        );
        
        level.addFreshEntity(laser);
        DebugLogger.info("创建激光弹，位置=%s", startPos);
    }
    
    /**
     * 示例2：创建带制导的导弹
     */
    public static void example2_GuidedMissile(EntityType<? extends MissileProjectile> entityType,
                                             Level level,
                                             Vec3 startPos,
                                             Vec3 direction,
                                             Entity target) {
        // 创建追踪目标的导弹
        MissileProjectile missile = MissileProjectile.createGuided(
            entityType,
            level,
            startPos,
            direction,
            2.0,  // 速度
            target  // 制导目标
        );
        
        level.addFreshEntity(missile);
        DebugLogger.info("创建制导导弹，目标=%s", target.getType().getDescriptionId());
    }
    
    /**
     * 示例3：创建无制导的导弹（抛物线）
     */
    public static void example3_UnguidedMissile(EntityType<? extends MissileProjectile> entityType,
                                               Level level,
                                               Vec3 startPos,
                                               Vec3 direction) {
        MissileProjectile missile = MissileProjectile.createUnguided(
            entityType,
            level,
            startPos,
            direction,
            2.5
        );
        
        level.addFreshEntity(missile);
    }
    
    /**
     * 示例4：创建榴弹（高抛物线）
     */
    public static void example4_Howitzer(EntityType<? extends HowitzerProjectile> entityType,
                                        Level level,
                                        Vec3 startPos,
                                        Vec3 upwardDirection) {
        // 榴弹需要向上的初始方向以形成抛物线
        HowitzerProjectile howitzer = HowitzerProjectile.create(
            entityType,
            level,
            startPos,
            upwardDirection,
            1.5  // 较低的速度
        );
        
        level.addFreshEntity(howitzer);
        DebugLogger.info("创建榴弹");
    }
    
    /**
     * 示例5：手动构建参数并创建子弹
     */
    public static void example5_CustomParameters(EntityType<? extends AbstractProjectileEntity> entityType,
                                                Level level,
                                                BlockPos sourcePos,
                                                Vec3 startPos,
                                                Vec3 direction,
                                                Entity target) {
        // 使用构建器模式自定义所有参数
        ProjectileParameters params = ProjectileParameters.builder()
                .source(sourcePos)                      // 发射源位置
                .speed(2.0)                             // 速度
                .withGravity(0.05)                      // 启用重力，系数0.05
                .guidedTarget(target)                   // 设置制导目标
                .lifetime(150)                          // 寿命150 ticks
                .build();
        
        // 注意：这里需要根据具体类型实例化
        // AbstractProjectileEntity projectile = new YourCustomProjectile(entityType, level, params);
        // projectile.setPos(startPos);
        // projectile.getMotionController().initializeDirection(direction);
        // level.addFreshEntity(projectile);
        
        DebugLogger.info("使用自定义参数创建子弹: %s", params.toString());
    }
    
    /**
     * 示例6：查询子弹的命中结果
     */
    public static void example6_QueryHitResult(AbstractProjectileEntity projectile) {
        ProjectileHitResult result = projectile.getLastHitResult();
        
        if (result == null) {
            DebugLogger.info("子弹尚未命中任何目标");
            return;
        }
        
        if (result.isHit()) {
            DebugLogger.info("子弹已命中！");
            
            // 获取命中位置
            result.getHitPosition().ifPresent(pos -> 
                DebugLogger.info("命中位置: %s", pos.toString())
            );
            
            // 检查是否命中实体
            result.getHitEntity().ifPresent(entity -> 
                DebugLogger.info("命中实体: %s", entity.getType().getDescriptionId())
            );
            
            // 检查是否命中方块
            result.getHitBlock().ifPresent(blockState -> 
                DebugLogger.info("命中方块: %s", blockState.getBlock().getDescriptionId())
            );
            
            DebugLogger.info("飞行时间: %d ticks", result.getFlightTime());
        } else {
            DebugLogger.info("子弹未命中，飞行时间: %d ticks", result.getFlightTime());
        }
    }
    
    /**
     * 示例7：动态修改制导目标
     */
    public static void example7_ChangeGuidanceTarget(AbstractProjectileEntity projectile, 
                                                     Entity newTarget) {
        // 可以在飞行过程中更改制导目标
        projectile.getGuidanceSystem().setTarget(newTarget);
        DebugLogger.info("已更新制导目标为: %s", newTarget.getType().getDescriptionId());
    }
    
    /**
     * 示例8：禁用制导
     */
    public static void example8_DisableGuidance(AbstractProjectileEntity projectile) {
        projectile.getGuidanceSystem().disableGuidance();
        DebugLogger.info("已禁用制导，子弹将沿当前方向直线飞行");
    }
    
    /**
     * 示例9：检查子弹状态
     */
    public static void example9_CheckStatus(AbstractProjectileEntity projectile) {
        // 检查是否超时
        boolean expired = projectile.getMotionController().isExpired();
        DebugLogger.info("是否超时: %b", expired);
        
        // 获取剩余寿命
        int remaining = projectile.getMotionController().getRemainingLifetime();
        DebugLogger.info("剩余寿命: %d ticks", remaining);
        
        // 检查是否启用制导
        boolean hasGuidance = projectile.getGuidanceSystem().isGuidanceEnabled();
        DebugLogger.info("是否启用制导: %b", hasGuidance);
        
        // 获取当前制导目标
        Entity target = projectile.getGuidanceSystem().getTarget();
        if (target != null) {
            DebugLogger.info("制导目标: %s", target.getType().getDescriptionId());
        }
    }
}
