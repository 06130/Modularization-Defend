package org.lingZero.modularization_defend.Event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.lingZero.modularization_defend.DataComponents.DefendCoreData;
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
            builder.set(ModDataComponents.CORE_MODULE_DATA.get(), DefendCoreData.createDefault());
        });
    }
}
