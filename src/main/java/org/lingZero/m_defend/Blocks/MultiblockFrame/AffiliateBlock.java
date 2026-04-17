package org.lingZero.m_defend.Blocks.MultiblockFrame;


import net.minecraft.client.renderer.RenderType;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.Register.ModBlocks;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 附属方块，用于多方块结构中的附属部分
 */
public class AffiliateBlock extends Block implements EntityBlock {
    
    public AffiliateBlock(Properties properties) {
        super(properties);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AffiliateBlockEntity(pos, state);
    }
    
    /**
     * 设置方块的渲染层为透明
     */
    @OnlyIn(Dist.CLIENT)
    public static void initClient() {
        net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.AFFILIATE_BLOCK.get(), RenderType.translucent());
    }
    
    /**
     * 重定向到主方块坐标
     * 如果点击的是附属方块，返回其关联的主方块坐标；否则返回原坐标
     *
     * @param level 世界实例
     * @param clickedPos 点击的方块坐标
     * @return 重定向后的坐标（如果是附属方块则返回主方块坐标，否则返回原坐标）
     */
    public static BlockPos redirectIfAffiliate(Level level, BlockPos clickedPos) {
        if (level.getBlockState(clickedPos).getBlock() instanceof AffiliateBlock) {
            DebugLogger.debug("检测到附属方块 at: " + clickedPos + "，尝试获取主方块坐标");
            
            // 获取 BlockEntity 并读取主方块坐标
            BlockEntity blockEntity = level.getBlockEntity(clickedPos);
            if (blockEntity instanceof AffiliateBlockEntity affiliateBE) {
                BlockPos mainBlockPos = affiliateBE.getMainBlockPos();
                DebugLogger.debug("找到主方块坐标: " + mainBlockPos);
                
                // 验证主方块是否存在且仍为 BaseTurretBlock
                if (level.getBlockState(mainBlockPos).getBlock() instanceof BaseTurretBlock) {
                    return mainBlockPos; // 重定向到主方块
                } else {
                    DebugLogger.warn("主方块不存在或已被破坏");
                }
            } else {
                DebugLogger.warn("无法获取附属方块的 BlockEntity");
            }
        }
        
        // 如果不是附属方块或重定向失败，返回原坐标
        return clickedPos;
    }
    
    /**
     * 方块被破坏时调用
     * 重定向到主方块并执行破坏逻辑
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            // 只在服务端执行
            if (!level.isClientSide) {
                DebugLogger.info("附属方块被破坏 at: " + pos + "，查找主方块");
                
                // 使用重定向方法获取主方块坐标
                BlockPos mainBlockPos = redirectIfAffiliate(level, pos);
                
                // 如果重定向成功且不是原位置，说明是附属方块
                if (!mainBlockPos.equals(pos)) {
                    DebugLogger.info("重定向到主方块并执行破坏");
                    // 破坏主方块（会触发 Turret1Block.onRemove 从而破坏所有附属方块）
                    level.destroyBlock(mainBlockPos, true); // 掉落物品
                } else {
                    DebugLogger.warn("无法获取主方块坐标，仅破坏当前附属方块");
                }
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
    
    /**
     * 左键点击方块时的处理逻辑
     * 重定向到主方块
     */
    @Override
    protected void attack(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        // 重定向到主方块
        BlockPos mainBlockPos = redirectIfAffiliate(level, pos);
        
        // 如果重定向成功且不是原位置，调用主方块的 attack 方法
        if (!mainBlockPos.equals(pos)) {
            BlockState mainState = level.getBlockState(mainBlockPos);
            if (mainState.getBlock() instanceof BaseTurretBlock) {
                DebugLogger.debug("左键点击附属方块，重定向到主方块: " + mainBlockPos);
                // 由于 attack 是 protected 方法，我们通过主方块实例来处理
                // 这里暂时不处理，因为炮塔通常不需要左键交互
                // 如果需要，可以在 BaseTurretBlock 中添加公共静态方法供调用
            }
        } else {
            // 否则调用默认行为
            super.attack(state, level, pos, player);
        }
    }
    
    /**
     * 右键点击方块时的处理逻辑（带物品）
     * 重定向到主方块
     */
    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, 
                                                        @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, 
                                                        @NotNull BlockHitResult hitResult) {
        // 重定向到主方块
        BlockPos mainBlockPos = redirectIfAffiliate(level, pos);
        
        // 如果重定向成功且不是原位置，调用主方块的交互逻辑
        if (!mainBlockPos.equals(pos)) {
            BlockEntity mainBE = level.getBlockEntity(mainBlockPos);
            if (mainBE instanceof BaseTurretBlockEntity turretBE) {
                DebugLogger.debug("右键点击附属方块（带物品），重定向到主方块: " + mainBlockPos);
                // 使用公共静态方法处理交互
                InteractionResult result = BaseTurretBlock.handleInteraction(turretBE, player, stack);
                return result == InteractionResult.PASS ? 
                    ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : 
                    ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        
        // 否则调用默认行为
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
    
    /**
     * 右键点击方块时的处理逻辑（不带物品）
     * 重定向到主方块
     */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, 
                                                         @NotNull Player player, @NotNull BlockHitResult hitResult) {
        // 重定向到主方块
        BlockPos mainBlockPos = redirectIfAffiliate(level, pos);
        
        // 如果重定向成功且不是原位置，调用主方块的交互逻辑
        if (!mainBlockPos.equals(pos)) {
            BlockEntity mainBE = level.getBlockEntity(mainBlockPos);
            if (mainBE instanceof BaseTurretBlockEntity turretBE) {
                DebugLogger.debug("右键点击附属方块（不带物品），重定向到主方块: " + mainBlockPos);
                // 获取玩家手持物品
                ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
                // 使用公共静态方法处理交互
                return BaseTurretBlock.handleInteraction(turretBE, player, heldItem);
            }
        }
        
        // 否则调用默认行为
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
