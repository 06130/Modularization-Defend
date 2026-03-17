package org.lingZero.modularization_defend.Blocks.ElectricityRepeater;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockData;

/**
 * 电力中继器多方块数据
 * 专用于电力中继器的 2x2x10 结构
 */
public class ElectricityRepeaterMultiblockData extends MultiblockData {
    
    public ElectricityRepeaterMultiblockData(Level level, BlockPos controllerPos) {
        super(level, controllerPos);
    }
    
    @Override
    protected boolean isPositionInStructure(int x, int y, int z) {
        // 电力中继器结构：2x2x10
        return x >= 0 && x < 2 &&
               y >= 0 && y < 10 &&
               z >= 0 && z < 2;
    }
}
