package org.lingZero.modularization_defend.Blocks.AgreementCore;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
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
 * 协议核心多方块方块
 * 负责处理方块的放置、破坏等事件
 */
public class AgreementCoreMultiblock extends Block implements EntityBlock {
    
    public AgreementCoreMultiblock(BlockBehaviour.Properties properties) {
        super(properties);
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AgreementCoreBlockEntity(pos, state);
    }
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        // 不在这里设置控制器标志，由 MultiblockPlacer 统一处理
        super.onPlace(state, level, pos, oldState, isMoving);
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // 当方块被破坏时，通知 BlockEntity 破坏多方块结构
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AgreementCoreBlockEntity core) {
                // 先调用 blockRemoved 标记需要重新检查
                core.blockRemoved();
                // 然后破坏多方块
                core.breakMultiblock();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
    
    public void neighborChanged(BlockState state, Level level, BlockPos pos, BlockState neighborState, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborState.getBlock(), neighborPos, isMoving);
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AgreementCoreBlockEntity core) {
                core.onNeighborChange(neighborPos);
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
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AgreementCoreBlockEntity core) {
                // 调用 BlockEntity 的 use 方法，让它自己判断是否重定向到主方块
                return core.use(player, InteractionHand.MAIN_HAND);
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
    
    /**
     * 获取方块的着色颜色
     * 非控制器方块将变得透明
     */
    public float getShadeBrightness(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (level instanceof Level actualLevel) {
            BlockEntity blockEntity = actualLevel.getBlockEntity(pos);
            if (blockEntity instanceof AgreementCoreBlockEntity core) {
                // 如果不是控制器，增加亮度使其看起来更透明
                if (!core.isController()) {
                    return 1.0f; // 最大亮度，使其看起来更淡
                }
            }
        }
        return super.getShadeBrightness(state, level, pos);
    }
}
