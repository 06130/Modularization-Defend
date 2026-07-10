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

    // 示例食物物品，ID 为 "modularization_defend:example_item"，始终可食用，营养值 1，饱和度 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item",
            new Item.Properties().food(new FoodProperties.Builder()
                    .alwaysEdible().nutrition(1).saturationModifier(2f).build()));
}
