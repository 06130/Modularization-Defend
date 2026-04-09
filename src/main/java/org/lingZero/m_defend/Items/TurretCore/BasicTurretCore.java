package org.lingZero.m_defend.Items.TurretCore;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.DataComponents.TurretCoreData;
import org.lingZero.m_defend.Items.TurretCore.frame.CampType;
import org.lingZero.m_defend.Items.TurretCore.frame.TurretCore;
import org.lingZero.m_defend.Register.ModEntities;
import org.lingZero.m_defend.entity.LaserProjectile;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 基础炮塔核心物品实现类
 */
public class BasicTurretCore extends TurretCore {


    /**
     * 获取该炮塔核心的阵营类型
     * @return 阵营类型
     */
    @Override
    public CampType getCampType() {
        return CampType.NONE;
    }

    /**
     * 激光攻击方式
     * 直线射击，高伤害，无重力影响
     * 子类必须实现此方法
     *
     * @param level     世界实例
     * @param data      炮塔核心数据
     * @param sourcePos 炮塔位置
     * @param target    目标实体
     * @param targetPos 目标位置
     * @return true 如果攻击成功
     */
    @Override
    protected boolean attackWithLaser(@NotNull Level level, @NotNull TurretCoreData data, @NotNull Vec3 sourcePos, @Nullable LivingEntity target, @NotNull Vec3 targetPos) {
        // 只在服务端执行
        if (level.isClientSide()) {
            return false;
        }
        
        // 计算方向向量
        Vec3 direction = targetPos.subtract(sourcePos).normalize();
        
        // 设置速度（快速直线飞行）
        double speed = 0.5;
        
        // 计算基础伤害（8点）并根据伤害等级加成
        float baseDamage = 8.0F;
        float damageMultiplier = 1.0F + (data.harmLevel() * 0.15F);  // 每级增加15%伤害
        float finalDamage = baseDamage * damageMultiplier;
        
        // 使用简化版本创建激光弹
        LaserProjectile projectile = LaserProjectile.create(
            ModEntities.SIMPLE_LASER_PROJECTILE.get(),
            level,
            sourcePos,
            direction,
            speed
        );
        
        // 设置伤害
        projectile.setDamage(finalDamage);
        
        // 如果有目标实体，设置制导目标
        if (target != null) {
            projectile.setHomingTarget(target);
        }
        
        // 生成实体到世界中
        level.addFreshEntity(projectile);
        
        DebugLogger.debug("发射激光子弹: 起点=%s, 目标=%s, 速度=%.2f, 伤害=%.1f (等级%d, 倍率%.0f%%)", 
            sourcePos.toString(), 
            targetPos.toString(),
            speed,
            finalDamage,
            data.harmLevel(),
            damageMultiplier * 100);
        
        return true;
    }

    /**
     * 导弹攻击方式
     * 自动制导，范围伤害
     * 子类必须实现此方法
     *
     * @param level     世界实例
     * @param data      炮塔核心数据
     * @param sourcePos 炮塔位置
     * @param target    目标实体
     * @param targetPos 目标位置
     * @return true 如果攻击成功
     */
    @Override
    protected boolean attackWithMissile(@NotNull Level level, @NotNull TurretCoreData data, @NotNull Vec3 sourcePos, @Nullable LivingEntity target, @NotNull Vec3 targetPos) {
        return false;
    }

    /**
     * 榴弹炮攻击方式
     * 抛物线轨迹，范围爆炸伤害
     * 子类必须实现此方法
     *
     * @param level     世界实例
     * @param data      炮塔核心数据
     * @param sourcePos 炮塔位置
     * @param target    目标实体
     * @param targetPos 目标位置
     * @return true 如果攻击成功
     */
    @Override
    protected boolean attackWithHowitzer(@NotNull Level level, @NotNull TurretCoreData data, @NotNull Vec3 sourcePos, @Nullable LivingEntity target, @NotNull Vec3 targetPos) {
        return false;
    }

    /**
     * 范围法术攻击方式
     * 区域效果，持续伤害或控制
     * 子类必须实现此方法
     *
     * @param level     世界实例
     * @param data      炮塔核心数据
     * @param sourcePos 炮塔位置
     * @param target    目标实体
     * @param targetPos 目标位置
     * @return true 如果攻击成功
     */
    @Override
    protected boolean attackWithRange(@NotNull Level level, @NotNull TurretCoreData data, @NotNull Vec3 sourcePos, @Nullable LivingEntity target, @NotNull Vec3 targetPos) {
        return false;
    }

    public BasicTurretCore(Properties properties) {
        super(properties);
    }
}
