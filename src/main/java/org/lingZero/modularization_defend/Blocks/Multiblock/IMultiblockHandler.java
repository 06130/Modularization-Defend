package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * 多方块操作处理器接口
 * 定义多方块的基本操作方法
 */
public interface IMultiblockHandler {
    
    /**
     * 尝试形成多方块结构
     * 
     * @param level 世界
     * @param pos 位置
     * @param player 玩家（可能为 null）
     * @return 是否成功
     */
    boolean tryFormMultiblock(Level level, BlockPos pos, @Nullable Player player);
    
    /**
     * 形成多方块结构（包括自动填充缺失的方块）
     * 
     * @param level 世界
     * @param pos 位置
     */
    void formMultiblockWithPlacement(Level level, BlockPos pos);
    
    /**
     * 破坏多方块结构
     * 
     * @param level 世界
     * @param pos 位置
     * @param multiblockData 多方块数据（可能为 null）
     */
    void breakMultiblock(Level level, BlockPos pos, @Nullable MultiblockData multiblockData);
}
