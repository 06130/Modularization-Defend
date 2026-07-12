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
import net.minecraft.world.level.block.RenderShape;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 可部署高台——3x3x3 立方体多方块结构，主方块位于底面中心。
 * 实体无法穿过（有碰撞）。预留 GeckoLib 模型渲染位。
 */
public class DeployableZoneHighBlock extends Block implements EntityBlock {

    private static final BlockPos[] BOUNDING_OFFSETS = generateOffsets();
    private static final VoxelShape SHAPE_CUBE = Block.box(-16, 0, -16, 32, 48, 32);

    private static BlockPos[] generateOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    offsets.add(new BlockPos(x, y, z));
                }
            }
        }
        return offsets.toArray(BlockPos[]::new);
    }

    public DeployableZoneHighBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_GREEN)
                .strength(3.5F, 3600000.0F)
                .noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeployableZoneHighBlockEntity(pos, state);
    }

    // TODO: 添加 GeckoLib 模型后改为 ENTITYBLOCK_ANIMATED
    @NotNull
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        if (type == ModBlockEntities.DEPLOYABLE_ZONE_HIGH.get()) {
            @SuppressWarnings("unchecked")
            BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) (level1, pos1, state1, be) ->
                    DeployableZoneHighBlockEntity.serverTick(level1, pos1, state1, (DeployableZoneHighBlockEntity) be);
            return ticker;
        }
        return null;
    }

    // ==================== 多方块放置/拆除 ====================

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!BoundingHelper.canPlaceBoundingBlocks(context.getLevel(), context.getClickedPos(), BOUNDING_OFFSETS)) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            if (!BoundingHelper.placeBoundingBlocks(level, pos, BOUNDING_OFFSETS)) {
                level.removeBlock(pos, false);
                if (placer instanceof Player player && !player.getAbilities().instabuild) {
                    popResource(level, pos, stack.copy());
                } else if (!(placer instanceof Player)) {
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof DeployableZoneHighBlockEntity be) {
            player.sendSystemMessage(Component.literal(
                    "[可部署高台] 已运行 " + be.getTickCount() + " tick"));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // ==================== 碰撞箱：实体不可穿过 ====================

    @NotNull
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_CUBE;
    }

    @NotNull
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_CUBE;
    }

    @NotNull
    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_CUBE;
    }

    @NotNull
    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE_CUBE;
    }
}
