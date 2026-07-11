package org.lingZero.modularization_defend.CreativeTab;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.lingZero.modularization_defend.Item.ModItems;

/**
 * 模组创造模式标签页注册中心，统一管理所有自定义标签页的注册与物品填充。
 */
public class ModCreativeTabs {
    // 创建延迟注册表，所有创造模式标签都在 "modularization_defend" 命名空间下注册
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, "modularization_defend");

    // 示例标签页：标题为 "itemGroup.modularization_defend"，图标使用示例物品
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register(
            "example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modularization_defend"))
                    .icon(() -> ModItems.EXAMPLE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.EXAMPLE_ITEM.get());
                        output.accept(ModItems.COMBAT_CONSOLE_ITEM.get());
                        output.accept(ModItems.BLUE_DOOR_ITEM.get());
                        output.accept(ModItems.DEPLOYABLE_ZONE_ITEM.get());
                        output.accept(ModItems.DEPLOYABLE_ZONE_HIGH_ITEM.get());
                        output.accept(ModItems.RED_DOOR_ITEM.get());
                        output.accept(ModItems.ENTITY_SELECTOR.get());
                        output.accept(ModItems.PLAYER_ID_CARD.get());
                    }).build());

    /**
     * 将模组物品添加到原版创造模式标签页中。
     * 当前逻辑：把示例方块物品添加到"建筑方块"标签页。
     */
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(ModItems.EXAMPLE_BLOCK_ITEM);
    }
}
