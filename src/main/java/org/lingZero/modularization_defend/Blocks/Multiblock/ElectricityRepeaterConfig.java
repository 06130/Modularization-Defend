package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lingZero.modularization_defend.Register.ModBlocks;
import org.lingZero.modularization_defend.Blocks.ElectricityRepeater.ElectricityRepeaterBlockEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * 电力中继器多方块配置
 * 定义 2x2x10 的标准矩形结构
 */
public class ElectricityRepeaterConfig implements IMultiblockConfig {
    
    // 多方块尺寸
    public static final int SIZE_X = 2;
    public static final int SIZE_Y = 10;
    public static final int SIZE_Z = 2;
    
    @Override
    public String getId() {
        return "electricity_repeater";
    }
    
    @Override
    public boolean canForm(LevelAccessor level, BlockPos controllerPos) {
        // 遍历所有位置
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
    
    @Override
    public void place(Level level, BlockPos controllerPos) {
        // 遍历所有位置（包括控制器位置）
        for (int y = 0; y < SIZE_Y; y++) {
            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    BlockPos checkPos = controllerPos.offset(x, y, z);
                    
                    // 放置方块（使用 UPDATE_ALL 确保客户端同步）
                    BlockState stateToPlace = ModBlocks.ELECTRICITY_REPEATER_BLOCK.get().defaultBlockState();
                    level.setBlock(checkPos, stateToPlace, Block.UPDATE_ALL);
                    
                    // 设置控制器标志
                    BlockEntity blockEntity = level.getBlockEntity(checkPos);
                    if (blockEntity instanceof ElectricityRepeaterBlockEntity repeater) {
                        // 第一个位置是控制器，其余不是
                        boolean isController = (x == 0 && y == 0 && z == 0);
                        repeater.setController(isController);
                    }
                }
            }
        }
        
        // 通知控制器更新多方块数据
        BlockEntity controllerBE = level.getBlockEntity(controllerPos);
        if (controllerBE instanceof ElectricityRepeaterBlockEntity repeater) {
            // 初始化多方块数据并验证
            repeater.initializeMultiblock();
        }
    }
    
    @Override
    public Set<BlockPos> getComponentPositions(BlockPos controllerPos) {
        Set<BlockPos> positions = new HashSet<>();
        
        // 遍历 2x2x6 的所有位置
        for (int y = 0; y < SIZE_Y; y++) {
            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {
                    BlockPos checkPos = controllerPos.offset(x, y, z);
                    positions.add(checkPos);
                }
            }
        }
        
        return positions;
    }
}
