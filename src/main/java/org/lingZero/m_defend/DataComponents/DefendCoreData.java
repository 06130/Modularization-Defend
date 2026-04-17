package org.lingZero.m_defend.DataComponents;

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
    int harmLevel,            // 伤害倍率等级 (0-4)
    int energyExpendLevel,    // 能量消耗倍率等级 (0-4)
    long energyMax,           // 能量存储上限
    long energyCurrent,       // 当前能量
    double shieldCapacity,    // 护盾容量
    boolean shieldActive,     // 护盾系统状态
    String fortressCore,      // 炮台核心类型
    int speedUpgradeLevel,    // 速度升级等级 (0-4)
    int energyUpgradeLevel,   // 能量升级等级 (0-4)
    int efficiencyUpgradeLevel, // 效率升级等级 (0-4)
    int capacityUpgradeLevel, // 容量升级等级 (0-4)
    int securityUpgradeLevel  // 安全升级等级 (0-4)
) {
    /**
     * 创建默认数据
     */
    public static DefendCoreData createDefault() {
        return new DefendCoreData(
            0,      // 默认伤害等级
            0,      // 默认能量消耗等级
            10000,  // 默认能量存储上限
            0,      // 默认当前能量
            0.0,    // 默认护盾容量
            false,  // 护盾系统默认关闭
            "none", // 炮台核心类型
            0,      // 默认速度升级等级
            0,      // 默认能量升级等级
            0,      // 默认效率升级等级
            0,      // 默认容量升级等级
            0       // 默认安全升级等级
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
            harmLevel,
            energyExpendLevel,
            energyMax,
            Math.max(0, Math.min(newEnergy, energyMax)), // 确保在有效范围内
            shieldCapacity,
            shieldActive,
            fortressCore,
            speedUpgradeLevel,
            energyUpgradeLevel,
            efficiencyUpgradeLevel,
            capacityUpgradeLevel,
            securityUpgradeLevel
        );
    }
    
    /**
     * 更新护盾状态
     */
    public DefendCoreData withShieldActive(boolean active) {
        return new DefendCoreData(
            harmLevel,
            energyExpendLevel,
            energyMax,
            energyCurrent,
            shieldCapacity,
            active,
            fortressCore,
            speedUpgradeLevel,
            energyUpgradeLevel,
            efficiencyUpgradeLevel,
            capacityUpgradeLevel,
            securityUpgradeLevel
        );
    }
    
    /**
     * 更新炮台核心类型
     */
    public DefendCoreData withFortressCore(String core) {
        return new DefendCoreData(
            harmLevel,
            energyExpendLevel,
            energyMax,
            energyCurrent,
            shieldCapacity,
            shieldActive,
            core,
            speedUpgradeLevel,
            energyUpgradeLevel,
            efficiencyUpgradeLevel,
            capacityUpgradeLevel,
            securityUpgradeLevel
        );
    }
    
    /**
     * 更新速度升级等级
     */
    public DefendCoreData withSpeedUpgradeLevel(int level) {
        return new DefendCoreData(
            harmLevel,
            energyExpendLevel,
            energyMax,
            energyCurrent,
            shieldCapacity,
            shieldActive,
            fortressCore,
            Math.max(0, Math.min(level, 4)),
            energyUpgradeLevel,
            efficiencyUpgradeLevel,
            capacityUpgradeLevel,
            securityUpgradeLevel
        );
    }
    
    /**
     * 更新能量升级等级
     */
    public DefendCoreData withEnergyUpgradeLevel(int level) {
        return new DefendCoreData(
            harmLevel,
            energyExpendLevel,
            energyMax,
            energyCurrent,
            shieldCapacity,
            shieldActive,
            fortressCore,
            speedUpgradeLevel,
            Math.max(0, Math.min(level, 4)),
            efficiencyUpgradeLevel,
            capacityUpgradeLevel,
            securityUpgradeLevel
        );
    }
    
    /**
     * 更新效率升级等级
     */
    public DefendCoreData withEfficiencyUpgradeLevel(int level) {
        return new DefendCoreData(
            harmLevel,
            energyExpendLevel,
            energyMax,
            energyCurrent,
            shieldCapacity,
            shieldActive,
            fortressCore,
            speedUpgradeLevel,
            energyUpgradeLevel,
            Math.max(0, Math.min(level, 4)),
            capacityUpgradeLevel,
            securityUpgradeLevel
        );
    }
    
    /**
     * 更新容量升级等级
     */
    public DefendCoreData withCapacityUpgradeLevel(int level) {
        return new DefendCoreData(
            harmLevel,
            energyExpendLevel,
            energyMax,
            energyCurrent,
            shieldCapacity,
            shieldActive,
            fortressCore,
            speedUpgradeLevel,
            energyUpgradeLevel,
            efficiencyUpgradeLevel,
            Math.max(0, Math.min(level, 4)),
            securityUpgradeLevel
        );
    }
    
    /**
     * 更新安全升级等级
     */
    public DefendCoreData withSecurityUpgradeLevel(int level) {
        return new DefendCoreData(
            harmLevel,
            energyExpendLevel,
            energyMax,
            energyCurrent,
            shieldCapacity,
            shieldActive,
            fortressCore,
            speedUpgradeLevel,
            energyUpgradeLevel,
            efficiencyUpgradeLevel,
            capacityUpgradeLevel,
            Math.max(0, Math.min(level, 4))
        );
    }
    
    /**
     * 根据升级等级计算属性值
     * 每级提供不同的加成
     */
    public int getHarmMultiplier() {
        // 伤害升级：每级增加 15% 伤害
        return 100 + (energyUpgradeLevel * 15);
    }
    
    public int getEnergyEfficiency() {
        // 效率升级：每级减少 8% 能量消耗
        return Math.max(20, 100 - (efficiencyUpgradeLevel * 8));
    }
    
    public long getCalculatedEnergyMax() {
        // 容量升级：每级增加 50% 能量上限
        long baseEnergy = 10000;
        return baseEnergy + (capacityUpgradeLevel * 5000);
    }
    
    /**
     * 持久化编解码器 - 用于磁盘存储
     */
    public static final Codec<DefendCoreData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("harm_level").forGetter(DefendCoreData::harmLevel),
            Codec.INT.fieldOf("energy_expend_level").forGetter(DefendCoreData::energyExpendLevel),
            Codec.LONG.fieldOf("energy_max").forGetter(DefendCoreData::energyMax),
            Codec.LONG.fieldOf("energy_current").forGetter(DefendCoreData::energyCurrent),
            Codec.DOUBLE.fieldOf("shield_capacity").forGetter(DefendCoreData::shieldCapacity),
            Codec.BOOL.fieldOf("shield_active").forGetter(DefendCoreData::shieldActive),
            Codec.STRING.fieldOf("fortress_core").forGetter(DefendCoreData::fortressCore),
            Codec.INT.fieldOf("speed_upgrade_level").forGetter(DefendCoreData::speedUpgradeLevel),
            Codec.INT.fieldOf("energy_upgrade_level").forGetter(DefendCoreData::energyUpgradeLevel),
            Codec.INT.fieldOf("efficiency_upgrade_level").forGetter(DefendCoreData::efficiencyUpgradeLevel),
            Codec.INT.fieldOf("capacity_upgrade_level").forGetter(DefendCoreData::capacityUpgradeLevel),
            Codec.INT.fieldOf("security_upgrade_level").forGetter(DefendCoreData::securityUpgradeLevel)
    ).apply(instance, DefendCoreData::new));
    
    /**
     * 网络序列化编解码器 - 用于网络同步
     * 由于字段较多，使用 Codec 作为网络编解码器（NeoForge 会自动包装）
     */
    public static final StreamCodec<ByteBuf, DefendCoreData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
