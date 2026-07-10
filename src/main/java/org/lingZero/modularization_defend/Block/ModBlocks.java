package org.lingZero.modularization_defend.Block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.Block.bounding.BoundingBlock;
import org.lingZero.modularization_defend.Block.example.ExampleMultiblockBlock;
import org.lingZero.modularization_defend.Block.example.GhostMultiblockBlock;

/**
 * 模组方块注册中心，统一管理本模组所有方块的注册定义。
 */
public class ModBlocks {
    // 创建延迟注册表，所有方块都在 "modularization_defend" 命名空间下注册
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("modularization_defend");

    // 示例方块：ID 为 "modularization_defend:example_block"
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE));

    // ==================== 多方块系统 ====================

    /**
     * 不可见的占位方块，用于填充多方块结构的虚拟体积。
     * 该方块不注册物品，玩家无法通过正常途径获取。
     */
    public static final DeferredBlock<BoundingBlock> BOUNDING_BLOCK = BLOCKS.register("bounding_block",
            BoundingBlock::new);

    /**
     * 示例多方块结构的主方块。放置时自动在周围生成占位方块。
     */
    public static final DeferredBlock<ExampleMultiblockBlock> EXAMPLE_MULTIBLOCK = BLOCKS.register("example_multiblock",
            ExampleMultiblockBlock::new);

    /**
     * 幽灵多方块结构——3x3x3 立方体，可瞄准交互但无碰撞（可穿过）。
     */
    public static final DeferredBlock<GhostMultiblockBlock> GHOST_MULTIBLOCK = BLOCKS.register("ghost_multiblock",
            GhostMultiblockBlock::new);
}
