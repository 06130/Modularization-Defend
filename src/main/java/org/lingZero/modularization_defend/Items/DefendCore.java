package org.lingZero.modularization_defend.Items;

import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.Register.ModDataComponents;
import org.lingZero.modularization_defend.Register.ModKeyBindings;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

//TODO:
// 1.GUI 相关；
// 2.模块升级系统；
// 3.能量缓存；
// 4.护盾模块；
// 5 随身炮台及其升级系统；
// ！6.数据组件 (高优先) #进行中
// 7.饰品槽添加 (高优先) #部分完成
// 8.渲染相关 (低优先)
public class DefendCore extends Item implements ICurioItem {
    public DefendCore(Properties properties) {
        super(properties);
    }
    
    /**
     * 数据组件
     */
    private static CompoundTag createDefaultData() {
        CompoundTag data = new CompoundTag();
        
        data.putInt("firing_rate_level", 1);  // 默认射速倍率
        data.putInt("harm_level", 1);  // 默认伤害倍率
        data.putInt("energy_expend_level", 1);  // 默认能量消耗倍率
        
        data.putLong("energy_max", 10000);  // 默认能量存储上限
        data.putLong("energy_current", 0); // 默认当前能量
        
        data.putDouble("level", 1); // 护盾容量 (1=1 点伤害)
        data.putBoolean("shieldActive", false);  // 护盾系统默认关闭
        data.putString("turretCount", "null");  // 炮台核心类型
        
        return data;
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
        // 使用翻译键并替换 %d 为按键名称，使用白色 (#FFFFFF)
        tooltip.add(Component.translatable("tooltip.modularization_defend.defend_core.desc", keyName)
                .withStyle(style -> style.withColor(0xFFFFFF))
        );
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
        
    /**
     * 获取物品的能量存储能力
     * 
     * @param stack 物品栈
     * @return IEnergyStorage 实例，实现 FE 能量接口
     */
    @Nullable
    public static IEnergyStorage getEnergyStorage(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof DefendCore)) {
            return null;
        }
            
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                CompoundTag data = stack.getOrDefault(ModDataComponents.CORE_MODULE_DATA.get(), new CompoundTag());
                long currentEnergy = data.getLong("energy_current");
                long maxEnergy = data.getLong("energy_max");
                long canReceive = Math.min(maxReceive, maxEnergy - currentEnergy);
                    
                if (!simulate && canReceive > 0) {
                    data.putLong("energy_current", currentEnergy + canReceive);
                    stack.set(ModDataComponents.CORE_MODULE_DATA.get(), data);
                }
                    
                return (int) canReceive;
            }
                
            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                CompoundTag data = stack.getOrDefault(ModDataComponents.CORE_MODULE_DATA.get(), new CompoundTag());
                long currentEnergy = data.getLong("energy_current");
                long canExtract = Math.min(maxExtract, currentEnergy);
                    
                if (!simulate && canExtract > 0) {
                    data.putLong("energy_current", currentEnergy - canExtract);
                    stack.set(ModDataComponents.CORE_MODULE_DATA.get(), data);
                }
                    
                return (int) canExtract;
            }
                
            @Override
            public int getEnergyStored() {
                CompoundTag data = stack.getOrDefault(ModDataComponents.CORE_MODULE_DATA.get(), new CompoundTag());
                return (int) Math.min(data.getLong("energy_current"), Integer.MAX_VALUE);
            }
                
            @Override
            public int getMaxEnergyStored() {
                CompoundTag data = stack.getOrDefault(ModDataComponents.CORE_MODULE_DATA.get(), new CompoundTag());
                return (int) Math.min(data.getLong("energy_max"), Integer.MAX_VALUE);
            }
                
            @Override
            public boolean canExtract() {
                return true;
            }
                
            @Override
            public boolean canReceive() {
                return true;
            }
        };
    }
}
