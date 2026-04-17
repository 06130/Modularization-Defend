package org.lingZero.m_defend.DataComponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.lingZero.m_defend.Config;

/**
 * TurretCore的数据组件记录类
 */
public record TurretCoreData(
        int firingRateLevel,      // 射速倍率等级
        int harmLevel,            // 伤害倍率等级
        int energyLevel,          // 能量消耗减免倍率等级
        TurretType turretType,     // 炮塔同调类型
        long energy, // 当前能量存储
        long maxEnergy // 最大能量存储

) {
    /**
     * 创建默认数据
     */
    public static TurretCoreData createDefault() {
        return new TurretCoreData(
                0,              // 默认射速等级
                0,              // 默认伤害等级
                0,              // 默认能量消耗减免等级
                TurretType.NONE, // 默认炮塔核心类型
                0L,             // 默认当前能量
                100L              // 默认最大能量
        );
    }
    
    /**
     * 更新射速等级
     */
    public TurretCoreData withFiringRateLevel(int level) {
        return new TurretCoreData(
                Math.max(0, Math.min(level, Config.getFiringRateLevelMax())),
                harmLevel,
                energyLevel,
                turretType,
                energy,
                maxEnergy
        );
    }
    
    /**
     * 更新伤害等级
     */
    public TurretCoreData withHarmLevel(int level) {
        return new TurretCoreData(
                firingRateLevel,
                Math.max(0, Math.min(level, Config.getHarmLevelMax())),
                energyLevel,
                turretType,
                energy,
                maxEnergy
        );
    }
    
    /**
     * 更新能量消耗减免等级
     */
    public TurretCoreData withEnergyLevel(int level) {
        return new TurretCoreData(
                firingRateLevel,
                harmLevel,
                Math.max(0, Math.min(level, Config.getEnergyLevelMax())),
                turretType,
                energy,
                maxEnergy
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
                type != null ? type : TurretType.NONE,
                energy,
                maxEnergy
        );
    }
    
    /**
     * 更新当前能量
     */
    public TurretCoreData withEnergy(long energy) {
        return new TurretCoreData(
                firingRateLevel,
                harmLevel,
                energyLevel,
                turretType,
                Math.max(0, Math.min(energy, maxEnergy)),
                maxEnergy
        );
    }
    
    /**
     * 更新最大能量
     */
    public TurretCoreData withMaxEnergy(long maxEnergy) {
        return new TurretCoreData(
                firingRateLevel,
                harmLevel,
                energyLevel,
                turretType,
                Math.min(energy, maxEnergy),
                Math.max(0, maxEnergy)
        );
    }
    
    /**
     * 持久化编解码器 - 用于磁盘存储
     */
    public static final Codec<TurretCoreData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("firing_rate_level").forGetter(TurretCoreData::firingRateLevel),
            Codec.INT.fieldOf("harm_level").forGetter(TurretCoreData::harmLevel),
            Codec.INT.fieldOf("energy_level").forGetter(TurretCoreData::energyLevel),
            TurretType.CODEC.fieldOf("turret_type").forGetter(TurretCoreData::turretType),
            Codec.LONG.fieldOf("energy").forGetter(TurretCoreData::energy),
            Codec.LONG.fieldOf("max_energy").forGetter(TurretCoreData::maxEnergy)
    ).apply(instance, TurretCoreData::new));
    
    /**
     * 网络序列化编解码器 - 用于网络同步
     */
    public static final StreamCodec<ByteBuf, TurretCoreData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
