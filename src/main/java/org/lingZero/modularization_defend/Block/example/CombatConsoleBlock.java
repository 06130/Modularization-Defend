package org.lingZero.modularization_defend.Block.example;

import com.sighs.apricityui.util.kjs.ApricityUIServerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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
 * 作战控制台——2 格高的竖直柱体多方块结构。
 * <p>
 * 放置时自动在上方生成 1 个占位方块，破坏时自动清理。
 * 右键点击打开 ApricityUI 监控面板界面。
 */
public class CombatConsoleBlock extends Block implements EntityBlock {

    /** 占位方块相对于主方块的偏移坐标（上方 1 格） */
    private static final BlockPos[] BOUNDING_OFFSETS = {
            new BlockPos(0, 1, 0),
    };

    /** 覆盖整个多方块结构的碰撞箱（10×10 宽立柱，2 格高） */
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 32, 13);

    public CombatConsoleBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.5F, 3600000.0F));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CombatConsoleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!BoundingHelper.canPlaceBoundingBlocks(context.getLevel(), context.getClickedPos(), BOUNDING_OFFSETS)) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        if (type == ModBlockEntities.COMBAT_CONSOLE.get()) {
            @SuppressWarnings("unchecked")
            BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) (level1, pos1, state1, be) ->
                    CombatConsoleBlockEntity.serverTick(level1, pos1, state1, (CombatConsoleBlockEntity) be);
            return ticker;
        }
        return null;
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

    /** 右键打开 ApricityUI 作战控制台监控面板 */
    @NotNull
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ApricityUIServerUtil.openScreen(serverPlayer, "apricity/combat_console/index.html",
                    ApricityUIServerUtil.bind()
                            .templatePath("apricity/combat_console/index.html")
                            .build());
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
