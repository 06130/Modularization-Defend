package org.lingZero.m_defend.Blocks.MultiblockFrame;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.m_defend.DataComponents.TurretType;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 炮塔主方块抽象基类
 * 所有炮塔主方块都应继承此类
 */
public abstract class BaseTurretBlock extends Block implements EntityBlock {
    
    /**
     * 获取炮塔类型
     * 子类必须实现此方法以返回对应的炮塔类型
     *
     * @return 炮塔类型枚举
     */
    public abstract TurretType getTurretType();
    
    // 多方块结构尺寸配置（子类可重写）
    public int getStructureWidth() {
        return 0;   // 默认区域宽度（X轴和Z轴的半径，0表示1x1）
    }
    
    public int getStructureHeight() {
        return 2;   // 默认区域高度（Y轴）
    }
    
    /**
     * 获取炮塔掉落物品
     * 子类必须实现此方法以返回对应的炮塔物品
     *
     * @return 炮塔物品
     */
    protected abstract Item getTurretItem();
    
    public BaseTurretBlock(Properties properties) {
        super(properties);
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
    public void dismantleStructure(Level level, BlockPos mainBlockPos, Player player) {
        if (level.isClientSide) {
            return;
        }
        
        DebugLogger.info("开始拆除炮塔结构 at: " + mainBlockPos);
        
        // 破坏所有附属方块
        destroyAffiliateBlocks(level, mainBlockPos);
        
        // 破坏主方块并掉落物品
        level.destroyBlock(mainBlockPos, false); // 不自动掉落，手动控制
        
        // 手动掉落炮塔物品
        ItemStack itemStack = new ItemStack(getTurretItem());
        popResource(level, mainBlockPos, itemStack);
        
        DebugLogger.info("炮塔结构拆除完成，掉落物品: " + getTurretItem().getDescriptionId());
    }
    
    /**
     * 破坏所有附属方块
     *
     * @param level 世界实例
     * @param mainBlockPos 主方块坐标
     */
    private void destroyAffiliateBlocks(Level level, BlockPos mainBlockPos) {
        int width = getStructureWidth();
        int height = getStructureHeight();
        
        // 计算结构的基准位置（底层）
        BlockPos basePos = mainBlockPos.offset(0, -(height - 1), 0);
        
        // 遍历区域内的所有位置
        for (int x = -width; x <= width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = -width; z <= width; z++) {
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
