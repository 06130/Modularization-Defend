package org.lingZero.modularization_defend.Blocks.ElectricityRepeater;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import static org.lingZero.modularization_defend.Register.ModBlockEntities.Electricity_Repeater_BLOCK_ENTITY;

/**
 * 电力中继器多方块方块
 * 负责处理方块的放置、破坏等事件
 */
public class ElectricityRepeaterMultiblock extends Block implements EntityBlock {
    
    public ElectricityRepeaterMultiblock(BlockBehaviour.Properties properties) {
        super(properties);
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricityRepeaterBlockEntity(pos, state);
    }
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        // 方块被放置后，标记为控制器
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ElectricityRepeaterBlockEntity repeater) {
                // 确保第一个放置的是控制器
                repeater.setController(true);
            }
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // 当方块被破坏时，通知 BlockEntity 破坏多方块结构
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ElectricityRepeaterBlockEntity repeater) {
                repeater.breakMultiblock();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 简化实现，由 BlockEntity 自己处理 tick
        if (level.isClientSide) {
            return null;
        }
        return new BlockEntityTicker<>() {
            @Override
            public void tick(Level pLevel, BlockPos pPos, BlockState pState, T pBlockEntity) {
                // 空实现，tick 逻辑在 BlockEntity 内部处理
            }
        };
    }
}
