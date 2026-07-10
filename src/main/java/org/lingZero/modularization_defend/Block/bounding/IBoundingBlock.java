package org.lingZero.modularization_defend.Block.bounding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

/**
 * 由多方块结构"主方块"的BlockEntity实现的接口。
 * 占位方块(BoundingBlockEntity)通过此接口将交互代理到主方块。
 */
public interface IBoundingBlock {

    /**
     * 当占位方块接收到红石信号变化时调用。
     */
    default void onBoundingBlockPowerChange(BlockPos boundingPos, int oldLevel, int newLevel) {
    }

    /**
     * 获取占位方块位置的比较器信号。
     */
    default int getBoundingComparatorSignal(Vec3i offset) {
        return 0;
    }
}
