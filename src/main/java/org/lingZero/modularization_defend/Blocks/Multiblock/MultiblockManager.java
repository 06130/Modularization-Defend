package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 多方块管理器
 * 用于管理世界中的所有多方块结构
 */
public class MultiblockManager {
    private static final Map<UUID, MultiblockData> MULTIBLOCK_MAP = new HashMap<>();
    
    /**
     * 注册一个多方块结构
     */
    public static void registerMultiblock(MultiblockData data) {
        if (data != null && data.getControllerPos() != null) {
            // 使用控制器位置生成 UUID 作为键
            UUID key = generateKey(data.getControllerPos());
            MULTIBLOCK_MAP.put(key, data);
        }
    }
    
    /**
     * 移除一个多方块结构
     */
    public static void removeMultiblock(BlockPos controllerPos) {
        if (controllerPos != null) {
            UUID key = generateKey(controllerPos);
            MULTIBLOCK_MAP.remove(key);
        }
    }
    
    /**
     * 获取指定位置的多方块数据
     */
    @Nullable
    public static MultiblockData getMultiblock(BlockPos controllerPos) {
        if (controllerPos == null) {
            return null;
        }
        UUID key = generateKey(controllerPos);
        return MULTIBLOCK_MAP.get(key);
    }
    
    /**
     * 检查某个位置是否属于任何多方块结构
     */
    public static boolean isPartOfMultiblock(Level level, BlockPos pos) {
        for (MultiblockData data : MULTIBLOCK_MAP.values()) {
            if (data.containsBlock(pos)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 根据位置获取所属的多方块结构
     */
    @Nullable
    public static MultiblockData getMultiblockByPosition(Level level, BlockPos pos) {
        for (MultiblockData data : MULTIBLOCK_MAP.values()) {
            if (data.containsBlock(pos)) {
                return data;
            }
        }
        return null;
    }
    
    /**
     * 清理无效的多方块结构
     */
    public static void cleanupInvalidMultiblocks() {
        MULTIBLOCK_MAP.entrySet().removeIf(entry -> 
            entry.getValue() == null || !entry.getValue().isFormed()
        );
    }
    
    /**
     * 生成唯一的键值
     */
    private static UUID generateKey(BlockPos pos) {
        // 使用位置和维度信息生成唯一键
        long seed = pos.asLong();
        return new UUID(seed, seed);
    }
    
    /**
     * 获取当前注册的多方块数量
     */
    public static int getMultiblockCount() {
        return MULTIBLOCK_MAP.size();
    }
    
    /**
     * 清空所有多方块数据（用于世界卸载）
     */
    public static void clearAll() {
        MULTIBLOCK_MAP.clear();
    }
}
