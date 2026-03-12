package org.lingZero.modularization_defend.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;

import static org.lingZero.modularization_defend.modularization_defend.BLOCKS;

public class example_block {
    // 创建一个新方块，ID 为 "modularization_defend:example_block"，结合命名空间和路径
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));

}
