package org.lingZero.m_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.Register.ModItems;
import org.lingZero.m_defend.util.DebugLogger;

public class Turret1Block extends Block implements EntityBlock {
    
    // 多方块结构尺寸配置
    public static final int STRUCTURE_WIDTH = 0;   // 区域宽度（X轴和Z轴的半径，0表示1x1）
    public static final int STRUCTURE_HEIGHT = 2;  // 区域高度（Y轴）
    
    public Turret1Block(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Turret1BlockEntity(pos, state);
    }
    
    /**
     * 方块被破坏时调用
     * 注意：不再自动拆除结构，需要使用拆除工具
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
    
    /**
     * 使用拆除工具拆除整个炮塔结构
     *
     * @param level 世界实例
     * @param mainBlockPos 主方块坐标
     * @param player 玩家实例
     */
    public static void dismantleStructure(Level level, BlockPos mainBlockPos, Player player) {
        if (level.isClientSide) {
            return;
        }
        
        DebugLogger.info("开始拆除炮塔结构 at: " + mainBlockPos);
        
        // 破坏所有附属方块
        destroyAffiliateBlocks(level, mainBlockPos);
        
        // 破坏主方块并掉落物品
        level.destroyBlock(mainBlockPos, false); // 不自动掉落，手动控制
        
        // 手动掉落 Turret1Item
        ItemStack itemStack = new ItemStack(ModItems.TURRET_1_ITEM.get());
        popResource(level, mainBlockPos, itemStack);
        
        DebugLogger.info("炮塔结构拆除完成，掉落物品: Turret1Item");
    }
    
    /**
     * 破坏所有附属方块
     *
     * @param level 世界实例
     * @param mainBlockPos 主方块坐标
     */
    private static void destroyAffiliateBlocks(Level level, BlockPos mainBlockPos) {
        // 计算结构的基准位置（底层）
        BlockPos basePos = mainBlockPos.offset(0, -(STRUCTURE_HEIGHT - 1), 0);
        
        // 遍历区域内的所有位置
        for (int x = -STRUCTURE_WIDTH; x <= STRUCTURE_WIDTH; x++) {
            for (int y = 0; y < STRUCTURE_HEIGHT; y++) {
                for (int z = -STRUCTURE_WIDTH; z <= STRUCTURE_WIDTH; z++) {
                    BlockPos checkPos = basePos.offset(x, y, z);
                    
                    // 跳过主方块位置
                    if (checkPos.equals(mainBlockPos)) {
                        continue;
                    }
                    
                    // 检查是否为附属方块
                    if (level.getBlockState(checkPos).getBlock() instanceof AffiliateBlock) {
                        level.destroyBlock(checkPos, false); // 不掉落物品
                        DebugLogger.debug("破坏附属方块 at: " + checkPos);
                    }
                }
            }
        }
        
        DebugLogger.info("附属方块破坏完成");
    }
}
