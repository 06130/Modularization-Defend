package org.lingZero.modularization_defend.Event;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.Register.ModDataComponents;
import org.lingZero.modularization_defend.Register.ModItems;

/**
 * 默认数据组件事件处理器
 * 用于在物品注册后设置默认数据组件
 */
@EventBusSubscriber(modid = ModularizationDefend.MODID)
public class ModDefaultComponentsEvent {

    @SubscribeEvent
    public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        // 为防御核心设置默认数据组件
        event.modify(ModItems.DEFEND_CORE.get(), builder -> {
            builder.set(ModDataComponents.CORE_MODULE_DATA.get(), createDefaultCoreData());
        });
    }

    /**
     * 创建防御核心的默认数据
     */
    private static CompoundTag createDefaultCoreData() {
        CompoundTag data = new CompoundTag();
        
        data.putInt("firing_rate_level", 1);  // 默认射速倍率
        data.putInt("harm_level", 1);  // 默认伤害倍率
        data.putInt("energy_expend_level", 1);  // 默认能量消耗倍率
        
        data.putLong("energy_max", 1000);  // 默认能量存储上限
        data.putLong("energy_current", 1000); // 默认当前能量
        
        data.putDouble("level", 1); // 护盾容量 (1=1 点伤害)
        data.putBoolean("shieldActive", false);  // 护盾系统默认关闭
        data.putString("turretCount", "null");  // 炮台核心类型
        
        return data;
    }
}