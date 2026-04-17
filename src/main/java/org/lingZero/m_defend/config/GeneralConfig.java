package org.lingZero.m_defend.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 通用配置类
 * 包含电网、GUI、协议网络等基础配置
 */
public class GeneralConfig {
    
    private final ModConfigSpec spec;
    
    // 电网配置
    public final ModConfigSpec.DoubleValue maxConnectionDistance;

    // 协议网络配置
    public final ModConfigSpec.IntValue heartbeatInterval;
    
    // 炮塔核心升级配置
    public final ModConfigSpec.IntValue harmLevelMax;
    public final ModConfigSpec.IntValue energyLevelMax;
    
    public GeneralConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        
        // ==================== 电网配置 ====================
        builder.comment("电网配置").push("network");
        
        maxConnectionDistance = builder
                .comment("电网配置工具的最大连接距离", "警告：不要设置过大，可能导致未知问题")
                .defineInRange("max_connection_distance", 64.0, 1.0, Double.MAX_VALUE);
        
        builder.pop();

        
        // ==================== 协议网络配置 ====================
        builder.comment("协议网络配置").push("protocol");
        
        heartbeatInterval = builder
                .comment("协议网络默认心跳间隔（tick）", "警告：如果你不知道这是什么，请不要修改")
                .defineInRange("heartbeat_interval", 40, 1, 65536);
        
        builder.pop();
        
        // ==================== 炮塔核心升级配置 ====================
        builder.comment("炮塔核心升级配置").push("turret_core");
        
        harmLevelMax = builder
                .comment("伤害倍率上限")
                .defineInRange("harm_level_max", 16, 1, Integer.MAX_VALUE);
        
        energyLevelMax = builder
                .comment("能量消耗减免倍率上限")
                .defineInRange("energy_level_max", 16, 1, Integer.MAX_VALUE);
        
        builder.pop();
        
        spec = builder.build();
    }
    
    public ModConfigSpec getSpec() {
        return spec;
    }
}
