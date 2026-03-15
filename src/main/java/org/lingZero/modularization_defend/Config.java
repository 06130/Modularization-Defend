package org.lingZero.modularization_defend;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ModularizationDefend.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 电网配置工具相关设置
    private static final ModConfigSpec.DoubleValue MAX_CONNECTION_DISTANCE = BUILDER
            .comment("电网配置工具的最大连接距离。注：不要调太大，我不保证会发生什么")
            .defineInRange("maxConnectionDistance", 64.0, 1.0, Double.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

    // 电网配置工具配置
    public static double maxConnectionDistance;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // 加载电网配置工具设置
        maxConnectionDistance = MAX_CONNECTION_DISTANCE.get();
    }
}
