package org.lingZero.modularization_defend.Block.bounding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * 不可见的占位方块，用于填充多方块结构的虚拟体积。
 * <p>
 * 关键行为：
 * <ul>
 *   <li>不可见（RenderShape.INVISIBLE）</li>
 *   <li>碰撞箱从主方块代理并反向偏移，形成完整结构碰撞</li>
 *   <li>所有交互（右键、破坏、爆炸等）代理到主方块</li>
 *   <li>不能被活塞推动</li>
 * </ul>
 */
public class BoundingBlock extends Block implements EntityBlock {

    public BoundingBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5F, 4.8F)
                .requiresCorrectToolForDrops()
                .dynamicShape()
                .noOcclusion()
                .pushReaction(PushReaction.BLOCK));
    }

    /**
     * 从世界中查找占位方块指向的主方块坐标。
     */
    @Nullable
    public static BlockPos getMainBlockPos(BlockGetter world, BlockPos thisPos) {
        if (world.getBlockEntity(thisPos) instanceof BoundingBlockEntity be) {
            BlockPos mainPos = be.getMainPos();
            if (mainPos != null && !mainPos.equals(thisPos)) {
                return mainPos;
            }
        }
        return null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoundingBlockEntity(pos, state);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        BlockPos mainPos = getMainBlockPos(context.getLevel(), context.getClickedPos());
        // 如果没有主方块或主方块失效，允许替换（清理残留占位方块）
        return mainPos == null || context.getLevel().getBlockState(mainPos).isAir();
    }

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return InteractionResult.FAIL;
        }
        BlockState mainState = world.getBlockState(mainPos);
        return mainState.useWithoutItem(world, player, hit.withPosition(mainPos));
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return ItemInteractionResult.FAIL;
        }
        BlockState mainState = world.getBlockState(mainPos);
        return mainState.useItemOn(stack, world, player, hand, hit.withPosition(mainPos));
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockPos mainPos = getMainBlockPos(world, pos);
            if (mainPos != null) {
                BlockState mainState = world.getBlockState(mainPos);
                if (!mainState.isAir()) {
                    // 移除主方块，主方块的onRemove会级联清理其余占位方块
                    world.removeBlock(mainPos, false);
                }
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player,
                                       boolean willHarvest, FluidState fluidState) {
        if (willHarvest) {
            return true; // 玩家处于创造模式时直接通过
        }
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            BlockState mainState = world.getBlockState(mainPos);
            if (!mainState.isAir()) {
                mainState.onDestroyedByPlayer(world, mainPos, player, false, mainState.getFluidState());
            }
        }
        return super.onDestroyedByPlayer(state, world, pos, player, false, fluidState);
    }

    @NotNull
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos != null) {
            BlockState mainState = level.getBlockState(mainPos);
            if (!mainState.isAir()) {
                // 代理到主方块处理破坏逻辑（如掉落物品）
                mainState.getBlock().playerWillDestroy(level, mainPos, mainState, player);
                return state;
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion,
                                  BiConsumer<ItemStack, BlockPos> dropConsumer) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos == null) {
            super.onExplosionHit(state, level, pos, explosion, dropConsumer);
        } else {
            level.getBlockState(mainPos).onExplosionHit(level, mainPos, explosion, dropConsumer);
        }
    }

    @NotNull
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos, Player player) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return ItemStack.EMPTY;
        }
        BlockState mainState = world.getBlockState(mainPos);
        return mainState.getBlock().getCloneItemStack(mainState, target, world, mainPos, player);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return super.getExplosionResistance(state, world, pos, explosion);
        }
        return world.getBlockState(mainPos).getExplosionResistance(world, mainPos, explosion);
    }

    @NotNull
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @NotNull
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return proxyShape(world, pos, context, (s, w, p, c) -> s.getShape(w, p, c));
    }

    @NotNull
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return proxyShape(world, pos, context, (s, w, p, c) -> s.getCollisionShape(w, p, c));
    }

    @NotNull
    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return proxyShape(world, pos, context, (s, w, p, c) -> s.getVisualShape(w, p, c));
    }

    @NotNull
    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return Shapes.empty();
        }
        BlockState mainState = world.getBlockState(mainPos);
        VoxelShape shape = mainState.getOcclusionShape(world, mainPos);
        BlockPos offset = pos.subtract(mainPos);
        return shape.move(-offset.getX(), -offset.getY(), -offset.getZ());
    }

    /**
     * 从主方块获取 shape 并反向偏移，使正确的一段对齐到当前占位方块位置。
     */
    private VoxelShape proxyShape(BlockGetter world, BlockPos pos, @Nullable CollisionContext context,
                                  ShapeGetter getter) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return Shapes.empty();
        }
        BlockState mainState = world.getBlockState(mainPos);
        VoxelShape shape = getter.get(mainState, world, mainPos, context);
        BlockPos offset = pos.subtract(mainPos);
        return shape.move(-offset.getX(), -offset.getY(), -offset.getZ());
    }

    @FunctionalInterface
    private interface ShapeGetter {
        VoxelShape get(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context);
    }
}
