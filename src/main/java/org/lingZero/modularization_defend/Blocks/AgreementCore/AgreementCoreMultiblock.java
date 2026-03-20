package org.lingZero.modularization_defend.Blocks.AgreementCore;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.lingZero.modularization_defend.util.DebugLogger;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * 协议核心多方块方块
 * 负责处理方块的放置、破坏等事件
 * 使用新的 Multiblock 框架实现
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AgreementCoreMultiblock extends Block implements EntityBlock {
    
    public AgreementCoreMultiblock(BlockBehaviour.Properties properties) {
        super(properties);
        DebugLogger.info("AgreementCoreMultiblock 构造函数被调用！");
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DebugLogger.debug("创建 BlockEntity: " + pos + ", " + state);
        return new AgreementCoreBlockEntity(pos, state);
    }
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        // 注意：实际的放置由 MultiblockPlacer 在玩家右键时处理
        // onPlace 只用于验证和更新方块状态
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // 注意：多方块破坏由 MultiblockEvents.onBlockBreak 事件处理
        // 这里只处理原版的方块移除逻辑
        super.onRemove(state, level, pos, newState, isMoving);
    }
    
    /**
     * 当玩家手持物品右键点击方块时调用
     * 注意：多方块放置由 MultiblockEvents 处理，这里只处理已成型后的 GUI 打开
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        DebugLogger.debug("===== AgreementCoreMultiblock.useItemOn 被调用 =====");
        
        // 在客户端直接返回
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        
        // 如果已经成型且是主方块，打开 GUI
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AgreementCoreBlockEntity core) {
            if (core.isMultiblockFormed() && core.isController()) {
                DebugLogger.info("打开 GUI");
                core.use(player, hand);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        
        // 否则传递给默认处理
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    
    /**
     * 当玩家空手右键点击方块时调用（用于打开 GUI）
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        DebugLogger.info("===== AgreementCoreMultiblock.useWithoutItem 被调用 =====");
        DebugLogger.info("客户端：" + level.isClientSide + ", 位置：" + pos + ", 玩家：" + player.getName().getString());
        
        if (!level.isClientSide) {
            // 尝试打开 GUI（如果已经成型）
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AgreementCoreBlockEntity core) {
                DebugLogger.debug("方块实体存在，isFormed: " + core.isMultiblockFormed() + ", isController: " + core.isController());
                if (core.isMultiblockFormed() && core.isController()) {
                    // 已成型，打开 GUI
                    DebugLogger.info("打开 GUI");
                    return core.use(player, InteractionHand.MAIN_HAND);
                }
            }
        }
        
        DebugLogger.debug("返回 SUCCESS");
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
