package org.lingZero.modularization_defend.items.BlockItem;

import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;

import static org.lingZero.modularization_defend.blocks.example_block.EXAMPLE_BLOCK;
import static org.lingZero.modularization_defend.modularization_defend.ITEMS;

public class example_block {
    // 创建一个新方块物品，ID 为 "modularization_defend:example_block"，结合命名空间和路径
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);
}
