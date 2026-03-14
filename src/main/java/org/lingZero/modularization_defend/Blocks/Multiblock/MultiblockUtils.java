package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 多方块工具类
 * 提供多方块检测和验证的辅助方法
 */
public class MultiblockUtils {
    
    /**
     * 检查指定位置周围是否可以形成 2x2x6 的多方块结构
     * @param level 世界
     * @param pos 基准位置（通常是控制器位置）
     * @return 如果结构有效返回 true
     */
    public static boolean check2x2x6Multiblock(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        
        // 检查 2x2x6 的空间
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    
                    // 检查是否为有效的多方块方块
                    if (!isValidMultiblockBlock(state)) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * 获取 2x2x6 结构中所有方块的位置
     * @param level 世界
     * @param pos 基准位置
     * @return 所有方块位置的列表
     */
    public static List<BlockPos> get2x2x6BlockPositions(Level level, BlockPos pos) {
        List<BlockPos> positions = new ArrayList<>();
        
        if (level == null || pos == null) {
            return positions;
        }
        
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    positions.add(pos.offset(x, y, z));
                }
            }
        }
        
        return positions;
    }
    
    /**
     * 检查方块是否为有效的多方块组成部分
     * 这里可以根据需要扩展更多的验证逻辑
     */
    public static boolean isValidMultiblockBlock(BlockState state) {
        // 目前简单检查方块是否为空气或液体
        if (state.isAir()) {
            return false;
        }
        
        // 可以在这里添加更多特定的检查
        // 例如：只允许特定类型的方块作为多方块的一部分
        
        return true;
    }
    
    /**
     * 查找最近的控制器位置
     * @param level 世界
     * @param pos 搜索起点
     * @param maxRange 最大搜索范围
     * @return 找到的控制器位置，如果没有则返回 null
     */
    @Nullable
    public static BlockPos findNearestController(Level level, BlockPos pos, int maxRange) {
        if (level == null || pos == null) {
            return null;
        }
        
        // 在范围内搜索
        for (int dx = -maxRange; dx <= maxRange; dx++) {
            for (int dy = -maxRange; dy <= maxRange; dy++) {
                for (int dz = -maxRange; dz <= maxRange; dz++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    // 这里可以检查该位置是否有控制器 BlockEntity
                    // 目前简化处理
                }
            }
        }
        
        return null;
    }
    
    /**
     * 计算结构的体积
     */
    public static int calculateVolume(int width, int height, int depth) {
        return width * height * depth;
    }
    
    /**
     * 获取 2x2x6 结构的体积
     */
    public static int get2x2x6Volume() {
        return calculateVolume(2, 6, 2); // 24 个方块
    }
}
