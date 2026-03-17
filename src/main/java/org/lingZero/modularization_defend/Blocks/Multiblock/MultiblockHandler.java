package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * 通用多方块处理器
 * 基于配置实现多方块的成型、破坏等操作
 */
public class MultiblockHandler implements IMultiblockHandler {
    
    private final IMultiblockConfig config;
    
    public MultiblockHandler(IMultiblockConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean tryFormMultiblock(Level level, BlockPos pos, @Nullable Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }
        
        // 使用 MultiblockFormation 来处理成型逻辑
        MultiblockFormation formation = new MultiblockFormation(level, pos, config);
        return formation.tryForm(player);
    }
    
    /**
     * 检测是否可以形成多方块结构
     */
    public boolean canFormMultiblock(Level level, BlockPos pos) {
        if (level == null) {
            return false;
        }
        
        MultiblockFormation formation = new MultiblockFormation(level, pos, config);
        return formation.canForm();
    }
    
    @Override
    public void formMultiblockWithPlacement(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 使用 MultiblockFormation 来填充缺失的方块
        MultiblockFormation formation = new MultiblockFormation(level, pos, config);
        formation.fillMissingBlocks(
            getBlockType(),
            (blockEntity, isController) -> {
                // 设置新方块的控制器标志
                if (blockEntity instanceof IMultiblockComponent component) {
                    component.setController(isController);
                }
            }
        );
    }
    
    @Override
    public void breakMultiblock(Level level, BlockPos pos, @Nullable MultiblockData multiblockData) {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 使用 MultiblockFormation 来破坏结构
        MultiblockFormation.breakMultiblock(level, pos, multiblockData);
    }
    
    /**
     * 获取对应的方块类型
     * 子类需要实现此方法以指定具体的方块
     */
    protected net.minecraft.world.level.block.Block getBlockType() {
        throw new UnsupportedOperationException("子类必须实现 getBlockType() 方法");
    }
}
