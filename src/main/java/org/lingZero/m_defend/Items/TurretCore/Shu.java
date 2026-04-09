package org.lingZero.m_defend.Items.TurretCore;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.DataComponents.TurretCoreData;
import org.lingZero.m_defend.Items.TurretCore.frame.CampType;
import org.lingZero.m_defend.Items.TurretCore.frame.TurretCore;

public class Shu extends TurretCore {

    @Override
    public CampType getCampType() {
        return CampType.YAN_SUI;
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
        return false;
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

    public Shu(Properties properties) {
        super(properties);
    }

}
