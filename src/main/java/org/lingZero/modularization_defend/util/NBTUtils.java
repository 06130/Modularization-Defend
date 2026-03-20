package org.lingZero.modularization_defend.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * NBT 解析工具类
 * 提供从方块实体中解析和提取 NBT 数据的工具方法
 * 
 * <h2>使用方法：</h2>
 * <pre>
 * // 读取 NBT 数据
 * Map&lt;String, Tag&gt; nbtData = NBTUtils.getAllNBTEntries(level, pos);
 * CompoundTag compound = NBTUtils.getCompoundTag(level, pos, "Inventory");
 * int value = NBTUtils.getIntTag(level, pos, "Energy", 0);
 * 
 * // 写入 NBT 数据
 * NBTUtils.putBlockTag(level, pos, "CustomData", customTag);
 * NBTUtils.putIntTag(level, pos, "Energy", 1000);
 * </pre>
 */
public final class NBTUtils {
    
    /**
     * 私有构造函数，防止实例化
     */
    private NBTUtils() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * 获取方块实体的完整 NBT 数据
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 获取箱子的所有 NBT 数据
     * Map&lt;String, Tag&gt; chestNBT = NBTUtils.getAllNBTEntries(level, chestPos);
     * for (Map.Entry&lt;String, Tag&gt; entry : chestNBT.entrySet()) {
     *     System.out.println(entry.getKey() + ": " + entry.getValue());
     * }
     * </pre>
     * 
     * @param level 世界
     * @param pos 方块位置
     * @return NBT 条目映射，如果方块没有 BlockEntity 则返回空映射
     */
    @NotNull
    public static Map<String, Tag> getAllNBTEntries(@Nullable Level level, @Nullable BlockPos pos) {
        if (level == null || pos == null) {
            return Collections.emptyMap();
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return Collections.emptyMap();
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null) {
            return Collections.emptyMap();
        }
        
        return getAllEntries(nbt);
    }
    
    /**
     * 从 CompoundTag 中提取所有条目
     * 
     * @param nbt NBT 标签
     * @return 所有条目的映射
     */
    @NotNull
    private static Map<String, Tag> getAllEntries(@NotNull CompoundTag nbt) {
        Map<String, Tag> entries = new HashMap<>();
        for (String key : nbt.getAllKeys()) {
            entries.put(key, nbt.get(key));
        }
        return entries;
    }
    
    /**
     * 获取指定位置的 CompoundTag
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 获取机械的库存数据
     * CompoundTag inventory = NBTUtils.getCompoundTag(level, pos, "Inventory");
     * if (inventory != null) {
     *     // 处理库存数据
     * }
     * </pre>
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @return CompoundTag，不存在则返回 null
     */
    @Nullable
    public static CompoundTag getCompoundTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName) {
        if (level == null || pos == null || tagName == null) {
            return null;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null || !nbt.contains(tagName, Tag.TAG_COMPOUND)) {
            return null;
        }
        
        return nbt.getCompound(tagName);
    }
    
    /**
     * 获取指定位置的 ListTag
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 获取物品列表
     * ListTag items = NBTUtils.getListTag(level, pos, "Items");
     * if (items != null) {
     *     for (int i = 0; i < items.size(); i++) {
     *         CompoundTag item = items.getCompound(i);
     *         // 处理物品数据
     *     }
     * }
     * </pre>
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @return ListTag，不存在则返回 null
     */
    @Nullable
    public static ListTag getListTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName) {
        if (level == null || pos == null || tagName == null) {
            return null;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null || !nbt.contains(tagName, Tag.TAG_LIST)) {
            return null;
        }
        
        return nbt.getList(tagName, Tag.TAG_COMPOUND);
    }
    
    /**
     * 获取指定位置的 String 类型标签
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 获取自定义名称
     * String customName = NBTUtils.getStringTag(level, pos, "CustomName");
 * 
     * // 获取所有者名称
     * String owner = NBTUtils.getStringTag(level, pos, "OwnerName", "Unknown");
     * </pre>
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @return String 值，不存在则返回 null
     */
    @Nullable
    public static String getStringTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName) {
        return getStringTag(level, pos, tagName, null);
    }
    
    /**
     * 获取指定位置的 String 类型标签（带默认值）
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @param defaultValue 默认值
     * @return String 值，不存在则返回默认值
     */
    @Nullable
    public static String getStringTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName, @Nullable String defaultValue) {
        if (level == null || pos == null || tagName == null) {
            return defaultValue;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return defaultValue;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null || !nbt.contains(tagName, Tag.TAG_STRING)) {
            return defaultValue;
        }
        
        return nbt.getString(tagName);
    }
    
    /**
     * 获取指定位置的 Int 类型标签
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 获取能量存储
     * int energy = NBTUtils.getIntTag(level, pos, "Energy", 0);
     * 
     * // 获取进度值
     * int progress = NBTUtils.getIntTag(level, pos, "Progress", -1);
     * </pre>
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @param defaultValue 默认值
     * @return int 值
     */
    public static int getIntTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName, int defaultValue) {
        if (level == null || pos == null || tagName == null) {
            return defaultValue;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return defaultValue;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null || !nbt.contains(tagName, Tag.TAG_INT)) {
            return defaultValue;
        }
        
        return nbt.getInt(tagName);
    }
    
    /**
     * 获取指定位置的 Boolean 类型标签
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 检查是否激活
     * boolean isActive = NBTUtils.getBooleanTag(level, pos, "Active", false);
     * 
     * // 检查是否完成
     * boolean isFinished = NBTUtils.getBooleanTag(level, pos, "Finished", false);
     * </pre>
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @param defaultValue 默认值
     * @return boolean 值
     */
    public static boolean getBooleanTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName, boolean defaultValue) {
        if (level == null || pos == null || tagName == null) {
            return defaultValue;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return defaultValue;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null || !nbt.contains(tagName, Tag.TAG_BYTE)) {
            return defaultValue;
        }
        
        return nbt.getBoolean(tagName);
    }
    
    /**
     * 获取指定位置的 Double 类型标签
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @param defaultValue 默认值
     * @return double 值
     */
    public static double getDoubleTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName, double defaultValue) {
        if (level == null || pos == null || tagName == null) {
            return defaultValue;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return defaultValue;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null || !nbt.contains(tagName, Tag.TAG_DOUBLE)) {
            return defaultValue;
        }
        
        return nbt.getDouble(tagName);
    }
    
    /**
     * 获取指定位置的 Float 类型标签
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @param defaultValue 默认值
     * @return float 值
     */
    public static float getFloatTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName, float defaultValue) {
        if (level == null || pos == null || tagName == null) {
            return defaultValue;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return defaultValue;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null || !nbt.contains(tagName, Tag.TAG_FLOAT)) {
            return defaultValue;
        }
        
        return nbt.getFloat(tagName);
    }
    
    /**
     * 获取指定位置的 Long 类型标签
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @param defaultValue 默认值
     * @return long 值
     */
    public static long getLongTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName, long defaultValue) {
        if (level == null || pos == null || tagName == null) {
            return defaultValue;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return defaultValue;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null || !nbt.contains(tagName, Tag.TAG_LONG)) {
            return defaultValue;
        }
        
        return nbt.getLong(tagName);
    }
    
    /**
     * 获取指定位置的 ByteArray 类型标签
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @return byte 数组，不存在则返回 null
     */
    @Nullable
    public static byte[] getByteArrayTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName) {
        if (level == null || pos == null || tagName == null) {
            return null;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null || !nbt.contains(tagName, Tag.TAG_BYTE_ARRAY)) {
            return null;
        }
        
        return nbt.getByteArray(tagName);
    }
    
    /**
     * 获取指定位置的 IntArray 类型标签
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @return int 数组，不存在则返回 null
     */
    @Nullable
    public static int[] getIntArrayTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName) {
        if (level == null || pos == null || tagName == null) {
            return null;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null || !nbt.contains(tagName, Tag.TAG_INT_ARRAY)) {
            return null;
        }
        
        return nbt.getIntArray(tagName);
    }
    
    /**
     * 获取指定位置的 LongArray 类型标签
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @return long 数组，不存在则返回 null
     */
    @Nullable
    public static long[] getLongArrayTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName) {
        if (level == null || pos == null || tagName == null) {
            return null;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null || !nbt.contains(tagName, Tag.TAG_LONG_ARRAY)) {
            return null;
        }
        
        return nbt.getLongArray(tagName);
    }
    
    /**
     * 检查指定位置是否存在某个标签
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 检查是否有自定义数据
     * if (NBTUtils.hasTag(level, pos, "CustomData")) {
     *     // 处理自定义数据
     * }
     * 
     * // 检查是否有特定类型的标签
     * if (NBTUtils.hasTag(level, pos, "Energy", Tag.TAG_INT)) {
     *     // 处理能量数据
     * }
     * </pre>
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @return 是否存在
     */
    public static boolean hasTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName) {
        return hasTag(level, pos, tagName, -1);
    }
    
    /**
     * 检查指定位置是否存在某个标签（可指定类型）
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param tagName 标签名称
     * @param tagType 标签类型（使用 {@link Tag} 常量）
     * @return 是否存在
     */
    public static boolean hasTag(@Nullable Level level, @Nullable BlockPos pos, @NotNull String tagName, int tagType) {
        if (level == null || pos == null || tagName == null) {
            return false;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null) {
            return false;
        }
        
        if (tagType == -1) {
            return nbt.contains(tagName);
        } else {
            return nbt.contains(tagName, tagType);
        }
    }
    
    /**
     * 获取所有可用的标签键名
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 获取所有 NBT 键名
     * Set&lt;String&gt; keys = NBTUtils.getAllKeys(level, pos);
     * for (String key : keys) {
     *     System.out.println("Found tag: " + key);
     * }
     * </pre>
     * 
     * @param level 世界
     * @param pos 方块位置
     * @return 所有键名的集合
     */
    @NotNull
    public static Set<String> getAllKeys(@Nullable Level level, @Nullable BlockPos pos) {
        if (level == null || pos == null) {
            return Collections.emptySet();
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return Collections.emptySet();
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null) {
            return Collections.emptySet();
        }
        
        return nbt.getAllKeys();
    }
    
    /**
     * 判断方块是否有 NBT 数据
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 快速检查方块是否有 NBT 数据
     * if (NBTUtils.hasNBT(level, pos)) {
     *     // 处理 NBT 数据
     * }
     * </pre>
     * 
     * @param level 世界
     * @param pos 方块位置
     * @return 是否有 NBT 数据
     */
    public static boolean hasNBT(@Nullable Level level, @Nullable BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        return nbt != null && !nbt.isEmpty();
    }
    
    /**
     * 深度解析嵌套的 NBT 结构，返回扁平化的 Map
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 解析复杂的嵌套 NBT 数据
     * Map&lt;String, Object&gt; flatData = NBTUtils.flattenNBT(level, pos);
     * for (Map.Entry&lt;String, Object&gt; entry : flatData.entrySet()) {
     *     System.out.println(entry.getKey() + " = " + entry.getValue());
     * }
     * </pre>
     * 
     * @param level 世界
     * @param pos 方块位置
     * @return 扁平化的数据映射
     */
    @NotNull
    public static Map<String, Object> flattenNBT(@Nullable Level level, @Nullable BlockPos pos) {
        Map<String, Object> result = new HashMap<>();
        
        if (level == null || pos == null) {
            return result;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return result;
        }
        
        CompoundTag nbt = blockEntity.saveWithoutMetadata(level.registryAccess());
        if (nbt == null) {
            return result;
        }
        
        flattenCompound("", nbt, result);
        return result;
    }
    
    /**
     * 递归展平 CompoundTag
     * 
     * @param prefix 前缀
     * @param compound 要展平的 CompoundTag
     * @param result 结果 Map
     */
    private static void flattenCompound(String prefix, CompoundTag compound, Map<String, Object> result) {
        for (String key : compound.getAllKeys()) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            Tag tag = compound.get(key);
            
            if (tag instanceof CompoundTag) {
                flattenCompound(fullKey, (CompoundTag) tag, result);
            } else if (tag instanceof ListTag) {
                result.put(fullKey, convertListToList((ListTag) tag));
            } else {
                result.put(fullKey, convertTagToObject(tag));
            }
        }
    }
    
    /**
     * 将 ListTag 转换为 Java List
     * 
     * @param listTag ListTag
     * @return Java List
     */
    @NotNull
    private static List<Object> convertListToList(ListTag listTag) {
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < listTag.size(); i++) {
            Tag tag = listTag.get(i);
            if (tag instanceof CompoundTag) {
                Map<String, Object> nested = new HashMap<>();
                flattenCompound("", (CompoundTag) tag, nested);
                result.add(nested);
            } else if (tag instanceof ListTag) {
                result.add(convertListToList((ListTag) tag));
            } else {
                result.add(convertTagToObject(tag));
            }
        }
        return result;
    }
    
    /**
     * 将 Tag 转换为对应的 Java 对象
     * 
     * @param tag NBT Tag
     * @return Java 对象
     */
    @Nullable
    private static Object convertTagToObject(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        switch (tag.getId()) {
            case Tag.TAG_BYTE:
                return ((net.minecraft.nbt.ByteTag) tag).getAsByte();
            case Tag.TAG_SHORT:
                return ((net.minecraft.nbt.ShortTag) tag).getAsShort();
            case Tag.TAG_INT:
                return ((net.minecraft.nbt.IntTag) tag).getAsInt();
            case Tag.TAG_LONG:
                return ((net.minecraft.nbt.LongTag) tag).getAsLong();
            case Tag.TAG_FLOAT:
                return ((net.minecraft.nbt.FloatTag) tag).getAsFloat();
            case Tag.TAG_DOUBLE:
                return ((net.minecraft.nbt.DoubleTag) tag).getAsDouble();
            case Tag.TAG_STRING:
                return ((net.minecraft.nbt.StringTag) tag).getAsString();
            default:
                return tag.toString();
        }
    }
}
