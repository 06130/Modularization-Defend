package org.lingZero.m_defend;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.lingZero.m_defend.config.DebugConfig;
import org.lingZero.m_defend.config.GeneralConfig;
import org.lingZero.m_defend.config.TurretConfig;
import org.lingZero.m_defend.util.DebugLogger;

import static org.lingZero.m_defend.ModularizationDefend.MODVERSION;

/**
 * 模组配置管理类
 * 采用模块化设计，每个配置类别独立管理
 */
@SuppressWarnings("removal")
@EventBusSubscriber(modid = ModularizationDefend.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    
    // ==================== 配置实例（公开静态字段）====================
    public static final GeneralConfig GENERAL = new GeneralConfig();
    public static final TurretConfig TURRET = new TurretConfig();
    public static final DebugConfig DEBUG = new DebugConfig();
    
    // ==================== 便捷访问方法 ====================
    
    /**
     * 获取最大连接距离
     */
    public static double getMaxConnectionDistance() {
        return GENERAL.maxConnectionDistance.get();
    }

    /**
     * 获取协议网络心跳间隔
     */
    public static int getHeartbeatInterval() {
        return GENERAL.heartbeatInterval.get();
    }
    
    /**
     * 获取炮塔核心升级上限
     */
    public static int getFiringRateLevelMax() {
        return GENERAL.firingRateLevelMax.get();
    }
    
    public static int getHarmLevelMax() {
        return GENERAL.harmLevelMax.get();
    }
    
    public static int getEnergyLevelMax() {
        return GENERAL.energyLevelMax.get();
    }
    
    /**
     * 获取调试模式状态
     */
    public static boolean isDebugEnabled() {
        return DEBUG.enabled.get();
    }
    
    /**
     * 获取调试日志级别
     */
    public static String getDebugLogLevel() {
        return DEBUG.logLevel.get();
    }
    
    // ==================== 配置加载事件 ====================
    
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // 只在配置重载时更新调试日志系统（首次加载时 DebugLogger 已在 ModularizationDefend 构造函数中初始化）
        if (event instanceof ModConfigEvent.Reloading) {
            updateDebugLogger();
            DebugLogger.info("配置已重载");
        } else if (event instanceof ModConfigEvent.Loading) {
            // 首次加载时，延迟更新调试日志（等待配置完全加载）
            DebugLogger.debug("配置首次加载，将在下一 tick 更新调试日志");
        }
    }
    
    private static void updateDebugLogger() {
        try {
            if (!DEBUG.enabled.get()) {
                DebugLogger.shutdown();
            } else {
                DebugLogger.setLogLevel(DEBUG.logLevel.get());
                DebugLogger.info("调试日志已启用，级别: {}", DEBUG.logLevel.get());
            }
        } catch (IllegalStateException e) {
            // 配置尚未加载完成，忽略
            DebugLogger.debug("配置尚未完全加载，跳过调试日志更新");
        }
    }
    
    // ==================== 玩家登录事件 ====================
    
    @SubscribeEvent
    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (event != null) {
            var player = event.getEntity();
            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.literal("欢迎使用模块化防御，当前版本：" + MODVERSION));
            }
        }
    }
}
