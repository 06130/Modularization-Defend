package org.lingZero.modularization_defend.Blocks.AgreementCore;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lingZero.modularization_defend.Blocks.Multiblock.IMultiblockConfig;
import org.lingZero.modularization_defend.Register.ModBlocks;

import java.util.HashSet;
import java.util.Set;

/**
 * 协议核心多方块配置
 * 定义 5x5x1 底座 + 3x3x12 柱体的结构
 * 
 * 结构说明：
 * - 底座：5x5x1，中心在 (0,0,0)
 * - 柱体：3x3x12，从 y=1 到 y=12，中心对齐底座
 */
public class AgreementCoreConfig implements IMultiblockConfig {
    
    // 底座尺寸
    public static final int BASE_SIZE_X = 5;
    public static final int BASE_SIZE_Y = 1;
    public static final int BASE_SIZE_Z = 5;
    
    // 柱体尺寸
    public static final int COLUMN_SIZE_X = 3;
    public static final int COLUMN_SIZE_Y = 12;
    public static final int COLUMN_SIZE_Z = 3;
    
    // 总高度
    public static final int TOTAL_HEIGHT = BASE_SIZE_Y + COLUMN_SIZE_Y; // 13
    
    @Override
    public String getId() {
        return "agreement_core";
    }
    
    @Override
    public boolean canForm(LevelAccessor level, BlockPos controllerPos) {
        // 检查底座区域 (5x5x1)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos checkPos = controllerPos.offset(x, 0, z);
                
                // 检查该位置是否为空或可替换
                if (!level.isEmptyBlock(checkPos)) {
                    BlockState state = level.getBlockState(checkPos);
                    if (!state.canBeReplaced()) {
                        return false; // 有阻挡方块
                    }
                }
            }
        }
        
        // 检查柱体区域 (3x3x12)，从 y=1 到 y=12
        for (int y = 1; y <= 12; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
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
        // 放置底座 (5x5x1)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos checkPos = controllerPos.offset(x, 0, z);
                
                // 是否是控制器位置（底座中央）
                boolean isController = (x == 0 && z == 0);
                
                // 放置方块（使用 UPDATE_ALL 确保所有更新都执行）
                BlockState stateToPlace = ModBlocks.AGREEMENT_CORE_BLOCK.get().defaultBlockState();
                level.setBlock(checkPos, stateToPlace, Block.UPDATE_ALL); // flags=3
                
                // 设置控制器标志和主方块坐标
                BlockEntity blockEntity = level.getBlockEntity(checkPos);
                if (blockEntity instanceof AgreementCoreBlockEntity core) {
                    core.setController(isController);
                    core.setControllerPosFromNBT(controllerPos);
                }
            }
        }
        
        // 放置柱体 (3x3x12)，从 y=1 到 y=12
        for (int y = 1; y <= 12; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos checkPos = controllerPos.offset(x, y, z);
                    
                    // 放置方块
                    BlockState stateToPlace = ModBlocks.AGREEMENT_CORE_BLOCK.get().defaultBlockState();
                    level.setBlock(checkPos, stateToPlace, Block.UPDATE_ALL); // flags=3
                    
                    // 设置控制器标志（都不是控制器）和主方块坐标
                    BlockEntity blockEntity = level.getBlockEntity(checkPos);
                    if (blockEntity instanceof AgreementCoreBlockEntity core) {
                        core.setController(false);
                        core.setControllerPosFromNBT(controllerPos);
                    }
                }
            }
        }
        
        // 通知控制器更新多方块数据
        BlockEntity controllerBE = level.getBlockEntity(controllerPos);
        if (controllerBE instanceof AgreementCoreBlockEntity core) {
            // 初始化多方块数据并验证
            core.initializeMultiblock();
        }
    }
    
    @Override
    public Set<BlockPos> getComponentPositions(BlockPos controllerPos) {
        Set<BlockPos> positions = new HashSet<>();
        
        // 添加底座位置 (5x5x1)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                positions.add(controllerPos.offset(x, 0, z));
            }
        }
        
        // 添加柱体位置 (3x3x12)，从 y=1 到 y=12
        for (int y = 1; y <= 12; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    positions.add(controllerPos.offset(x, y, z));
                }
            }
        }
        
        return positions;
    }
}
