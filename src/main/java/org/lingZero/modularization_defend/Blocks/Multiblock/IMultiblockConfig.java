package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.Set;

/**
 * 多方块配置接口
 * 定义多方块的基本属性和行为
 */
public interface IMultiblockConfig {
    /**
     * 获取多方块的唯一标识符
     */
    String getId();
    
    /**
     * 检查指定位置是否可以形成多方块
     * 
     * @param level 世界访问器
     * @param controllerPos 控制器位置
     * @return 如果可以形成返回 true
     */
    boolean canForm(LevelAccessor level, BlockPos controllerPos);
    
    /**
     * 放置多方块结构
     * 
     * @param level 世界
     * @param controllerPos 控制器位置
     */
    void place(Level level, BlockPos controllerPos);
    
    /**
     * 获取多方块的所有组成方块位置
     * 
     * @param controllerPos 控制器位置
     * @return 包含所有方块位置的集合
     * @implNote 该方法用于多方块破坏时获取所有需要移除的方块位置
     */
    @SuppressWarnings("unused")
    Set<BlockPos> getComponentPositions(BlockPos controllerPos);
}
