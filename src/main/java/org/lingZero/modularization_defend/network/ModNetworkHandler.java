package org.lingZero.modularization_defend.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.lingZero.modularization_defend.ModularizationDefend;
import org.lingZero.modularization_defend.util.DebugLogger;

/**
 * 网络消息注册器
 */
@EventBusSubscriber(modid = ModularizationDefend.MODID)
public class ModNetworkHandler {
    
    /**
     * 注册所有网络消息
     */
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        
        // 注册打开 DefendCore GUI 的消息（客户端 -> 服务端）
        registrar.playToServer(
            OpenDefendCoreGUIMessage.TYPE,
            OpenDefendCoreGUIMessage.STREAM_CODEC,
            OpenDefendCoreGUIMessage::handle
        );
        
        DebugLogger.info("网络消息注册完成");
    }
}
