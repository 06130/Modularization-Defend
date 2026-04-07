package org.lingZero.m_defend.Register;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.lingZero.m_defend.ModularizationDefend;
import org.lingZero.m_defend.ldlibUI.DefendCoreContainerMenu;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, ModularizationDefend.MODID);
    
     // DefendCore GUI 容器菜单类型
    public static final DeferredHolder<MenuType<?>, MenuType<DefendCoreContainerMenu>> DEFEND_CORE_MENU =
            MENUS.register("defend_core_menu", () ->
                    IMenuTypeExtension.create((windowId, playerInventory, buffer) -> {
                        return new DefendCoreContainerMenu(windowId, playerInventory);
                    })
            );
}
