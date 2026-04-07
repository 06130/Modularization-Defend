package org.lingZero.modularization_defend.DataComponents;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.StringRepresentable;
import net.neoforged.fml.common.asm.enumextension.*;
import net.neoforged.fml.common.asm.enumextension.NetworkedEnum.NetworkCheck;

import java.util.function.IntFunction;

/**
 * 炮塔核心类型枚举
 * 支持 NeoForge 的枚举扩展系统，允许其他模组动态添加新的炮塔类型
 * 实现了字符串表示和网络同步功能
 */
@NetworkedEnum(NetworkCheck.BIDIRECTIONAL)  // 支持双向网络同步检查
@IndexedEnum  // 支持基于索引的映射
@NamedEnum(1)  // 支持命名枚举
public enum TurretType implements StringRepresentable, IExtensibleEnum {
    NONE(0, "none"),      // 无类型（默认值）
    LASER(1, "laser"),    // 能量激光炮塔
    MISSILE(2, "missile"),// 导弹炮塔
    HOWITZER(3, "howitzer"),  // 榴弹炮塔
    RANGE(4, "range"); // 范围法术炮塔

    /** 编解码器 - 用于磁盘持久化存储，通过字符串名称序列化 */
    public static final Codec<TurretType> CODEC = StringRepresentable.fromValues(TurretType::values);
    
    /** ID 映射函数 - 用于通过整数 ID 查找枚举值，越界时返回 NONE */
    public static final IntFunction<TurretType> BY_ID = ByIdMap.continuous((p_335877_) -> p_335877_.id, values(), OutOfBoundsStrategy.ZERO);
    
    /** 流编解码器 - 用于网络数据包同步，通过 ID 进行高效传输 */
    public static final StreamCodec<ByteBuf, TurretType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, (p_335484_) -> p_335484_.id);
    
    /** 枚举的唯一标识 ID，用于网络传输和映射 */
    private final int id;
    
    /** 枚举的字符串名称，用于序列化和显示 */
    private final String name;

    /**
     * 构造函数
     * @param id 枚举的唯一标识 ID
     * @param name 枚举的字符串名称
     */
    private TurretType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 获取枚举的 ID
     * @return 枚举的唯一标识 ID
     */
    public int getId() {
        return this.id;
    }

    /**
     * 获取枚举的字符串名称（实现 StringRepresentable 接口）
     * @return 枚举的字符串表示
     */
    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * 获取枚举扩展信息（实现 IExtensibleEnum 接口）
     * 用于 NeoForge 的枚举扩展系统
     * @return 枚举扩展信息对象
     */
    public static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(TurretType.class);
    }
}
