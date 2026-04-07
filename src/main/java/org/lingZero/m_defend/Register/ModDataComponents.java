package org.lingZero.m_defend.Register;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.m_defend.DataComponents.DefendCoreData;
import org.lingZero.m_defend.DataComponents.TurretCoreData;
import org.lingZero.m_defend.ModularizationDefend;

/**
 * 数据组件类型注册
 * 使用 DeferredRegister.DataComponents 简化数据组件注册并避免泛型推断问题
 */
public class ModDataComponents {
    // 使用专门的 DataComponents 注册器
    @SuppressWarnings("removal")
    public static final DeferredRegister.DataComponents REGISTRAR =
            DeferredRegister.createDataComponents(ModularizationDefend.MODID);

    // DefendCore物品的核心模块数据组件
    // 同时支持持久化存储和网络同步
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DefendCoreData>> CORE_MODULE_DATA =
            REGISTRAR.registerComponentType("core_module_data", 
                    builder -> builder
                            .persistent(DefendCoreData.CODEC)  // 用于磁盘存储的编解码器
                            .networkSynchronized(DefendCoreData.STREAM_CODEC)  // 用于网络同步的流编解码器
                    );

    //TurretCore
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TurretCoreData>> TURRET_CORE_DATA =
            REGISTRAR.registerComponentType("turret_core_data",
                    builder -> builder
                            .persistent(TurretCoreData.CODEC)
                            .networkSynchronized(TurretCoreData.STREAM_CODEC)
            );

}
