package org.lingZero.m_defend.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 调试配置类
 * 控制调试日志的启用和级别
 */
public class DebugConfig {
    
    private final ModConfigSpec spec;
    
    public final ModConfigSpec.BooleanValue enabled;
    public final ModConfigSpec.ConfigValue<String> logLevel;
    
    public DebugConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        
        builder.comment("调试配置");
        
        enabled = builder
                .comment("启用调试模式")
                .define("enabled", true);
        
        logLevel = builder
                .comment("调试日志输出级别（TRACE/DEBUG/INFO/WARN/ERROR）", "低于此级别的日志将不会输出")
                .define("log_level", "DEBUG");
        
        spec = builder.build();
    }
    
    public ModConfigSpec getSpec() {
        return spec;
    }
}
