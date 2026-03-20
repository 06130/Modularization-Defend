package org.lingZero.modularization_defend.Blocks.ElectricityRepeater;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.AbstractMultiblock;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.IMultiblockStructure;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.MultiblockStructure;
import org.lingZero.modularization_defend.Register.ModBlocks;

/**
 * 电力中继器多方块定义
 * 使用新的 Multiblock 框架实现
 * 结构：2x2x10（宽 x 高 x 深）
 */
public class ElectricityRepeaterMultiblockDef extends AbstractMultiblock {
    
    private static final ElectricityRepeaterMultiblockDef INSTANCE = new ElectricityRepeaterMultiblockDef();
    
    public ElectricityRepeaterMultiblockDef() {
        super("electricity_repeater");
    }
    
    /**
     * 获取单例实例
     */
    @NotNull
    public static ElectricityRepeaterMultiblockDef getInstance() {
        return INSTANCE;
    }
    
    @Override
    @NotNull
    protected IMultiblockStructure createStructure() {
        // 创建 2x2x10 的结构
        return MultiblockStructure.start("ElectricityRepeater")
                // 第 1 层（底部）- 2x2
                .aisle(
                    "BB",
                    "YB"
                )
                // 第 2-9 层 - 2x2
                .aisle("BB", "BB")
                .aisle("BB", "BB")
                .aisle("BB", "BB")
                .aisle("BB", "BB")
                .aisle("BB", "BB")
                .aisle("BB", "BB")
                .aisle("BB", "BB")
                .aisle("BB", "BB")
                // 第 10 层（顶部）- 2x2
                .aisle(
                    "BB",
                    "BB"
                )
                // 定义字符对应的方块
                .where('Y', getMasterBlock())  // Y 代表主方块（控制器）
                .where('B', getPartBlock())    // B 代表其他结构方块
                // 设置主方块位置（左下角）
                .setMaster('Y')
                .build();
    }
    
    @Override
    @NotNull
    protected Block getMasterBlock() {
        // 返回主方块（控制器方块）
        return ModBlocks.ELECTRICITY_REPEATER_BLOCK.get();
    }
    
    @Override
    @NotNull
    protected Block getPartBlock() {
        // 返回结构方块（与主方块相同）
        return ModBlocks.ELECTRICITY_REPEATER_BLOCK.get();
    }
    
    /**
     * 获取结构尺寸信息
     */
    public int getWidth() {
        return 2;  // X 轴宽度
    }
    
    public int getHeight() {
        return 10; // Y 轴高度
    }
    
    public int getDepth() {
        return 2;  // Z 轴深度
    }
}
