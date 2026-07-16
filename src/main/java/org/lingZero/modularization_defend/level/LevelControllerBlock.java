package org.lingZero.modularization_defend.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.Block.ModBlockEntities;
import org.lingZero.modularization_defend.Item.ProgramStorageCardItem;
import org.lingZero.modularization_defend.nodegraph.GraphEditorOpener;

/**
 * 关卡控制器方块——关卡流程控制的载体。
 *
 * <h3>交互</h3>
 * <ul>
 *   <li><b>潜行 + 空手右键</b> — 打开节点图编辑器直接编辑方块上的图</li>
 *   <li><b>空手右键</b> — 启动关卡（IDLE）/ 复位关卡（RUNNING、终态）</li>
 *   <li><b>手持程序存储卡右键</b> — 将卡上的图导入方块</li>
 * </ul>
 */
public class LevelControllerBlock extends Block implements EntityBlock {

    public LevelControllerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(3.5F, 3600000.0F)
                .noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LevelControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        if (type == ModBlockEntities.LEVEL_CONTROLLER.get()) {
            @SuppressWarnings("unchecked")
            BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) (level1, pos1, state1, be) ->
                    LevelControllerBlockEntity.serverTick(level1, pos1, state1, (LevelControllerBlockEntity) be);
            return ticker;
        }
        return null;
    }

    // ==================== 交互 ====================

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof ProgramStorageCardItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LevelControllerBlockEntity be) {
            if (be.importGraphFromCard(stack)) {
                player.sendSystemMessage(Component.translatable(
                        "block.modularization_defend.level_controller.graph_imported"));
            } else {
                player.sendSystemMessage(Component.translatable(
                        "block.modularization_defend.level_controller.card_empty"));
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            // 潜行右键：客户端打开图编辑器（图数据经 BE 更新包同步到客户端）
            if (level.isClientSide) {
                openEditorOnClient(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LevelControllerBlockEntity be) {
            be.toggleLevel(player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** 仅在客户端执行，避免服务端加载客户端类 */
    private static void openEditorOnClient(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof LevelControllerBlockEntity be) {
            GraphEditorOpener.openControllerEditor(be.getGraphTag().copy(), pos);
        }
    }
}
