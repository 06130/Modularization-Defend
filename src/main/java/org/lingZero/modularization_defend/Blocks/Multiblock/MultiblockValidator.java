package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 多方块结构验证器
 * 用于验证和检测多方块结构
 */
public class MultiblockValidator {
    
    private final Level level;
    private final int width;
    private final int height;
    private final int depth;

    public MultiblockValidator(Level level, int width, int height, int depth) {
        this.level = level;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**
     * 验证指定位置是否可以形成多方块结构
     * @param controllerPos 控制器位置（作为基准点）
     * @return 如果结构有效返回 true
     */
    public boolean validate(BlockPos controllerPos) {
        if (level == null || controllerPos == null) {
            return false;
        }

        // 检查 2x2x6 的长方体空间
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    BlockPos checkPos = controllerPos.offset(x, y, z);
                    
                    if (!isValidPosition(checkPos)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 检查单个位置是否有效
     */
    protected boolean isValidPosition(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        
        // 检查方块是否为空气或液体
        if (state.isAir() || state.getFluidState().isEmpty()) {
            return false;
        }

        // 可以在这里添加更多验证逻辑
        // 例如检查特定类型的方块
        
        return true;
    }

    /**
     * 获取结构的宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * 获取结构的高度
     */
    public int getHeight() {
        return height;
    }

    /**
     * 获取结构的深度
     */
    public int getDepth() {
        return depth;
    }

    /**
     * 获取世界对象
     */
    @Nullable
    public Level getLevel() {
        return level;
    }
}
