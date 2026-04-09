package org.lingZero.m_defend.Items.TargetFilter;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.DataComponents.TargetFilterData;
import org.lingZero.m_defend.Register.ModDataComponents;

import java.util.List;

/**
 * 目标过滤器物品抽象基类
 * 提供获取数据组件和显示tooltip的通用方法
 */
public abstract class BaseTargetFilter extends Item {

    public BaseTargetFilter(Properties properties) {
        super(properties);
    }

    /**
     * 获取物品堆栈的过滤器数据组件
     * @param stack 物品堆栈
     * @return 过滤器数据，如果不存在则返回默认值
     */
    public static @NotNull TargetFilterData getFilterData(@NotNull ItemStack stack) {
        TargetFilterData data = stack.get(ModDataComponents.TARGET_FILTER_DATA.get());
        return data != null ? data : TargetFilterData.createDefault();
    }

    /**
     * 获取过滤器类型
     * 子类可以重写此方法以返回不同的过滤器类型
     * @return 过滤器类型
     */
    public TargetFilterData.FilterType getFilterType() {
        return TargetFilterData.FilterType.HOSTILE;
    }

    /**
     * 显示物品提示信息
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, TooltipFlag flag) {
        TargetFilterData data = getFilterData(stack);
        TargetFilterData.FilterType type = data.filterType();

        // 显示过滤器类型
        tooltip.add(Component.translatable("tooltip.modularization_defend.target_filter.type")
                .append(": ")
                .append(Component.translatable(getFilterTypeTranslationKey(type)))
                .withStyle(style -> style.withColor(0x00ff00))
        );

        // 如果是实体ID过滤器，显示实体ID
        if (type == TargetFilterData.FilterType.ENTITY_ID && data.entityId().isPresent()) {
            tooltip.add(Component.translatable("tooltip.modularization_defend.target_filter.entity_id")
                    .append(": ")
                    .append(Component.literal(data.entityId().get()))
                    .withStyle(style -> style.withColor(0xffff00))
            );
        }
    }


    /**
     * 获取过滤器类型的翻译键
     */
    protected String getFilterTypeTranslationKey(TargetFilterData.FilterType type) {
        return switch (type) {
            case HOSTILE -> "tooltip.modularization_defend.target_filter.type.hostile";
            case NEUTRAL -> "tooltip.modularization_defend.target_filter.type.neutral";
            case FRIENDLY -> "tooltip.modularization_defend.target_filter.type.friendly";
            case PLAYER -> "tooltip.modularization_defend.target_filter.type.player";
            case ENTITY_ID -> "tooltip.modularization_defend.target_filter.type.entity_id";
        };
    }
}
