package org.lingZero.m_defend.Items.Turret;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lingZero.m_defend.Blocks.Multiblock.AffiliateBlockEntity;
import org.lingZero.m_defend.Register.ModBlocks;
import org.lingZero.m_defend.util.DebugLogger;

public class Turret1Item extends Item {
    public Turret1Item(Properties properties) {
        super(properties);
    }

    /**
     * 当玩家手持该物品右键点击方块时触发
     *
     * @param context 使用上下文，包含点击位置等信息
     * @return 交互结果
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();
        
        // 只在服务端执行逻辑，避免重复触发
        if (!level.isClientSide && player != null) {
            handleRightClickBlock(player, context.getHand(), itemStack, clickedPos, level);
        }
        
        // 返回成功，消耗此次右键操作
        return InteractionResult.SUCCESS;
    }

    /**
     * 处理右键点击方块的逻辑
     *
     * @param player 玩家实例
     * @param hand 使用的手
     * @param itemStack 物品堆栈
     * @param clickedPos 被点击的方块坐标
     * @param level 世界实例
     */
    private void handleRightClickBlock(Player player, InteractionHand hand, ItemStack itemStack, BlockPos clickedPos, Level level) {
        DebugLogger.info("玩家 " + player.getName().getString() + " 右键点击了 Turret1Item");
        DebugLogger.info("点击的方块坐标: x=" + clickedPos.getX() + ", y=" + clickedPos.getY() + ", z=" + clickedPos.getZ());
        
        // 检查目标方块上方区域是否为空或可替换
        int width = 0;  // 区域宽度（X轴和Z轴的半径，0表示1x1）
        int height = 2; // 区域高度（Y轴）
        
        if (isAreaClear(level, clickedPos.above(), width, height)) {
            DebugLogger.info("目标区域为空或可替换，可以放置炮塔");
            // 放置多方块结构
            placeMultiblockStructure(level, clickedPos.above(), player, itemStack, hand);
        } else {
            DebugLogger.info("目标区域被占用，无法放置炮塔");
            // 向玩家发送提示消息
            player.sendSystemMessage(Component.translatable("message.modularization_defend.turret_placement_blocked"));
        }
    }
    
    /**
     * 检查指定区域是否为空或可替换方块
     *
     * @param level 世界实例
     * @param basePos 区域基准位置（从该位置开始检查）
     * @param width 区域宽度（X轴和Z轴的半径）
     * @param height 区域高度（Y轴）
     * @return 如果区域内所有方块都是空气或可替换方块则返回true
     */
    private boolean isAreaClear(Level level, BlockPos basePos, int width, int height) {
        // 遍历区域内的每个方块
        for (int x = -width; x <= width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = -width; z <= width; z++) {
                    BlockPos checkPos = basePos.offset(x, y, z);
                    BlockState blockState = level.getBlockState(checkPos);
                    
                    // 检查方块是否为空气或可替换
                    if (!blockState.isAir() && !blockState.canBeReplaced()) {
                        DebugLogger.debug("区域被占用: x=" + checkPos.getX() + ", y=" + checkPos.getY() + ", z=" + checkPos.getZ() + ", 方块: " + blockState.getBlock().getName().getString());
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * 放置多方块结构
     *
     * @param level 世界实例
     * @param basePos 结构基准位置（主方块将放置在此位置）
     * @param player 玩家实例
     * @param itemStack 物品堆栈
     * @param hand 使用的手
     */
    private void placeMultiblockStructure(Level level, BlockPos basePos, Player player, ItemStack itemStack, InteractionHand hand) {
        int width = 0;  // 区域宽度（X轴和Z轴的半径，0表示1x1）
        int height = 2; // 区域高度（Y轴）
        
        // 首先在顶层位置放置主方块
        BlockPos mainBlockPos = basePos.offset(0, height - 1, 0); // 主方块在顶层
        level.setBlock(mainBlockPos, ModBlocks.TURRET1_BLOCK.get().defaultBlockState(), 3);
        DebugLogger.info("放置主方块 at: " + mainBlockPos);
        
        // 遍历区域内的其他位置，放置附属方块
        for (int x = -width; x <= width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = -width; z <= width; z++) {
                    // 跳过主方块位置
                    if (x == 0 && y == height - 1 && z == 0) {
                        continue;
                    }
                    
                    BlockPos affiliatePos = basePos.offset(x, y, z);
                    level.setBlock(affiliatePos, ModBlocks.AFFILIATE_BLOCK.get().defaultBlockState(), 3);
                    
                    // 获取附属方块的 BlockEntity 并设置主方块坐标
                    BlockEntity blockEntity = level.getBlockEntity(affiliatePos);
                    if (blockEntity instanceof AffiliateBlockEntity affiliateBE) {
                        affiliateBE.setMainBlockPos(mainBlockPos);
                        DebugLogger.debug("设置附属方块 at " + affiliatePos + " 的主方块坐标为 " + mainBlockPos);
                    }
                }
            }
        }
        
        // 如果不是创造模式，消耗物品
        if (!player.isCreative()) {
            itemStack.shrink(1);
        }
        
        DebugLogger.info("多方块结构放置完成，共 " + ((width * 2 + 1) * (width * 2 + 1) * height) + " 个方块");
    }
}
