package org.lingZero.modularization_defend.Item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.lingZero.modularization_defend.Block.ModBlocks;

/**
 * 模组物品注册中心，统一管理本模组所有物品（含方块物品）的注册定义。
 */
public class ModItems {
    // 创建延迟注册表，所有物品都在 "modularization_defend" 命名空间下注册
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("modularization_defend");

    // 示例方块的对应方块物品，ID 为 "modularization_defend:example_block"
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block",
            ModBlocks.EXAMPLE_BLOCK);

    // 作战控制台的方块物品
    public static final DeferredItem<BlockItem> COMBAT_CONSOLE_ITEM = ITEMS.registerSimpleBlockItem("combat_console",
            ModBlocks.COMBAT_CONSOLE);

    // 蓝门多方块结构的方块物品
    public static final DeferredItem<BlockItem> BLUE_DOOR_ITEM = ITEMS.registerSimpleBlockItem("bluedoor",
            ModBlocks.BLUE_DOOR);

    // 示例食物物品，ID 为 "modularization_defend:example_item"，始终可食用，营养值 1，饱和度 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item",
            new Item.Properties().food(new FoodProperties.Builder()
                    .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // 实体选取器——右键实体存储其类型 ID
    public static final DeferredItem<EntitySelectorItem> ENTITY_SELECTOR = ITEMS.registerItem("entity_selector",
            EntitySelectorItem::new);
}
