package org.lingZero.modularization_defend.Block.example;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.Block.bounding.BoundingHelper;
import org.lingZero.modularization_defend.DataComponents.ModDataComponents;
import org.lingZero.modularization_defend.Item.EntitySelectorItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 幽灵多方块结构——3x3x3 立方体，主方块位于底面中心。
 * <p>
 * 关键特性：
 * <ul>
 *   <li>getShape() 返回完整 3x3x3 立方体轮廓——玩家可以瞄准结构任意位置进行交互</li>
 *   <li>getCollisionShape() 返回 Shapes.empty()——玩家和实体可以自由穿过</li>
 *   <li>手持实体选取器右键可写入/清除实体列表</li>
 * </ul>
 */
public class GhostMultiblockBlock extends Block implements EntityBlock {

    /**
     * 3x3x3 立方体的所有 26 个占位方块偏移（排除主方块自身的 (0,0,0)）。
     * 结构范围：x∈[-1,1], y∈[0,2], z∈[-1,1]
     */
    private static final BlockPos[] BOUNDING_OFFSETS = generateOffsets();

    /**
     * 完整 3x3x3 立方体的轮廓碰撞箱（用于鼠标瞄准检测，不阻挡移动）。
     * x: -16~32 (3格宽), y: 0~48 (3格高), z: -16~32 (3格深)
     */
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

    public GhostMultiblockBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(3.5F, 3600000.0F)
                .noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GhostMultiblockBlockEntity(pos, state);
    }

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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof EntitySelectorItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.sidedSuccess(true);
        }
        if (!(level.getBlockEntity(pos) instanceof GhostMultiblockBlockEntity be)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        ResourceLocation storedId = stack.get(ModDataComponents.STORED_ENTITY_ID.get());
        if (storedId != null) {
            // 实体选取器存有 ID → 写入 GhostMultiblock
            be.addEntityId(storedId);
            player.sendSystemMessage(Component.translatable(
                    "block.modularization_defend.ghost_multiblock.entity_added",
                    storedId.toString()));
        } else {
            // 实体选取器无 ID → 清空 GhostMultiblock 的实体列表
            be.clearEntityIds();
            player.sendSystemMessage(Component.translatable(
                    "block.modularization_defend.ghost_multiblock.entities_cleared"));
        }
        return ItemInteractionResult.sidedSuccess(false);
    }

    // ==================== 碰撞箱：可瞄准，无碰撞 ====================

    @NotNull
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_CUBE; // 允许玩家瞄准整个 3x3x3 立方体
    }

    @NotNull
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // 无碰撞，玩家和实体可自由穿过
    }

    @NotNull
    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_CUBE; // 鼠标指向时显示整体轮廓
    }

    @NotNull
    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty(); // 不遮挡相邻方块的渲染面
    }
}
