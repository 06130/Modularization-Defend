package org.lingZero.modularization_defend.Register;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.lingZero.modularization_defend.Blocks.ElectricityRepeater.ElectricityRepeaterMenu;
import org.lingZero.modularization_defend.ModularizationDefend;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, ModularizationDefend.MODID);
    
    // 电力中继器菜单类型
    public static final DeferredHolder<MenuType<?>, MenuType<ElectricityRepeaterMenu>> ELECTRICITY_REPEATER_MENU =
            MENUS.register("electricity_repeater_menu", () ->
                    IMenuTypeExtension.create((windowId, playerInventory, buffer) -> {
                        // 从 BlockEntity 创建菜单
                        // 注意：实际使用时需要从 buffer 读取 BlockEntity 位置
                        return new ElectricityRepeaterMenu(windowId, playerInventory);
                    })
            );
}
