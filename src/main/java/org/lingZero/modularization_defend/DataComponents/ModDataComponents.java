package org.lingZero.modularization_defend.DataComponents;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组数据组件注册中心。
 * 使用 NeoForge 的 DataComponent API 实现物品数据的持久化与网络同步。
 */
public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "modularization_defend");

    /**
     * 存储实体类型 ID 的数据组件。
     * 用于实体选取器等需要记录实体类型的物品。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> STORED_ENTITY_ID =
            DATA_COMPONENTS.register("stored_entity_id",
                    () -> DataComponentType.<ResourceLocation>builder()
                            .persistent(ResourceLocation.CODEC)
                            .networkSynchronized(ResourceLocation.STREAM_CODEC)
                            .build());

    /**
     * 存储玩家名称的数据组件（用于玩家ID卡）。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> STORED_PLAYER_NAME =
            DATA_COMPONENTS.register("stored_player_name",
                    () -> DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build());

    /**
     * 存储玩家 UUID 的数据组件（用于玩家ID卡，以字符串形式存储）。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> STORED_PLAYER_UUID =
            DATA_COMPONENTS.register("stored_player_uuid",
                    () -> DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build());
}
