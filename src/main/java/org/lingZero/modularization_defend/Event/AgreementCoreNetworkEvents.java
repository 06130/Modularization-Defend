package org.lingZero.modularization_defend.Event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.lingZero.modularization_defend.AgreementCoreNetwork.NetworkMain;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.slf4j.Logger;

/**
 * 路由网络事件处理器
 * 负责在世界加载时初始化路由系统，并处理 Tick 事件
 */
@EventBusSubscriber(modid = ModularizationDefend.MODID)
public class AgreementCoreNetworkEvents {
    
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AgreementCoreNetworkEvents.class);
    
    /**
     * 标记路由系统是否已初始化
     */
    private static boolean initialized = false;
    
    /**
     * 监听世界加载事件，初始化路由系统
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        // 只在服务器端初始化
        if (event.getLevel().isClientSide()) {
            return;
        }
        
        // 防止重复初始化
        if (!initialized) {
            LOGGER.info("Initializing AgreementCore Network Routing System...");
            
            try {
                // 初始化路由系统
                NetworkMain.initialize();
                initialized = true;
                
                LOGGER.info("AgreementCore Network Routing System initialized successfully!");
                LOGGER.info("Local Address: {}", NetworkMain.getLocalAddress());
            } catch (Exception e) {
                LOGGER.error("Failed to initialize AgreementCore Network Routing System!", e);
            }
        }
    }
    
    /**
     * 监听世界卸载事件，清理路由系统
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        // 只在服务器端清理
        if (event.getLevel().isClientSide()) {
            return;
        }
        
        if (initialized) {
            LOGGER.info("Unloading AgreementCore Network Routing System...");
            initialized = false;
        }
    }
    
    /**
     * 监听服务器刻事件，处理路由队列
     */
    @SubscribeEvent
    public static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        // 只在初始化后处理 Tick
        if (initialized) {
            NetworkMain.tick();
        }
    }
}
