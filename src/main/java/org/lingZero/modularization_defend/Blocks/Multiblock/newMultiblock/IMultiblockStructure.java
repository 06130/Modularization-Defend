package org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 多方块结构信息接口
 * 定义多方块结构的基本信息和解析方法
 */
public interface IMultiblockStructure {
    
    /**
     * 获取结构的名称
     */
    @NotNull
    String getName();
    
    /**
     * 获取结构的宽度 (X 轴)
     */
    int getWidth();
    
    /**
     * 获取结构的高度 (Y 轴)
     */
    int getHeight();
    
    /**
     * 获取结构的深度 (Z 轴)
     */
    int getDepth();
    
    /**
     * 获取主方块的位置（相对于结构原点）
     */
    BlockPos getMasterPosition();
    
    /**
     * 检查某个位置是否为主方块
     * @param pos 相对于结构原点的位置
     * @return 是否为主方块
     */
    boolean isMaster(BlockPos pos);
    
    /**
     * 检查某个位置是否为结构方块
     * @param pos 相对于结构原点的位置
     * @return 是否为结构方块
     */
    boolean isPartBlock(BlockPos pos);
    
    /**
     * 获取所有结构方块的位置列表（相对于结构原点）
     */
    Set<BlockPos> getPartPositions();
    
    /**
     * 验证世界中指定位置是否可以形成此多方块结构
     * @param level 世界
     * @param origin 结构原点（世界坐标）
     * @return 是否可以形成
     */
    boolean canForm(Level level, BlockPos origin);
    
    /**
     * 获取结构的层信息
     * @return 层信息映射表，key 为 Y 坐标（相对值），value 为该层的布局
     */
    Map<Integer, List<String>> getLayers();
}
