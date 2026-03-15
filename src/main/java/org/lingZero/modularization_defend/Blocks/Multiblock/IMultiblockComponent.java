package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 多方块组件接口
 * 表示一个多方块结构的基本组件
 */
public interface IMultiblockComponent {
    
    /**
     * 获取多方块数据
     */
    @Nullable
    MultiblockData getMultiblockData();
    
    /**
     * 设置多方块数据
     */
    void setMultiblockData(@Nullable MultiblockData data);
    
    /**
     * 检查是否为控制器
     */
    boolean isController();
    
    /**
     * 设置是否为控制器
     */
    void setController(boolean controller);
    
    /**
     * 初始化多方块数据（由 BlockEntity 实现）
     */
    void initializeMultiblock();
}
