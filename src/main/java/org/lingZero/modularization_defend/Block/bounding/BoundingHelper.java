package org.lingZero.modularization_defend.Block.bounding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.Block.ModBlocks;

/**
 * 占位方块的放置与清理工具类。
 * <p>
 * 用法：在主方块的 setPlacedBy / onRemove 中调用对应静态方法即可。
 */
public class BoundingHelper {

    private BoundingHelper() {
    }

    /**
     * 检查所有偏移位置是否为空（无方块阻挡），供放置前的预检使用。
     *
     * @param level   当前世界
     * @param mainPos 主方块坐标
     * @param offsets 相对于主方块的偏移坐标数组
     * @return 全部位置均为空气时返回 true
     */
    public static boolean canPlaceBoundingBlocks(Level level, BlockPos mainPos, BlockPos[] offsets) {
        for (BlockPos offset : offsets) {
            BlockPos pos = mainPos.offset(offset);
            if (!level.isEmptyBlock(pos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 在 mainPos 的 offset 偏移位置放置占位方块，并设置其主方块引用。
     * 放置前会先检查所有位置是否为空。
     *
     * @param level   当前世界
     * @param mainPos 主方块坐标
     * @param offsets 相对于主方块的偏移坐标数组
     * @return 全部占位方块放置成功时返回 true，若任一位置被阻挡则返回 false（放一个都不放）
     */
    public static boolean placeBoundingBlocks(Level level, BlockPos mainPos, BlockPos[] offsets) {
        if (!canPlaceBoundingBlocks(level, mainPos, offsets)) {
            return false;
        }
        BlockState boundingState = ModBlocks.BOUNDING_BLOCK.get().defaultBlockState();
        for (BlockPos offset : offsets) {
            BlockPos boundingPos = mainPos.offset(offset);
            level.setBlock(boundingPos, boundingState, 3);
            if (level.getBlockEntity(boundingPos) instanceof BoundingBlockEntity be) {
                be.setMainPosition(mainPos);
            }
        }
        return true;
    }

    /**
     * 移除主方块关联的所有占位方块。
     *
     * @param level   当前世界
     * @param mainPos 主方块坐标
     * @param offsets 相对于主方块的偏移坐标数组
     */
    public static void removeBoundingBlocks(Level level, BlockPos mainPos, BlockPos[] offsets) {
        for (BlockPos offset : offsets) {
            BlockPos boundingPos = mainPos.offset(offset);
            BlockState state = level.getBlockState(boundingPos);
            if (state.is(ModBlocks.BOUNDING_BLOCK.get())) {
                level.removeBlockEntity(boundingPos);
                level.removeBlock(boundingPos, false);
            }
        }
    }
}
