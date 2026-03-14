package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 多方块结构放置工具类
 * 负责处理多方块的检测、放置和验证逻辑
 */
public class MultiblockPlacer {
    
    /**
     * 检查指定位置是否可以形成多方块结构
     * 
     * @param level 世界访问器
     * @param controllerPos 控制器位置（多方块的起始点）
     * @return 如果可以形成返回 true，否则返回 false
     */
    public static boolean canFormMultiblock(Level level, BlockPos controllerPos, IMultiblockConfig config) {
        return config.canForm(level, controllerPos);
    }
    
    /**
     * 一次性放置整个多方块结构
     * 
     * @param level 世界
     * @param controllerPos 控制器位置
     * @param config 多方块配置
     */
    public static void placeEntireMultiblock(Level level, BlockPos controllerPos, IMultiblockConfig config) {
        config.place(level, controllerPos);
    }
    
    /**
     * 尝试放置多方块结构
     * 如果放置失败会返还物品
     * 
     * @param level 世界
     * @param controllerPos 控制器位置
     * @param player 玩家
     * @param itemStack 物品堆
     * @param config 多方块配置
     * @return 如果放置成功返回 true，否则返回 false
     */
    public static boolean tryPlaceMultiblock(Level level, BlockPos controllerPos, Player player, ItemStack itemStack, IMultiblockConfig config) {
        // 检查是否可以形成多方块
        if (!canFormMultiblock(level, controllerPos, config)) {
            return false;
        }
        
        // 消耗一个物品
        if (!player.isCreative()) {
            itemStack.shrink(1);
        }
        
        // 放置多方块结构
        placeEntireMultiblock(level, controllerPos, config);
        
        return true;
    }
}
