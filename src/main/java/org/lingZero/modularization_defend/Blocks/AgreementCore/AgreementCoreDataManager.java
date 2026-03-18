package org.lingZero.modularization_defend.Blocks.AgreementCore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * 协议核心数据管理器
 * 负责管理协议核心的所有 NBT 数据操作，提供统一的读写接口
 * 
 * 管理的数据：
 * 1. 基础数据：value, isController
 * 2. 多方块数据：ControllerPos
 * 3. 能源网络数据：DirectNodes, 网络统计信息
 * 
 * @author Modularization Defend Team
 */
public class AgreementCoreDataManager {
    
    // NBT 键常量定义
    private static final String VALUE_KEY = "value";
    private static final String IS_CONTROLLER_KEY = "isController";
    private static final String CONTROLLER_POS_KEY = "ControllerPos";
    private static final String NETWORK_NBT_KEY = "EnergyNetwork";
    private static final String DIRECT_NODES_KEY = "DirectNodes";
    private static final String NODE_TYPE_KEY = "Type";
    private static final String TOTAL_NODES_KEY = "TotalNodes";
    private static final String LAST_SCAN_TIME_KEY = "LastScanTime";
    private static final String TOTAL_ENERGY_KEY = "TotalEnergy";
    private static final String TOTAL_CAPACITY_KEY = "TotalCapacity";
    
    // 心跳配置 NBT 键
    private static final String HEARTBEAT_INTERVAL_KEY = "HeartbeatInterval";
    private static final String HEARTBEAT_TIMER_KEY = "HeartbeatTimer";

    // 坐标子标签键
    private static final String POS_X_KEY = "X";
    private static final String POS_Y_KEY = "Y";
    private static final String POS_Z_KEY = "Z";
    
    // 被管理的方块实体
    private final AgreementCoreBlockEntity blockEntity;
    
    /**
     * 创建数据管理器
     * @param blockEntity 协议核心方块实体
     */
    public AgreementCoreDataManager(AgreementCoreBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }
    
    // ==================== 读取操作 ====================
    
    /**
     * 从 NBT 读取所有数据
     * @param tag NBT 标签
     * @param registries 注册表提供者
     */
    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        // 读取基础数据
        int value = tag.getInt(VALUE_KEY);
        boolean isController = tag.getBoolean(IS_CONTROLLER_KEY);
        
        // 读取主方块坐标
        BlockPos controllerPos = null;
        if (tag.contains(CONTROLLER_POS_KEY, 10)) { // 10 = CompoundTag
            controllerPos = readBlockPos(tag.getCompound(CONTROLLER_POS_KEY));
        }
        
        // 应用到方块实体
        applyReadData(value, isController, controllerPos);
        
        // 读取并恢复能源网络数据
        if (tag.contains(NETWORK_NBT_KEY, 10)) {
            readNetworkData(tag.getCompound(NETWORK_NBT_KEY), registries);
        }
        
        // 读取心跳配置
        readHeartbeatData(tag);
    }
    
    /**
     * 读取方块坐标
     */
    private BlockPos readBlockPos(CompoundTag tag) {
        return new BlockPos(
            tag.getInt(POS_X_KEY),
            tag.getInt(POS_Y_KEY),
            tag.getInt(POS_Z_KEY)
        );
    }
    
    /**
     * 读取心跳配置数据
     * @param tag NBT 标签
     */
    private void readHeartbeatData(CompoundTag tag) {
        try {
            // 读取心跳间隔
            if (tag.contains(HEARTBEAT_INTERVAL_KEY)) {
                int interval = tag.getInt(HEARTBEAT_INTERVAL_KEY);
                blockEntity.setHeartbeatInterval(interval);
            }
            
            // 读取心跳计时器
            if (tag.contains(HEARTBEAT_TIMER_KEY)) {
                int timer = tag.getInt(HEARTBEAT_TIMER_KEY);
                var timerField = AgreementCoreBlockEntity.class.getDeclaredField("heartbeatTimer");
                timerField.setAccessible(true);
                timerField.setInt(blockEntity, timer);
            }
        } catch (Exception e) {
            // 如果读取失败，使用默认值（已在构造函数中初始化）
        }
    }
    
    /**
     * 应用读取到的数据
     */
    private void applyReadData(int value, boolean isController, BlockPos controllerPos) {
        // 使用反射或公开方法设置值（根据实际字段可见性调整）
        try {
            var valueField = AgreementCoreBlockEntity.class.getDeclaredField("value");
            valueField.setAccessible(true);
            valueField.setInt(blockEntity, value);
            
            var controllerField = AgreementCoreBlockEntity.class.getDeclaredField("isController");
            controllerField.setAccessible(true);
            controllerField.setBoolean(blockEntity, isController);
            
            if (controllerPos != null) {
                var storedPosField = AgreementCoreBlockEntity.class.getDeclaredField("storedControllerPos");
                storedPosField.setAccessible(true);
                storedPosField.set(blockEntity, controllerPos);
            }
        } catch (Exception e) {
            throw new RuntimeException("读取协议核心数据失败", e);
        }
    }
    
    /**
     * 读取网络数据并恢复
     */
    private void readNetworkData(CompoundTag networkTag, HolderLookup.Provider registries) {
        // 已删除 EnergyNetwork 相关代码
        // 此方法暂时为空，等待重写
    }
    
    // ==================== 写入操作 ====================
    
    /**
     * 将所有数据写入到 NBT
     * @param tag NBT 标签
     * @param registries 注册表提供者
     */
    public void writeToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        // 写入基础数据
        writeBaseData(tag);
        
        // 写入主方块坐标
        writeControllerPos(tag);
        
        // 写入能源网络数据
        writeNetworkData(tag, registries);
        
        // 写入心跳配置
        writeHeartbeatData(tag);
    }
    
    /**
     * 写入基础数据
     */
    private void writeBaseData(CompoundTag tag) {
        try {
            var valueField = AgreementCoreBlockEntity.class.getDeclaredField("value");
            valueField.setAccessible(true);
            tag.putInt(VALUE_KEY, valueField.getInt(blockEntity));
            
            var controllerField = AgreementCoreBlockEntity.class.getDeclaredField("isController");
            controllerField.setAccessible(true);
            tag.putBoolean(IS_CONTROLLER_KEY, controllerField.getBoolean(blockEntity));
        } catch (Exception e) {
            throw new RuntimeException("写入协议核心基础数据失败", e);
        }
    }
    
    /**
     * 写入主方块坐标
     */
    private void writeControllerPos(CompoundTag tag) {
        try {
            var storedPosField = AgreementCoreBlockEntity.class.getDeclaredField("storedControllerPos");
            storedPosField.setAccessible(true);
            BlockPos storedControllerPos = (BlockPos) storedPosField.get(blockEntity);
            
            if (storedControllerPos != null) {
                CompoundTag controllerPosTag = new CompoundTag();
                controllerPosTag.putInt(POS_X_KEY, storedControllerPos.getX());
                controllerPosTag.putInt(POS_Y_KEY, storedControllerPos.getY());
                controllerPosTag.putInt(POS_Z_KEY, storedControllerPos.getZ());
                tag.put(CONTROLLER_POS_KEY, controllerPosTag);
            }
        } catch (Exception e) {
            throw new RuntimeException("写入主方块坐标失败", e);
        }
    }
    
    /**
     * 写入心跳配置数据
     * @param tag NBT 标签
     */
    private void writeHeartbeatData(CompoundTag tag) {
        try {
            // 写入心跳间隔
            var intervalField = AgreementCoreBlockEntity.class.getDeclaredField("heartbeatInterval");
            intervalField.setAccessible(true);
            int interval = intervalField.getInt(blockEntity);
            tag.putInt(HEARTBEAT_INTERVAL_KEY, interval);
            
            // 写入心跳计时器
            var timerField = AgreementCoreBlockEntity.class.getDeclaredField("heartbeatTimer");
            timerField.setAccessible(true);
            int timer = timerField.getInt(blockEntity);
            tag.putInt(HEARTBEAT_TIMER_KEY, timer);
        } catch (Exception e) {
            throw new RuntimeException("写入心跳配置数据失败", e);
        }
    }
    
    /**
     * 写入网络数据（分级存储优化）
     */
    private void writeNetworkData(CompoundTag tag, HolderLookup.Provider registries) {
        // 已删除 EnergyNetwork 相关代码
        // 此方法暂时为空，等待重写
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 获取值
     */
    public int getValue() {
        try {
            var valueField = AgreementCoreBlockEntity.class.getDeclaredField("value");
            valueField.setAccessible(true);
            return valueField.getInt(blockEntity);
        } catch (Exception e) {
            throw new RuntimeException("获取值失败", e);
        }
    }
    
    /**
     * 设置值
     */
    public void setValue(int value) {
        try {
            var valueField = AgreementCoreBlockEntity.class.getDeclaredField("value");
            valueField.setAccessible(true);
            valueField.setInt(blockEntity, value);
        } catch (Exception e) {
            throw new RuntimeException("设置值失败", e);
        }
    }
    
    /**
     * 检查是否为控制器
     */
    public boolean isController() {
        try {
            var controllerField = AgreementCoreBlockEntity.class.getDeclaredField("isController");
            controllerField.setAccessible(true);
            return controllerField.getBoolean(blockEntity);
        } catch (Exception e) {
            throw new RuntimeException("检查控制器状态失败", e);
        }
    }
    
    /**
     * 设置控制器状态
     */
    public void setController(boolean controller) {
        try {
            var controllerField = AgreementCoreBlockEntity.class.getDeclaredField("isController");
            controllerField.setAccessible(true);
            controllerField.setBoolean(blockEntity, controller);
        } catch (Exception e) {
            throw new RuntimeException("设置控制器状态失败", e);
        }
    }
    
    /**
     * 获取存储的主方块坐标
     */
    public BlockPos getStoredControllerPos() {
        try {
            var storedPosField = AgreementCoreBlockEntity.class.getDeclaredField("storedControllerPos");
            storedPosField.setAccessible(true);
            return (BlockPos) storedPosField.get(blockEntity);
        } catch (Exception e) {
            return null;
        }
    }
}
