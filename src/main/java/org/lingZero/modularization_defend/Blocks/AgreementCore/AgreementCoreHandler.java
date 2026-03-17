package org.lingZero.modularization_defend.Blocks.AgreementCore;

import org.lingZero.modularization_defend.Blocks.Multiblock.IMultiblockConfig;
import org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockHandler;
import org.lingZero.modularization_defend.Register.ModBlocks;

/**
 * 协议核心多方块处理器
 */
public class AgreementCoreHandler extends MultiblockHandler {
    
    private static final IMultiblockConfig CONFIG = new AgreementCoreConfig();
    
    public AgreementCoreHandler() {
        super(CONFIG);
    }
    
    @Override
    protected net.minecraft.world.level.block.Block getBlockType() {
        return ModBlocks.AGREEMENT_CORE_BLOCK.get();
    }
}
