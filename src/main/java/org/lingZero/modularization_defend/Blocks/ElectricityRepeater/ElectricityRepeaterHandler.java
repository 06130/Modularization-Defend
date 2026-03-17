package org.lingZero.modularization_defend.Blocks.ElectricityRepeater;

import org.lingZero.modularization_defend.Blocks.Multiblock.IMultiblockConfig;
import org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockHandler;
import org.lingZero.modularization_defend.Register.ModBlocks;

/**
 * 电力中继器多方块处理器
 */
public class ElectricityRepeaterHandler extends MultiblockHandler {
    
    private static final IMultiblockConfig CONFIG = new ElectricityRepeaterConfig();
    
    public ElectricityRepeaterHandler() {
        super(CONFIG);
    }
    
    @Override
    protected net.minecraft.world.level.block.Block getBlockType() {
        return ModBlocks.ELECTRICITY_REPEATER_BLOCK.get();
    }
}
