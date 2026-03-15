package org.lingZero.modularization_defend.Blocks.EntityBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.Blocks.BlockEntity.ElectricityRepeaterBlockEntity;

import javax.annotation.Nullable;

import static org.lingZero.modularization_defend.Register.ModBlockEntities.Electricity_Repeater_BLOCK_ENTITY;

public class ElectricityRepeaterEntityBlock extends Block implements EntityBlock {
    // 构造函数，委托给父类
    public ElectricityRepeaterEntityBlock(BlockBehaviour.Properties properties) {
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
        return level.isClientSide ? null : createTickerHelper(type, Electricity_Repeater_BLOCK_ENTITY.get(), ElectricityRepeaterBlockEntity::tick);
    }
    
    private static <E extends BlockEntity, A extends BlockEntity> @Nullable BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> type, BlockEntityType<E> checkedType, BlockEntityTicker<? super E> ticker
    ) {
        return checkedType == type ? (BlockEntityTicker<A>) ticker : null;
    }
}