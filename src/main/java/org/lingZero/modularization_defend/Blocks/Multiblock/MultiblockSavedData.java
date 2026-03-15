package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 多方块数据持久化处理器
 * 负责保存和加载所有多方块结构的数据
 */
public class MultiblockSavedData extends SavedData {
    
    private static final String DATA_NAME = "modularization_defend_multiblocks";
    
    // 存储所有多方块数据（package-private 以便 MultiblockManager 访问）
    final Map<UUID, MultiblockDataEntry> multiblockEntries = new HashMap<>();
    
    /**
     * 从 NBT 加载数据
     */
    public void loadFromNbt(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        multiblockEntries.clear();
        
        if (nbt.contains("Multiblocks", Tag.TAG_LIST)) {
            ListTag multiblocksNbt = nbt.getList("Multiblocks", Tag.TAG_COMPOUND);
            for (int i = 0; i < multiblocksNbt.size(); i++) {
                CompoundTag entryNbt = multiblocksNbt.getCompound(i);
                if (entryNbt.hasUUID("ControllerUUID")) {
                    UUID id = entryNbt.getUUID("ControllerUUID");
                    MultiblockDataEntry entry = new MultiblockDataEntry();
                    entry.load(entryNbt);
                    multiblockEntries.put(id, entry);
                }
            }
        }
    }
    
    /**
     * 从 NBT 加载（SavedData 要求的方法）
     */
    public void load(@NotNull CompoundTag nbt) {
        loadFromNbt(nbt, net.minecraft.core.RegistryAccess.EMPTY);
    }
    
    /**
     * 保存数据到 NBT
     */
    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        ListTag multiblocksNbt = new ListTag();
        
        for (Map.Entry<UUID, MultiblockDataEntry> entry : multiblockEntries.entrySet()) {
            CompoundTag entryNbt = new CompoundTag();
            entryNbt.putUUID("ControllerUUID", entry.getKey());
            entry.getValue().save(entryNbt);
            multiblocksNbt.add(entryNbt);
        }
        
        nbt.put("Multiblocks", multiblocksNbt);
        return nbt;
    }
    
    /**
     * 获取或创建 SavedData 实例
     */
    public static MultiblockSavedData getOrCreate() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new IllegalStateException("Server is not available");
        }
        
        DimensionDataStorage dataStorage = server.overworld().getDataStorage();
        return dataStorage.computeIfAbsent(
            new Factory<>(MultiblockSavedData::new, (tag, provider) -> {
                MultiblockSavedData data = new MultiblockSavedData();
                data.loadFromNbt(tag, provider);
                return data;
            }),
            DATA_NAME
        );
    }
    
    /**
     * 注册一个多方块结构
     */
    public void registerMultiblock(UUID controllerUUID, MultiblockData data) {
        MultiblockDataEntry entry = new MultiblockDataEntry(data);
        multiblockEntries.put(controllerUUID, entry);
        setDirty();
    }
    
    /**
     * 移除一个多方块结构
     */
    public void removeMultiblock(UUID controllerUUID) {
        multiblockEntries.remove(controllerUUID);
        setDirty();
    }
    
    /**
     * 获取多方块数据
     */
    public MultiblockDataEntry getMultiblock(UUID controllerUUID) {
        return multiblockEntries.get(controllerUUID);
    }
    
    /**
     * 检查是否包含某个多方块
     */
    public boolean hasMultiblock(UUID controllerUUID) {
        return multiblockEntries.containsKey(controllerUUID);
    }
    
    /**
     * 清理无效的条目
     */
    public void cleanupInvalid() {
        multiblockEntries.entrySet().removeIf(entry -> 
            entry.getValue() == null || !entry.getValue().isFormed()
        );
        if (!multiblockEntries.isEmpty()) {
            setDirty();
        }
    }
    
    /**
     * 多方块数据条目
     */
    public static class MultiblockDataEntry {
        private boolean isFormed;
        private int width;
        private int height;
        
        public MultiblockDataEntry() {
            this.isFormed = false;
            this.width = 2;
            this.height = 10;
        }
        
        public MultiblockDataEntry(MultiblockData data) {
            this.isFormed = data.isFormed();
            this.width = data.getWidth();
            this.height = data.getHeight();
        }
        
        public void load(CompoundTag nbt) {
            this.isFormed = nbt.getBoolean("IsFormed");
            this.width = nbt.getInt("Width");
            this.height = nbt.getInt("Height");
        }
        
        public void save(CompoundTag nbt) {
            nbt.putBoolean("IsFormed", this.isFormed);
            nbt.putInt("Width", this.width);
            nbt.putInt("Height", this.height);
        }
        
        public boolean isFormed() {
            return isFormed;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
    }
}
