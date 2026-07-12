package org.lingZero.modularization_defend.Block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.Block.bounding.BoundingBlock;
import org.lingZero.modularization_defend.Block.example.BlueDoorBlock;
import org.lingZero.modularization_defend.Block.example.CombatConsoleBlock;
import org.lingZero.modularization_defend.Block.example.RedDoorBlock;
import org.lingZero.modularization_defend.Block.example.DeployableZoneHighBlock;
import org.lingZero.modularization_defend.Block.DeployableZoneBlock;

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
     * 作战控制台——4 格高竖直柱体多方块结构。
     */
    public static final DeferredBlock<CombatConsoleBlock> COMBAT_CONSOLE = BLOCKS.register("combat_console",
            CombatConsoleBlock::new);

    /**
     * 蓝门多方块结构——3x3x3 立方体，GeckoLib动画渲染，可瞄准交互但无碰撞（可穿过）。
     */
    public static final DeferredBlock<BlueDoorBlock> BLUE_DOOR = BLOCKS.register("bluedoor",
            BlueDoorBlock::new);

    public static final DeferredBlock<DeployableZoneBlock> DEPLOYABLE_ZONE = BLOCKS.register("deployable_zone",
            DeployableZoneBlock::new);

    public static final DeferredBlock<DeployableZoneHighBlock> DEPLOYABLE_ZONE_HIGH = BLOCKS.register("deployable_zone_high",
            DeployableZoneHighBlock::new);

    public static final DeferredBlock<RedDoorBlock> RED_DOOR = BLOCKS.register("reddoor",
            RedDoorBlock::new);
}
