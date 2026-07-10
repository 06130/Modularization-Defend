package org.lingZero.modularization_defend.Block.example;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.Block.ModBlockEntities;
import org.lingZero.modularization_defend.Block.bounding.BoundingHelper;

/**
 * 示例多方块结构的主方块。
 * <p>
 * 放置时自动在偏移位置生成占位方块，破坏时自动清理。
 * 结构为一个竖直柱体：主方块上方 3 个占位方块（共4格高）。
 * <p>
 * 右键点击（主方块或任意占位方块）可查看 tick 计数。
 */
public class ExampleMultiblockBlock extends Block implements EntityBlock {

    /**
     * 占位方块相对于主方块的偏移坐标。
     * 此处定义了一个 3 格高的竖直柱体。
     */
    private static final BlockPos[] BOUNDING_OFFSETS = {
            new BlockPos(0, 1, 0),
            new BlockPos(0, 2, 0),
            new BlockPos(0, 3, 0),
    };

    /**
     * 覆盖整个多方块结构的碰撞箱（从主方块底部到最高占位方块顶部）。
     * box(3, 0, 3, 13, 64, 13) = 10×10 宽的立柱，4 格高（64 体素 = 4×16）。
     */
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 64, 13);

    public ExampleMultiblockBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.5F, 6.0F)
                .requiresCorrectToolForDrops());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExampleMultiblockBlockEntity(pos, state);
    }

    /**
     * 放置前预检：若占位区域内有障碍物，则阻止方块放置。
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos placePos = context.getClickedPos();
        if (!BoundingHelper.canPlaceBoundingBlocks(context.getLevel(), placePos, BOUNDING_OFFSETS)) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (type == ModBlockEntities.EXAMPLE_MULTIBLOCK.get()) {
            @SuppressWarnings("unchecked")
            BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) (level1, pos1, state1, be) ->
                    ExampleMultiblockBlockEntity.serverTick(level1, pos1, state1, (ExampleMultiblockBlockEntity) be);
            return ticker;
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            if (!BoundingHelper.placeBoundingBlocks(level, pos, BOUNDING_OFFSETS)) {
                // 防御性处理：若占位方块放置失败（例如被命令/生成绕过预检），
                // 移除主方块并掉落物品，避免产生不完整的多方块结构
                level.removeBlock(pos, false);
                if (placer instanceof Player player) {
                    if (!player.getAbilities().instabuild) {
                        popResource(level, pos, stack.copy());
                    }
                } else {
                    popResource(level, pos, stack.copy());
                }
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BoundingHelper.removeBoundingBlocks(level, pos, BOUNDING_OFFSETS);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ExampleMultiblockBlockEntity be) {
            player.sendSystemMessage(Component.literal(
                    "[ExampleMultiblock] Tick count: " + be.getTickCount()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // ==================== 碰撞箱（覆盖整个多方块结构） ====================

    @NotNull
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @NotNull
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @NotNull
    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @NotNull
    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }
}
