package org.lingZero.m_defend.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.m_defend.ModularizationDefend;

/**
 * 方块工具类
 * 提供方块相关的辅助方法
 */
public class BlockUtils {
    
    /**
     * 检查方块是否属于本模组
     * 
     * @param blockState 方块状态
     * @return true 如果方块属于本模组
     */
    public static boolean isModBlock(BlockState blockState) {
        if (blockState == null) {
            return false;
        }
        
        Block block = blockState.getBlock();
        var registryName = BuiltInRegistries.BLOCK.getKey(block);
        
        // 检查命名空间是否为本模组的 MODID
        return ModularizationDefend.MODID.equals(registryName.getNamespace());
    }
}
