package org.lingZero.modularization_defend.Register;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.ModularizationDefend;

/**
 * 数据组件类型注册
 */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, ModularizationDefend.MODID);

    public static final DataComponentType<?> MY_COMPONENT_TYPE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModularizationDefend.MODID, "my_component"),
            DataComponentType.<Double>builder().persistent(Codec.DOUBLE).build()
    );

}
