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

    // 可部署区的方块物品
    public static final DeferredItem<BlockItem> DEPLOYABLE_ZONE_ITEM = ITEMS.registerSimpleBlockItem("deployable_zone",
            ModBlocks.DEPLOYABLE_ZONE);

    // 可部署区（高台）的方块物品
    public static final DeferredItem<BlockItem> DEPLOYABLE_ZONE_HIGH_ITEM = ITEMS.registerSimpleBlockItem("deployable_zone_high",
            ModBlocks.DEPLOYABLE_ZONE_HIGH);

    // 红门多方块结构的方块物品
    public static final DeferredItem<BlockItem> RED_DOOR_ITEM = ITEMS.registerSimpleBlockItem("reddoor",
            ModBlocks.RED_DOOR);

    // 关卡编辑器的方块物品
    public static final DeferredItem<BlockItem> LEVEL_EDITOR_ITEM = ITEMS.registerSimpleBlockItem("level_editor",
            ModBlocks.LEVEL_EDITOR);

    // 关卡控制器的方块物品
    public static final DeferredItem<BlockItem> LEVEL_CONTROLLER_ITEM = ITEMS.registerSimpleBlockItem("level_controller",
            ModBlocks.LEVEL_CONTROLLER);

    // 示例食物物品，ID 为 "modularization_defend:example_item"，始终可食用，营养值 1，饱和度 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item",
            new Item.Properties().food(new FoodProperties.Builder()
                    .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    // 实体选取器——右键实体存储其类型 ID
    public static final DeferredItem<EntitySelectorItem> ENTITY_SELECTOR = ITEMS.registerItem("entity_selector",
            EntitySelectorItem::new);

    // 玩家ID卡——潜行+右键写入/清除玩家信息
    public static final DeferredItem<PlayerIdCardItem> PLAYER_ID_CARD = ITEMS.registerItem("player_id_card",
            PlayerIdCardItem::new);

    // 程序存储卡——潜行+右键打开节点图编辑器
    public static final DeferredItem<ProgramStorageCardItem> PROGRAM_STORAGE_CARD = ITEMS.registerItem(
            "program_storage_card", ProgramStorageCardItem::new);
}
