package org.lingZero.modularization_defend.Register;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * 模组按键绑定注册表
 * 负责注册和管理所有客户端按键绑定
 */
public class ModKeyBindings {
    
    /**
     * 打开 GUI 的按键绑定
     */
    public static KeyMapping openTurretGuiKey;
    
    /**
     * 注册到模组事件总线
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModKeyBindings::registerKeyMappings);
    }
    
    /**
     * 注册所有按键绑定
     * @param event 按键映射注册事件
     */
    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // 创建按键绑定，默认使用 G 键
        openTurretGuiKey = new KeyMapping(
            "key.modularization_defend.open_defend_core_gui",  // 翻译键
            InputConstants.KEY_G,  // 默认按键（G 键）
            "key.categories.misc"  // 按键分类（杂项）
        );
        event.register(openTurretGuiKey);
    }
}
