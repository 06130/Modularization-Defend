package org.lingZero.modularization_defend;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import static org.lingZero.modularization_defend.ModularizationDefend.MODVERSION;

@EventBusSubscriber(modid = ModularizationDefend.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 电网配置工具相关设置
    private static final ModConfigSpec.DoubleValue MAX_CONNECTION_DISTANCE = BUILDER
            .comment("电网配置工具的最大连接距离。注：不要调太大，我不保证会发生什么")
            .defineInRange("maxConnectionDistance", 64.0, 1.0, Double.MAX_VALUE);

    // GUI 配置
    private static final ModConfigSpec.ConfigValue<String> TEXT_COLOUR = BUILDER
            .comment("GUI主要文本字体颜色（十六进制格式，如 0x00AA00）")
            .define("textColour", "0xFFEE00");

    static final ModConfigSpec SPEC = BUILDER.build();

    // 电网配置工具配置
    public static double maxConnectionDistance;

    // GUI 配置
    public static int textColour;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // 加载电网配置工具设置
        maxConnectionDistance = MAX_CONNECTION_DISTANCE.get();
        // 加载 GUI 颜色配置并解析为整数
        textColour = parseColor(TEXT_COLOUR.get());
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
}
