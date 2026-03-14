package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 多方块形状构建器
 * 用于定义任意形状的多方块结构（支持非矩形）
 */
public class MultiblockShapeBuilder {
    
    private final Set<BlockPos> relativePositions = new HashSet<>();
    private BlockPos controllerOffset = BlockPos.ZERO;
    
    /**
     * 添加一个相对位置到形状中
     * 
     * @param x X 轴偏移
     * @param y Y 轴偏移
     * @param z Z 轴偏移
     * @return 构建器自身
     */
    public MultiblockShapeBuilder addBlock(int x, int y, int z) {
        relativePositions.add(new BlockPos(x, y, z));
        return this;
    }
    
    /**
     * 设置控制器方块的相对位置
     * 
     * @param x X 轴偏移
     * @param y Y 轴偏移
     * @param z Z 轴偏移
     * @return 构建器自身
     */
    public MultiblockShapeBuilder setControllerPosition(int x, int y, int z) {
        this.controllerOffset = new BlockPos(x, y, z);
        return this;
    }
    
    /**
     * 填充一个矩形区域
     * 
     * @param minX X 最小值
     * @param minY Y 最小值
     * @param minZ Z 最小值
     * @param maxX X 最大值
     * @param maxY Y 最大值
     * @param maxZ Z 最大值
     * @return 构建器自身
     */
    public MultiblockShapeBuilder fill(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    addBlock(x, y, z);
                }
            }
        }
        return this;
    }
    
    /**
     * 检查某个位置是否在形状范围内
     * 
     * @param level 世界访问器
     * @param controllerPos 控制器位置
     * @return 如果可以形成返回 true
     */
    public boolean canForm(LevelAccessor level, BlockPos controllerPos) {
        for (BlockPos relativePos : relativePositions) {
            BlockPos checkPos = controllerPos.offset(relativePos.getX(), relativePos.getY(), relativePos.getZ());
            
            if (!level.isEmptyBlock(checkPos)) {
                if (!level.getBlockState(checkPos).canBeReplaced()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 获取所有方块的绝对位置
     * 
     * @param controllerPos 控制器位置
     * @return 包含所有方块位置的集合
     */
    public Set<BlockPos> getAbsolutePositions(BlockPos controllerPos) {
        Set<BlockPos> absolutePositions = new HashSet<>();
        
        for (BlockPos relativePos : relativePositions) {
            BlockPos absolutePos = controllerPos.offset(relativePos.getX(), relativePos.getY(), relativePos.getZ());
            absolutePositions.add(absolutePos);
        }
        
        return absolutePositions;
    }
    
    /**
     * 获取控制器的相对位置
     */
    public BlockPos getControllerOffset() {
        return controllerOffset;
    }
    
    /**
     * 获取所有相对位置
     */
    public Set<BlockPos> getRelativePositions() {
        return Collections.unmodifiableSet(relativePositions);
    }
    
    /**
     * 获取形状的边界框大小
     */
    public BoxSize getBoundingBoxSize() {
        if (relativePositions.isEmpty()) {
            return new BoxSize(0, 0, 0);
        }
        
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        
        for (BlockPos pos : relativePositions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        
        return new BoxSize(
            maxX - minX + 1,
            maxY - minY + 1,
            maxZ - minZ + 1
        );
    }
    
    /**
     * 边界框尺寸
     */
    public record BoxSize(int sizeX, int sizeY, int sizeZ) {}
}
