package org.lingZero.m_defend;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.lingZero.m_defend.util.DebugLogger;

import static org.lingZero.m_defend.ModularizationDefend.MODVERSION;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = ModularizationDefend.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 电网配置工具相关设置
    private static final ModConfigSpec.DoubleValue MAX_CONNECTION_DISTANCE = BUILDER
            .comment("电网配置工具的最大连接距离。注：不要调太大，我不保证会发生什么")
            .defineInRange("maxConnectionDistance", 64.0, 1.0, Double.MAX_VALUE);

    // GUI 配置
    private static final ModConfigSpec.ConfigValue<String> TEXT_COLOUR = BUILDER
            .comment("GUI 主要文本字体颜色（十六进制格式，如 0x00AA00）。注：暂时没啥用")
            .define("textColour", "0xFFEE00");
        
    private static final ModConfigSpec.IntValue DEFAULT_HEARTBEAT_INTERVAL = BUILDER
            .comment("协议网络默认心跳间隔。注：如果你不知道你在干什么，请不要修改。")
            .defineInRange("AgreementCoreNetworkHeartbeatInterval", 40,1,65536);
    
    //炮塔核心升级配置
    private static final ModConfigSpec.IntValue FIRING_RATE_LEVEL_MAX_CONFIG = BUILDER
            .comment("射速倍率上限")
            .defineInRange("firingRateLevelmax", 16,1,2147483647);
    private static final ModConfigSpec.IntValue HARM_LEVEL_MAX_CONFIG = BUILDER
            .comment("伤害倍率上限")
            .defineInRange("harmLevelmax", 16,1,2147483647);
    private static final ModConfigSpec.IntValue ENERGY_LEVEL_MAX_CONFIG = BUILDER
            .comment("能量消耗减免倍率上限")
            .defineInRange("energyLevelmax", 16,1,2147483647);
    
    // 调试日志配置
    private static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOG = BUILDER
            .comment("启用调试模式")
            .define("enableDebug", true);
    
    private static final ModConfigSpec.ConfigValue<String> DEBUG_LOG_LEVEL = BUILDER
            .comment("调试日志输出级别（TRACE/DEBUG/INFO/WARN/ERROR），低于此级别的日志将不会输出")
            .define("debugLogLevel", "DEBUG");

    static final ModConfigSpec SPEC = BUILDER.build();


    public static double maxConnectionDistance;
    public static int textColour;
    public static int agreementCoreHeartbeatInterval;
    public static int FIRING_RATE_LEVEL_MAX;
    public static int HARM_LEVEL_MAX;
    public static int ENERGY_LEVEL_MAX;
    public static boolean enableDebugLog;
    public static String debugLogLevel;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // 加载电网配置工具设置
        maxConnectionDistance = MAX_CONNECTION_DISTANCE.get();
        // 加载 GUI 颜色配置并解析为整数
        textColour = parseColor(TEXT_COLOUR.get());
        // 加载协议网络心跳间隔配置
        agreementCoreHeartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL.get();
        // 加载炮塔核心升级配置
        FIRING_RATE_LEVEL_MAX = FIRING_RATE_LEVEL_MAX_CONFIG.get();
        HARM_LEVEL_MAX = HARM_LEVEL_MAX_CONFIG.get();
        ENERGY_LEVEL_MAX = ENERGY_LEVEL_MAX_CONFIG.get();
        // 加载调试日志配置
        enableDebugLog = ENABLE_DEBUG_LOG.get();
        debugLogLevel = DEBUG_LOG_LEVEL.get();
        
        // 如果未启用调试模式，关闭日志系统
        if (!enableDebugLog) {
            DebugLogger.shutdown();
        } else {
            // 如果启用了调试模式，更新日志级别
            // 注意：由于配置加载晚于日志系统初始化，这里需要动态更新级别
            DebugLogger.setLogLevel(debugLogLevel);
            DebugLogger.info("配置已加载，日志级别: {}", debugLogLevel);
        }
    }

    /**
     * 解析十六进制颜色字符串为整数
     * @param colorStr 颜色字符串（如 "0x00AA00" 或 "#00AA00"）
     * @return 整数颜色值
     */
    private static int parseColor(String colorStr) {
        try {
            colorStr = colorStr.replace("0x", "").replace("#", "");
            return (int) Long.parseLong(colorStr, 16);
        } catch (NumberFormatException e) {
            return 0x000000;
        }
    }
    
    /**
     * 玩家进入世界时触发
     * @param event 玩家登录事件
     */
    @SubscribeEvent
    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (event != null) {
            var player = event.getEntity();
            if (!player.level().isClientSide) {
                // 服务器端逻辑
                player.sendSystemMessage(Component.literal("欢迎使用模块化防御，当前版本："+MODVERSION));
            }
        }
    }
    
    /**
     * 获取协议网络心跳间隔
     * @return 心跳间隔（tick）
     */
    public static int getAgreementCoreHeartbeatInterval() {
        return agreementCoreHeartbeatInterval;
    }
}
