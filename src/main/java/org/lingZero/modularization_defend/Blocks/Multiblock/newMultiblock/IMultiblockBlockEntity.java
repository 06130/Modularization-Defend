package org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.util.DebugLogger;

/**
 * 多方块 BlockEntity NBT 管理接口
 * 
 * 设计思想参考 ImmersiveEngineering 和 Mekanism:
 * 1. 使用标准 Minecraft NBT 处理机制 (saveAdditional/loadAdditional)
 * 2. 区分持久化数据和网络同步数据
 * 3. 将 NBT 操作委托给专门的管理类
 */
public interface IMultiblockBlockEntity {
    
    /**
     * NBT 键名常量
     */
    String KEY_MULTIBLOCK_ID = "MultiblockId";
    String KEY_MASTER_POS = "MasterPos";
    String KEY_IS_MASTER = "IsMaster";
    String KEY_STRUCTURE_NAME = "StructureName";
    String KEY_FORMED = "MultiblockFormed";
    
    /**
     * 保存多方块数据到 NBT (用于持久化)
     * @param tag NBT 标签
     */
    default void saveMultiblockData(@NotNull CompoundTag tag) {
        // 由实现类提供具体逻辑
    }
    
    /**
     * 从 NBT 加载多方块数据 (用于持久化)
     * @param tag NBT 标签
     */
    default void loadMultiblockData(@NotNull CompoundTag tag) {
        // 由实现类提供具体逻辑
    }
    
    /**
     * 获取用于网络同步的 NBT 数据
     * @return 同步 NBT 标签
     */
    @NotNull
    default CompoundTag getSyncData() {
        CompoundTag syncTag = new CompoundTag();
        saveMultiblockData(syncTag);
        return syncTag;
    }
    
    /**
     * 处理接收到的网络同步数据
     * @param tag 同步 NBT 标签
     */
    default void handleSyncData(@NotNull CompoundTag tag) {
        loadMultiblockData(tag);
    }
    
    /**
     * 工具方法：将主方块坐标写入 NBT
     * @param tag NBT 标签
     * @param masterPos 主方块坐标
     */
    static void writeMasterPos(@NotNull CompoundTag tag, @NotNull BlockPos masterPos) {
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", masterPos.getX());
        posTag.putInt("y", masterPos.getY());
        posTag.putInt("z", masterPos.getZ());
        tag.put(KEY_MASTER_POS, posTag);
    }
    
    /**
     * 工具方法：从 NBT 读取主方块坐标
     * @param tag NBT 标签
     * @return 主方块坐标，如果不存在则返回 null
     */
    @Nullable
    static BlockPos readMasterPos(@NotNull CompoundTag tag) {
        if (!tag.contains(KEY_MASTER_POS, Tag.TAG_COMPOUND)) {
            return null;
        }
        
        CompoundTag posTag = tag.getCompound(KEY_MASTER_POS);
        int x = posTag.getInt("x");
        int y = posTag.getInt("y");
        int z = posTag.getInt("z");
        return new BlockPos(x, y, z);
    }
    
    /**
     * 工具方法：标记为主方块
     * @param tag NBT 标签
     */
    static void markAsMaster(@NotNull CompoundTag tag) {
        tag.putBoolean(KEY_IS_MASTER, true);
    }
    
    /**
     * 工具方法：检查是否为主方块
     * @param tag NBT 标签
     * @return 是否为主方块
     */
    static boolean isMaster(@NotNull CompoundTag tag) {
        return tag.getBoolean(KEY_IS_MASTER);
    }
    
    /**
     * 工具方法：设置结构名称
     * @param tag NBT 标签
     * @param structureName 结构名称
     */
    static void setStructureName(@NotNull CompoundTag tag, @NotNull String structureName) {
        tag.putString(KEY_STRUCTURE_NAME, structureName);
    }
    
    /**
     * 工具方法：获取结构名称
     * @param tag NBT 标签
     * @return 结构名称
     */
    @Nullable
    static String getStructureName(@NotNull CompoundTag tag) {
        String name = tag.getString(KEY_STRUCTURE_NAME);
        return name.isEmpty() ? null : name;
    }
    
    /**
     * 工具方法：标记为已成型
     * @param tag NBT 标签
     */
    static void setFormed(@NotNull CompoundTag tag) {
        tag.putBoolean(KEY_FORMED, true);
    }
    
    /**
     * 工具方法：检查是否已成型
     * @param tag NBT 标签
     * @return 是否已成型
     */
    static boolean isFormed(@NotNull CompoundTag tag) {
        return tag.getBoolean(KEY_FORMED);
    }
}
