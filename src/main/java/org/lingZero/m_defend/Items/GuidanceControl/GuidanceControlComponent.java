package org.lingZero.m_defend.Items.GuidanceControl;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 制导控制组件物品
 * 用于炮塔的制导控制系统
 */
public class GuidanceControlComponent extends Item {

    public GuidanceControlComponent(Properties properties) {
        super(properties);
    }

    /**
     * 显示物品提示信息
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, TooltipFlag flag) {
        // 显示物品描述
        tooltip.add(Component.translatable("tooltip.modularization_defend.guidance_control.description")
                .withStyle(style -> style.withColor(0xAAAAAA)));
    }
}
