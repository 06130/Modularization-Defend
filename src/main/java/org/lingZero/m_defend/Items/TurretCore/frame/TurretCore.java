package org.lingZero.m_defend.Items.TurretCore.frame;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.DataComponents.TurretCoreData;
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
     * tooltip
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, TooltipFlag flag){

        TurretCoreData data = getData(stack);

        int firing_rate_level = data.firingRateLevel();
        int harm_level = data.harmLevel();
        int energy_level = data.energyLevel();
        TurretType type = data.turretType();
        CampType campType = getCampType();

        tooltip.add(Component.translatable("tooltip.modularization_defend.turret_core.firing_rate_level", firing_rate_level)
                .withStyle(style -> style.withColor(0xffdb00))
        );
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
