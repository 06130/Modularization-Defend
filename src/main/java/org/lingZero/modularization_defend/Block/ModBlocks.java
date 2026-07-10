package org.lingZero.modularization_defend.Block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组方块注册中心，统一管理本模组所有方块的注册定义。
 */
public class ModBlocks {
    // 创建延迟注册表，所有方块都在 "modularization_defend" 命名空间下注册
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("modularization_defend");

    // 示例方块：ID 为 "modularization_defend:example_block"
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
}
