package org.lingZero.modularization_defend.DataComponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.lingZero.modularization_defend.Config;

/**
 * TurretCore的数据组件记录类
 */
public record TurretCoreData(
        int firingRateLevel,      // 射速倍率等级
        int harmLevel,            // 伤害倍率等级
        int energyLevel,          // 能量消耗减免倍率等级
        TurretType turretType     // 炮塔核心类型
) {
    /**
     * 创建默认数据
     */
    public static TurretCoreData createDefault() {
        return new TurretCoreData(
                0,              // 默认射速等级
                0,              // 默认伤害等级
                0,              // 默认能量消耗减免等级
                TurretType.NONE // 默认炮塔核心类型
        );
    }
    
    /**
     * 更新射速等级
     */
    public TurretCoreData withFiringRateLevel(int level) {
        return new TurretCoreData(
                Math.max(0, Math.min(level, Config.FIRING_RATE_LEVEL_MAX)),
                harmLevel,
                energyLevel,
                turretType
        );
    }
    
    /**
     * 更新伤害等级
     */
    public TurretCoreData withHarmLevel(int level) {
        return new TurretCoreData(
                firingRateLevel,
                Math.max(0, Math.min(level, Config.HARM_LEVEL_MAX)),
                energyLevel,
                turretType
        );
    }
    
    /**
     * 更新能量消耗减免等级
     */
    public TurretCoreData withEnergyLevel(int level) {
        return new TurretCoreData(
                firingRateLevel,
                harmLevel,
                Math.max(0, Math.min(level, Config.ENERGY_LEVEL_MAX)),
                turretType
        );
    }
    
    /**
     * 更新炮塔核心类型
     */
    public TurretCoreData withTurretType(TurretType type) {
        return new TurretCoreData(
                firingRateLevel,
                harmLevel,
                energyLevel,
                type != null ? type : TurretType.NONE
        );
    }
    
    /**
     * 持久化编解码器 - 用于磁盘存储
     */
    public static final Codec<TurretCoreData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("firing_rate_level").forGetter(TurretCoreData::firingRateLevel),
            Codec.INT.fieldOf("harm_level").forGetter(TurretCoreData::harmLevel),
            Codec.INT.fieldOf("energy_level").forGetter(TurretCoreData::energyLevel),
            TurretType.CODEC.fieldOf("turret_type").forGetter(TurretCoreData::turretType)
    ).apply(instance, TurretCoreData::new));
    
    /**
     * 网络序列化编解码器 - 用于网络同步
     */
    public static final StreamCodec<ByteBuf, TurretCoreData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
