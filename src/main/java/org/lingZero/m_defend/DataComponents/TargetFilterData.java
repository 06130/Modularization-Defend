package org.lingZero.m_defend.DataComponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * 目标过滤器数据组件
 * 用于存储过滤器物品的配置信息
 */
public record TargetFilterData(
        FilterType filterType,           // 过滤器类型
        Optional<String> entityId        // 实体ID（仅当 filterType 为 ENTITY_ID 时使用）
) {
    
    /**
     * 过滤器类型枚举
     */
    public enum FilterType {
        HOSTILE,      // 敌对生物
        NEUTRAL,      // 中立生物
        FRIENDLY,     // 友好生物
        PLAYER,       // 玩家
        ENTITY_ID     // 特定实体ID
    }
    
    /**
     * 创建默认数据（敌对生物过滤器）
     */
    public static TargetFilterData createDefault() {
        return new TargetFilterData(FilterType.HOSTILE, Optional.empty());
    }
    
    /**
     * 创建敌对生物过滤器
     */
    public static TargetFilterData hostile() {
        return new TargetFilterData(FilterType.HOSTILE, Optional.empty());
    }
    
    /**
     * 创建中立生物过滤器
     */
    public static TargetFilterData neutral() {
        return new TargetFilterData(FilterType.NEUTRAL, Optional.empty());
    }
    
    /**
     * 创建友好生物过滤器
     */
    public static TargetFilterData friendly() {
        return new TargetFilterData(FilterType.FRIENDLY, Optional.empty());
    }
    
    /**
     * 创建玩家过滤器
     */
    public static TargetFilterData player() {
        return new TargetFilterData(FilterType.PLAYER, Optional.empty());
    }
    
    /**
     * 创建实体ID过滤器
     * 
     * @param entityId 实体ID字符串
     */
    public static TargetFilterData entityId(String entityId) {
        return new TargetFilterData(FilterType.ENTITY_ID, Optional.of(entityId));
    }
    
    /**
     * 更新过滤器类型
     */
    public TargetFilterData withFilterType(FilterType type) {
        return new TargetFilterData(type, Optional.empty());
    }
    
    /**
     * 更新实体ID
     */
    public TargetFilterData withEntityId(String entityId) {
        return new TargetFilterData(FilterType.ENTITY_ID, Optional.of(entityId));
    }
    
    /**
     * 持久化编解码器 - 用于磁盘存储
     */
    public static final Codec<TargetFilterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(
                str -> FilterType.valueOf(str.toUpperCase()),
                FilterType::name
            ).fieldOf("filter_type").forGetter(TargetFilterData::filterType),
            Codec.STRING.optionalFieldOf("entity_id").forGetter(TargetFilterData::entityId)
    ).apply(instance, TargetFilterData::new));
    
    /**
     * 网络序列化编解码器 - 用于网络同步
     */
    public static final StreamCodec<ByteBuf, TargetFilterData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
