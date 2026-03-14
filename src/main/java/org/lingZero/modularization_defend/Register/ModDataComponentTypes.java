package org.lingZero.modularization_defend.Register;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.ModularizationDefend;

/**
 * 数据组件类型注册
 */
public class ModDataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, ModularizationDefend.MODID);

}
