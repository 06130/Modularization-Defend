package org.lingZero.modularization_defend.Blocks.AgreementCore;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lingZero.modularization_defend.Blocks.Multiblock.IMultiblockConfig;
import org.lingZero.modularization_defend.Blocks.Multiblock.IMultiblockComponent;
import org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockFormation;
import org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockHandler;
import org.lingZero.modularization_defend.Config;
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
    protected Block getBlockType() {
        return ModBlocks.AGREEMENT_CORE_BLOCK.get();
    }
    
    @Override
    public void formMultiblockWithPlacement(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 使用 MultiblockFormation 来填充缺失的方块
        var formation = new MultiblockFormation(level, pos, CONFIG);
        formation.fillMissingBlocks(
            getBlockType(),
            (blockEntity, isController) -> {
                // 设置新方块的控制器标志
                if (blockEntity instanceof IMultiblockComponent component) {
                    component.setController(isController);
                }
                // 设置心跳间隔为配置值
                if (blockEntity instanceof AgreementCoreBlockEntity core) {
                    core.loadHeartbeatIntervalFromConfig();
                }
            }
        );
    }
}
