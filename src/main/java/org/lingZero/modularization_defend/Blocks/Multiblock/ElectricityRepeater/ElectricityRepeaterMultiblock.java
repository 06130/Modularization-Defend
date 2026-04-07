package org.lingZero.modularization_defend.Blocks.Multiblock.ElectricityRepeater;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 电力中继器多方块方块
 * 负责处理方块的放置、破坏等事件
 * 使用新的 Multiblock 框架实现
 */
public class ElectricityRepeaterMultiblock extends Block implements EntityBlock {
    
    private static final ElectricityRepeaterMultiblockDef MULTIBLOCK = new ElectricityRepeaterMultiblockDef();
    
    public ElectricityRepeaterMultiblock(BlockBehaviour.Properties properties) {
        super(properties);
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricityRepeaterBlockEntity(pos, state);
    }
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        // 使用框架的 onPlace 方法自动处理多方块放置
        MULTIBLOCK.onPlace(level, pos, state);
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // 当方块被破坏时，使用框架的 onRemove 方法自动处理多方块破坏
        if (!state.is(newState.getBlock())) {
            MULTIBLOCK.onRemove(level, pos, null);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
    
    public void neighborChanged(BlockState state, Level level, BlockPos pos, BlockState neighborState, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborState.getBlock(), neighborPos, isMoving);
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ElectricityRepeaterBlockEntity repeater) {
                repeater.onNeighborChange(neighborPos);
            }
        }
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
    
    /**
     * 当玩家右键点击方块时调用
     * 使用新框架的 GUI 处理
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ElectricityRepeaterBlockEntity repeater) {
                // 调用 BlockEntity 的 use 方法，让它自己判断是否重定向到主方块
                return repeater.use(player, InteractionHand.MAIN_HAND);
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 返回方块的渲染形状
     * 使用 GeckoLib 渲染时需要返回 ENTITYBLOCK_ANIMATED
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
