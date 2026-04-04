package org.lingZero.modularization_defend.Register;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.lingZero.modularization_defend.Items.DefendCore;
import org.lingZero.modularization_defend.Items.DefendCoreEnergyStorage;
import org.lingZero.modularization_defend.ModularizationDefend;

/**
 * 能力注册器
 * 使用 NeoForge 的能力系统注册物品的能量存储能力
 */
@EventBusSubscriber(modid = ModularizationDefend.MODID)
public class ModCapabilities {
    
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 为 DefendCore 物品注册能量存储能力
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,  // 注册为物品能量存储能力
            (stack, context) -> {
                // 创建能量存储实例
                return new DefendCoreEnergyStorage(stack, ModDataComponents.CORE_MODULE_DATA.get());
            },
            ModItems.DEFEND_CORE.get()  // 仅对 DefendCore 物品生效
        );
    }
}
