package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lingZero.modularization_defend.Register.ModBlocks;

/**
 * 多方块结构放置工具类
 * 负责处理多方块的检测、放置和验证逻辑
 */
public class MultiblockPlacer {
    
    /**
     * 定义多方块结构的尺寸
     */
    public static final int SIZE_X = 2;
    public static final int SIZE_Y = 6;
    public static final int SIZE_Z = 2;
    
    /**
     * 检查指定位置是否可以形成多方块结构
     * 
     * @param level 世界访问器
     * @param controllerPos 控制器位置（多方块的起始点）
     * @return 如果可以形成返回 true，否则返回 false
     */
    public static boolean canFormMultiblock(net.minecraft.world.level.LevelAccessor level, BlockPos controllerPos) {
        // 遍历 2x2x6 的所有位置
        for (int y = 0; y < SIZE_Y; y++) {
            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    BlockPos checkPos = controllerPos.offset(x, y, z);
                    
                    // 检查该位置是否为空或可替换
                    if (!level.isEmptyBlock(checkPos)) {
                        BlockState state = level.getBlockState(checkPos);
                        if (!state.canBeReplaced()) {
                            return false; // 有阻挡方块
                        }
                    }
                }
            }
        }
        return true; // 没有阻挡
    }
    
    /**
     * 一次性放置整个多方块结构
     * 
     * @param level 世界
     * @param controllerPos 控制器位置
     */
    public static void placeEntireMultiblock(Level level, BlockPos controllerPos) {
        // 遍历 2x2x6 的所有位置
        for (int y = 0; y < SIZE_Y; y++) {
            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    BlockPos checkPos = controllerPos.offset(x, y, z);
                    
                    // 放置方块（使用 UPDATE_ALL 确保客户端同步）
                    BlockState stateToPlace = ModBlocks.ELECTRICITY_REPEATER_BLOCK.get().defaultBlockState();
                    level.setBlock(checkPos, stateToPlace, Block.UPDATE_ALL);
                    
                    // 设置控制器标志
                    BlockEntity blockEntity = level.getBlockEntity(checkPos);
                    if (blockEntity instanceof org.lingZero.modularization_defend.Blocks.BlockEntity.ElectricityRepeaterBlockEntity repeater) {
                        // 第一个位置是控制器，其余不是
                        repeater.setController(x == 0 && y == 0 && z == 0);
                    }
                }
            }
        }
        
        // 通知控制器更新多方块数据
        BlockEntity controllerBE = level.getBlockEntity(controllerPos);
        if (controllerBE instanceof org.lingZero.modularization_defend.Blocks.BlockEntity.ElectricityRepeaterBlockEntity repeater) {
            // 初始化多方块数据并验证
            repeater.initializeMultiblock();
        }
    }
    
    /**
     * 尝试放置多方块结构
     * 如果放置失败会返还物品
     * 
     * @param level 世界
     * @param controllerPos 控制器位置
     * @param player 玩家
     * @param itemStack 物品堆
     * @return 如果放置成功返回 true，否则返回 false
     */
    public static boolean tryPlaceMultiblock(Level level, BlockPos controllerPos, Player player, ItemStack itemStack) {
        // 检查是否可以形成多方块
        if (!canFormMultiblock(level, controllerPos)) {
            return false;
        }
        
        // 消耗一个物品
        if (!player.isCreative()) {
            itemStack.shrink(1);
        }
        
        // 放置多方块结构
        placeEntireMultiblock(level, controllerPos);
        
        return true;
    }
    
    /**
     * 获取多方块结构的边界框
     * 
     * @param controllerPos 控制器位置
     * @return 包含最小和最大位置的数组 [minPos, maxPos]
     */
    public static BlockPos[] getBounds(BlockPos controllerPos) {
        BlockPos minPos = controllerPos;
        BlockPos maxPos = controllerPos.offset(SIZE_X - 1, SIZE_Y - 1, SIZE_Z - 1);
        return new BlockPos[]{minPos, maxPos};
    }
    
    /**
     * 检查某个位置是否在多方块结构范围内
     * 
     * @param pos 要检查的位置
     * @param controllerPos 控制器位置
     * @return 如果在范围内返回 true，否则返回 false
     */
    public static boolean isInMultiblockBounds(BlockPos pos, BlockPos controllerPos) {
        int dx = pos.getX() - controllerPos.getX();
        int dy = pos.getY() - controllerPos.getY();
        int dz = pos.getZ() - controllerPos.getZ();
        
        return dx >= 0 && dx < SIZE_X &&
               dy >= 0 && dy < SIZE_Y &&
               dz >= 0 && dz < SIZE_Z;
    }
}
