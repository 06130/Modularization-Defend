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
    // 运行时缓存（内存中的快速访问）
    private static final Map<UUID, MultiblockData> MULTIBLOCK_MAP = new HashMap<>();
    
    /**
     * 注册一个多方块结构
     */
    public static void registerMultiblock(MultiblockData data) {
        if (data != null && data.getControllerPos() != null) {
            // 使用控制器位置生成 UUID 作为键
            UUID key = generateKey(data.getControllerPos());
            MULTIBLOCK_MAP.put(key, data);
            
            // 同时保存到持久化数据
            saveToPersistent(data, key);
        }
    }
    
    /**
     * 移除一个多方块结构
     */
    public static void removeMultiblock(BlockPos controllerPos) {
        if (controllerPos != null) {
            UUID key = generateKey(controllerPos);
            MULTIBLOCK_MAP.remove(key);
            
            // 同时从持久化数据中移除
            removeFromPersistent(key);
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
     * 从持久化数据加载所有多方块到内存
     */
    public static void loadFromPersistent(Level level) {
        if (level == null || level.isClientSide) {
            return;
        }
        
        try {
            MultiblockSavedData savedData = MultiblockSavedData.getOrCreate();
            
            // 遍历所有保存的多方块数据并重新验证
            for (Map.Entry<UUID, MultiblockSavedData.MultiblockDataEntry> entry : 
                 savedData.multiblockEntries.entrySet()) {
                
                UUID controllerUUID = entry.getKey();
                
                // 从 UUID 恢复控制器位置（与 generateKey 相反的操作）
                long mostSig = controllerUUID.getMostSignificantBits();
                long leastSig = controllerUUID.getLeastSignificantBits();
                
                int x = (int)(mostSig >>> 32);
                int z = (int)(mostSig & 0xFFFFFFFFL);
                int y = (int)(leastSig >>> 32);
                
                BlockPos controllerPos = new BlockPos(x, y, z);
                
                // 创建新的 MultiblockData 并尝试验证
                MultiblockData data = new MultiblockData(level, controllerPos);
                if (data.tryForm()) {
                    // 验证成功，添加到内存缓存
                    UUID key = generateKey(controllerPos);
                    MULTIBLOCK_MAP.put(key, data);
                }
            }
        } catch (Exception e) {
            // 如果加载失败，忽略错误（可能是存档损坏或不存在）
        }
    }
    
    /**
     * 保存到持久化数据
     */
    private static void saveToPersistent(MultiblockData data, UUID key) {
        try {
            MultiblockSavedData savedData = MultiblockSavedData.getOrCreate();
            savedData.registerMultiblock(key, data);
        } catch (Exception e) {
            // 忽略保存错误
        }
    }
    
    /**
     * 从持久化数据移除
     */
    private static void removeFromPersistent(UUID key) {
        try {
            MultiblockSavedData savedData = MultiblockSavedData.getOrCreate();
            savedData.removeMultiblock(key);
        } catch (Exception e) {
            // 忽略移除错误
        }
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
        // 使用位置信息生成唯一键（简化版本，不考虑维度）
        long mostSig = (long)pos.getX() << 32 | (pos.getZ() & 0xFFFFFFFFL);
        long leastSig = (long)pos.getY() << 32 | (pos.hashCode() & 0xFFFFFFFFL);
        return new UUID(mostSig, leastSig);
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
