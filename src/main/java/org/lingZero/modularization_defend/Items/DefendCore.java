package org.lingZero.modularization_defend.Items;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.DataComponents.DefendCoreData;
import org.lingZero.modularization_defend.Register.ModDataComponents;
import org.lingZero.modularization_defend.Register.ModKeyBindings;
import org.lingZero.modularization_defend.util.EnergydataConversion;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

//TODO:
// 1.GUI 相关；
// 2.模块升级系统；
// 3.能量缓存；
// 4.护盾模块；
// 5 随身炮台及其升级系统；
// ！6.数据组件 (高优先) #进行中(使用数据组件记录)
// 7.饰品槽添加 (高优先) #部分完成
// 8.渲染相关 (低优先)
public class DefendCore extends Item implements ICurioItem {
    public DefendCore(Properties properties) {
        super(properties);
    }
    
    /**
     * 获取物品的数据组件
     */
    public static @NotNull DefendCoreData getData(@NotNull ItemStack stack) {
        DefendCoreData data = stack.get(ModDataComponents.CORE_MODULE_DATA.get());
        return data != null ? data : DefendCoreData.createDefault();
    }
    
    /**
     * tooltip
     * @param stack 物品栈
     * @param context 上下文
     * @param tooltip 提示列表
     * @param flag 提示标志
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        // 获取按键绑定的显示名称
        KeyMapping openKey = ModKeyBindings.openTurretGuiKey;
        String keyName = openKey != null ? openKey.getTranslatedKeyMessage().getString() : "G";
        
        // 获取能量数据
        DefendCoreData data = getData(stack);
        long energy_current = data.energyCurrent();
        long energy_max = data.energyMax();
        
        // 使用翻译键并替换 %d 为按键名称，使用白色 (#FFFFFF)
        tooltip.add(Component.translatable("tooltip.modularization_defend.defend_core.desc", keyName)
                .withStyle(style -> style.withColor(0xFFFFFF))
        );
        
        // 使用单位转换格式化能量显示
        String currentEnergyFormatted = EnergydataConversion.convertToHumanReadable(energy_current);
        String maxEnergyFormatted = EnergydataConversion.convertToHumanReadable(energy_max);
        
        tooltip.add(Component.translatable("tooltip.modularization_defend.defend_core.energy", 
                currentEnergyFormatted, maxEnergyFormatted));
    }

    /**
     * 插槽物品安装后 Tick
     * @param slotContext 插槽上下文
     * @param stack 插槽物品栈
     */
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // do something
    }
}