package org.lingZero.modularization_defend.DataComponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * DefendCore物品的数据组件记录类
 * 使用数据组件系统而不是NBT格式存储数据
 */
public record DefendCoreData(
    int firingRateLevel,      // 射速倍率
    int harmLevel,            // 伤害倍率  
    int energyExpendLevel,    // 能量消耗倍率
    long energyMax,           // 能量存储上限
    long energyCurrent,       // 当前能量
    double shieldCapacity,    // 护盾容量
    boolean shieldActive,     // 护盾系统状态
    String fortressCore       // 炮台核心类型
) {
    /**
     * 创建默认数据
     */
    public static DefendCoreData createDefault() {
        return new DefendCoreData(
            1,      // 默认射速倍率
            1,      // 默认伤害倍率
            1,      // 默认能量消耗倍率
            10000,  // 默认能量存储上限
            0,      // 默认当前能量
            0.0,    // 默认护盾容量
            false,  // 护盾系统默认关闭
            "null"  // 炮台核心类型
        );
    }
    
    /**
     * 检查是否为有效数据
     */
    public boolean isValid() {
        return energyMax > 0 && energyCurrent >= 0 && energyCurrent <= energyMax;
    }
    
    /**
     * 更新当前能量
     */
    public DefendCoreData withEnergyCurrent(long newEnergy) {
        return new DefendCoreData(
            firingRateLevel,
            harmLevel,
            energyExpendLevel,
            energyMax,
            Math.max(0, Math.min(newEnergy, energyMax)), // 确保在有效范围内
            shieldCapacity,
            shieldActive,
            fortressCore
        );
    }
    
    /**
     * 更新护盾状态
     */
    public DefendCoreData withShieldActive(boolean active) {
        return new DefendCoreData(
            firingRateLevel,
            harmLevel,
            energyExpendLevel,
            energyMax,
            energyCurrent,
            shieldCapacity,
            active,
            fortressCore
        );
    }
    
    /**
     * 更新炮台核心类型
     */
    public DefendCoreData withFortressCore(String core) {
        return new DefendCoreData(
            firingRateLevel,
            harmLevel,
            energyExpendLevel,
            energyMax,
            energyCurrent,
            shieldCapacity,
            shieldActive,
            core
        );
    }
    
    /**
     * 持久化编解码器 - 用于磁盘存储
     */
    public static final Codec<DefendCoreData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("firing_rate_level").forGetter(DefendCoreData::firingRateLevel),
            Codec.INT.fieldOf("harm_level").forGetter(DefendCoreData::harmLevel),
            Codec.INT.fieldOf("energy_expend_level").forGetter(DefendCoreData::energyExpendLevel),
            Codec.LONG.fieldOf("energy_max").forGetter(DefendCoreData::energyMax),
            Codec.LONG.fieldOf("energy_current").forGetter(DefendCoreData::energyCurrent),
            Codec.DOUBLE.fieldOf("shield_capacity").forGetter(DefendCoreData::shieldCapacity),
            Codec.BOOL.fieldOf("shield_active").forGetter(DefendCoreData::shieldActive),
            Codec.STRING.fieldOf("fortress_core").forGetter(DefendCoreData::fortressCore)
    ).apply(instance, DefendCoreData::new));
    
    /**
     * 网络序列化编解码器 - 用于网络同步
     * 由于字段较多，使用 Codec 作为网络编解码器（NeoForge 会自动包装）
     */
    public static final StreamCodec<ByteBuf, DefendCoreData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
