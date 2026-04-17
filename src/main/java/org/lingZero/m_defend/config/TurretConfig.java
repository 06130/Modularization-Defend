package org.lingZero.m_defend.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 炮塔配置类
 * 包含所有炮塔类型的属性配置
 */
public class TurretConfig {
    
    private final ModConfigSpec spec;
    
    // 一级实弹炮塔配置
    public final BasicBulletV1Stats basicBulletV1;
    
    public TurretConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        
        builder.comment("炮塔配置").push("turret");
        
        // 初始化各级炮塔配置
        this.basicBulletV1 = new BasicBulletV1Stats(builder);
        
        builder.pop();
        
        spec = builder.build();
    }
    
    public ModConfigSpec getSpec() {
        return spec;
    }
    
    /**
     * 一级实弹炮塔属性配置
     */
    public static class BasicBulletV1Stats {
        public final ModConfigSpec.IntValue fireRate;
        public final ModConfigSpec.IntValue damage;
        public final ModConfigSpec.IntValue range;
        public final ModConfigSpec.LongValue energyCapacity;
        public final ModConfigSpec.IntValue searchHeight;
        
        BasicBulletV1Stats(ModConfigSpec.Builder builder) {
            builder.comment("一级实弹炮塔基础属性").push("basic_bullet_v1");
            
            fireRate = builder
                    .comment("基础射速（发/秒）", "警告：过高的射速会导致卡顿")
                    .defineInRange("fire_rate", 1, 1, 20);
            
            damage = builder
                    .comment("基础伤害")
                    .defineInRange("damage", 5, 1, Integer.MAX_VALUE);
            
            range = builder
                    .comment("射程（格）", "警告：过大的值可能导致性能问题")
                    .defineInRange("range", 30, 1, Integer.MAX_VALUE);
            
            energyCapacity = builder
                    .comment("能量缓冲区大小（FE）")
                    .defineInRange("energy_capacity", 1000L, 1L, Long.MAX_VALUE);
            
            searchHeight = builder
                    .comment("目标搜索高度（格）", "警告：过大的值可能导致性能问题")
                    .defineInRange("search_height", 15, 1, Integer.MAX_VALUE);
            
            builder.pop();
        }
    }
}
