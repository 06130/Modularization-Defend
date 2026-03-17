package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * 多方块结构数据类
 * 用于存储和管理多方块结构的信息
 */
public class MultiblockData {
    protected final Level level;
    protected final BlockPos controllerPos;
    protected final Set<BlockPos> blocks;
    private boolean isFormed = false;
    /**
     * 标记是否需要重新检查结构（Mekanism 风格）
     */
    public boolean needsRecheck = false;

    public MultiblockData(Level level, BlockPos controllerPos) {
        this.level = level;
        this.controllerPos = controllerPos;
        this.blocks = new HashSet<>();
    }

    /**
     * 尝试形成多方块结构
     */
    public boolean tryForm() {
        if (level == null || controllerPos == null) {
            return false;
        }

        blocks.clear();
        
        // 遍历所有预期的结构位置（由具体的多方块类型决定）
        // 注意：这里不应该硬编码尺寸，应该由外部传入或使用配置
        // 但由于 Mekanism 的 MultiblockData 设计，我们暂时保持这个模式
        // 实际使用时应该在子类中覆盖此方法
        for (int y = 0; y < 13; y++) { // 协议核心高度：1 (底座) + 12 (柱体)
            for (int x = -2; x <= 2; x++) { // 协议核心底座宽度
                for (int z = -2; z <= 2; z++) {
                    BlockPos checkPos = controllerPos.offset(x, y, z);
                    
                    // 检查该位置是否在结构范围内
                    if (!isPositionInStructure(x, y, z)) {
                        continue;
                    }
                    
                    BlockState state = level.getBlockState(checkPos);
                    
                    // 检查该位置是否为有效的多方块方块
                    if (MultiblockUtils.isValidMultiblockBlock(state)) {
                        // 验证该位置的 BlockEntity 是否正确设置了 isController
                        var blockEntity = level.getBlockEntity(checkPos);
                        if (blockEntity instanceof IMultiblockComponent component) {
                            // 确保控制器标志正确
                            boolean shouldBeController = checkPos.equals(controllerPos);
                            if (component.isControllerViaComponent() != shouldBeController) {
                                component.setController(shouldBeController);
                            }
                        }
                        blocks.add(checkPos);
                    } else {
                        // 如果任何位置无效，结构不完整
                        // 这可能是因为重启后部分方块被破坏或替换
                        blocks.clear();
                        isFormed = false;
                        // 从管理器中移除无效的记录
                        MultiblockManager.removeMultiblock(controllerPos);
                        return false;
                    }
                }
            }
        }

        isFormed = !blocks.isEmpty();
        
        // 如果成型成功，注册到管理器
        if (isFormed) {
            MultiblockManager.registerMultiblock(this);
        }
        
        return isFormed;
    }
    
    /**
     * 检查给定坐标是否在结构范围内
     * @param x 相对 X 坐标
     * @param y 相对 Y 坐标
     * @param z 相对 Z 坐标
     * @return 如果在结构内返回 true
     * @implNote 默认实现适用于协议核心，子类可以覆盖以支持不同形状
     */
    protected boolean isPositionInStructure(int x, int y, int z) {
        if (y == 0) {
            // 底座层 (5x5x1)
            return Math.abs(x) <= 2 && Math.abs(z) <= 2;
        } else if (y >= 1 && y <= 12) {
            // 柱体层 (3x3x12)
            return Math.abs(x) <= 1 && Math.abs(z) <= 1;
        }
        return false;
    }

    /**
     * 检查方块是否为有效的多方块组成部分
     */
    protected boolean isValidMultiblockBlock(BlockState state) {
        // 这里需要检查方块是否属于这个多方块结构
        // 目前简单返回 true，实际使用时需要根据具体方块类型判断
        return true;
    }

    /**
     * 破坏多方块结构
     */
    public void breakMultiblock() {
        isFormed = false;
        blocks.clear();
        // 从管理器中移除
        MultiblockManager.removeMultiblock(controllerPos);
    }
    
    /**
     * 设置需要重新检查结构
     */
    public void setNeedsRecheck() {
        needsRecheck = true;
    }
    
    /**
     * 检查是否需要重新验证结构
     */
    public boolean needsRecheck() {
        return needsRecheck;
    }

    /**
     * 获取所有组成方块的位置
     */
    @NotNull
    public Set<BlockPos> getBlocks() {
        return new HashSet<>(blocks);
    }

    /**
     * 检查多方块是否已成型
     */
    public boolean isFormed() {
        return isFormed;
    }

    /**
     * 获取控制器的位置
     */
    @Nullable
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    /**
     * 检查某个位置是否属于这个多方块结构
     */
    public boolean containsBlock(BlockPos pos) {
        return blocks.contains(pos);
    }

    /**
     * 获取组成方块的数量
     */
    public int getBlockCount() {
        return blocks.size();
    }
}
