package org.lingZero.modularization_defend.Register;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.ModularizationDefend;

/**
 * 数据组件类型注册
 */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, ModularizationDefend.MODID);

    // DefendCore物品的核心模块数据组件
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> CORE_MODULE_DATA = 
            DATA_COMPONENT_TYPES.register("core_module_data", 
                    () -> DataComponentType.<CompoundTag>builder()
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
                            .build());

}
