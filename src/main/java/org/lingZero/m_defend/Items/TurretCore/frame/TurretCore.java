package org.lingZero.m_defend.Items.TurretCore.frame;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.DataComponents.TurretCoreData;
import org.lingZero.m_defend.DataComponents.TurretType;
import org.lingZero.m_defend.Register.ModDataComponents;

import java.util.List;

/**
 * 炮塔核心物品抽象基类
 * 提供获取数据组件的通用方法
 */
public abstract class TurretCore extends Item {

    public TurretCore(Properties properties) {
        super(properties);
    }

    /**
     * 获取物品堆栈的数据组件
     * @param stack 物品堆栈
     * @return 炮塔核心数据，如果不存在则返回默认值
     */
    public static @NotNull TurretCoreData getData(@NotNull ItemStack stack) {
        TurretCoreData data = stack.get(ModDataComponents.TURRET_CORE_DATA.get());
        return data != null ? data : TurretCoreData.createDefault();
    }

    /**
     * 获取炮塔核心的阵营类型
     * 子类可以重写此方法以返回不同的阵营类型
     * @return 阵营类型
     */
    public CampType getCampType() {
        return CampType.NONE;
    }
    
    /**
     * 执行攻击逻辑
     * 根据炮塔类型调用对应的攻击方式
     * 
     * @param level 世界实例
     * @param data 炮塔核心数据
     * @param sourcePos 炮塔位置
     * @param target 目标实体（可为 null）
     * @param targetPos 目标位置（如果 target 为 null 时使用）
     * @return true 如果攻击成功执行
     */
    public boolean executeAttack(
            @NotNull Level level,
            @NotNull TurretCoreData data,
            @NotNull Vec3 sourcePos,
            @Nullable LivingEntity target,
            @NotNull Vec3 targetPos) {
        
        TurretType type = data.turretType();
        
        // 根据炮塔类型调用对应的攻击方式
        return switch (type) {
            case LASER -> attackWithLaser(level, data, sourcePos, target, targetPos);
            case MISSILE -> attackWithMissile(level, data, sourcePos, target, targetPos);
            case HOWITZER -> attackWithHowitzer(level, data, sourcePos, target, targetPos);
            case RANGE -> attackWithRange(level, data, sourcePos, target, targetPos);
            case NONE -> false;  // 无类型，无法攻击
        };
    }
    
    /**
     * 激光攻击方式
     * 直线射击，高伤害，无重力影响
     * 子类必须实现此方法
     * 
     * @param level 世界实例
     * @param data 炮塔核心数据
     * @param sourcePos 炮塔位置
     * @param target 目标实体
     * @param targetPos 目标位置
     * @return true 如果攻击成功
     */
    protected abstract boolean attackWithLaser(
            @NotNull Level level,
            @NotNull TurretCoreData data,
            @NotNull Vec3 sourcePos,
            @Nullable LivingEntity target,
            @NotNull Vec3 targetPos);
    
    /**
     * 导弹攻击方式
     * 自动制导，范围伤害
     * 子类必须实现此方法
     * 
     * @param level 世界实例
     * @param data 炮塔核心数据
     * @param sourcePos 炮塔位置
     * @param target 目标实体
     * @param targetPos 目标位置
     * @return true 如果攻击成功
     */
    protected abstract boolean attackWithMissile(
            @NotNull Level level,
            @NotNull TurretCoreData data,
            @NotNull Vec3 sourcePos,
            @Nullable LivingEntity target,
            @NotNull Vec3 targetPos);
    
    /**
     * 榴弹炮攻击方式
     * 抛物线轨迹，范围爆炸伤害
     * 子类必须实现此方法
     * 
     * @param level 世界实例
     * @param data 炮塔核心数据
     * @param sourcePos 炮塔位置
     * @param target 目标实体
     * @param targetPos 目标位置
     * @return true 如果攻击成功
     */
    protected abstract boolean attackWithHowitzer(
            @NotNull Level level,
            @NotNull TurretCoreData data,
            @NotNull Vec3 sourcePos,
            @Nullable LivingEntity target,
            @NotNull Vec3 targetPos);
    
    /**
     * 范围法术攻击方式
     * 区域效果，持续伤害或控制
     * 子类必须实现此方法
     * 
     * @param level 世界实例
     * @param data 炮塔核心数据
     * @param sourcePos 炮塔位置
     * @param target 目标实体
     * @param targetPos 目标位置
     * @return true 如果攻击成功
     */
    protected abstract boolean attackWithRange(
            @NotNull Level level,
            @NotNull TurretCoreData data,
            @NotNull Vec3 sourcePos,
            @Nullable LivingEntity target,
            @NotNull Vec3 targetPos);

    /**
     * tooltip
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, TooltipFlag flag){

        TurretCoreData data = getData(stack);

        int harm_level = data.harmLevel();
        int energy_level = data.energyLevel();
        TurretType type = data.turretType();
        CampType campType = getCampType();

        tooltip.add(Component.translatable("tooltip.modularization_defend.turret_core.harm_level", harm_level)
                .withStyle(style -> style.withColor(0x54fcfc))
        );
        tooltip.add(Component.translatable("tooltip.modularization_defend.turret_core.energy_level", energy_level)
                .withStyle(style -> style.withColor(0x3cfe9a))
        );
        tooltip.add(Component.translatable("tooltip.modularization_defend.turret_core.type")
                .append(": ")
                .append(Component.translatable(type.getSerializedName()))
                .withStyle(style -> style.withColor(0x0090ff))
        );
        tooltip.add(Component.translatable("tooltip.modularization_defend.turret_core.camp_type")
                .append(": ")
                .append(Component.translatable(campType.getSerializedName()))
                .withStyle(style -> style.withColor(0xff0051))
        );
    }
}
