package org.lingZero.modularization_defend.EnergyNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * 能源网络节点接口
 * 所有参与能源网络的设备都需要实现此接口
 */
public interface IEnergyNode {
    
    /**
     * 获取节点位置
     * @return 方块坐标
     */
    BlockPos getNodePosition();
    
    /**
     * 获取节点所在等级
     * @return 等级对象
     */
    Level getLevel();
    
    /**
     * 获取能量缓冲区
     * @return 能量缓冲区对象
     */
    EnergyBuffer getEnergyBuffer();
    
    /**
     * 设置能量缓冲区（由网络扫描时调用）
     * @param energy 能量值
     */
    void setEnergyBuffer(long energy);
    
    /**
     * 获取最大能量容量
     * @return 最大容量
     */
    long getMaxCapacity();
    
    /**
     * 获取当前能量需求
     * @return 每 tick 需要的能量
     */
    long getEnergyDemand();
    
    /**
     * 接收能量传输
     * @param amount 接收的能量量
     * @param from 来源节点
     * @return 实际接收的能量量
     */
    long receiveEnergy(long amount, IEnergyNode from);
    
    /**
     * 输出能量传输
     * @param amount 输出的能量量
     * @param to 目标节点
     * @return 实际输出的能量量
     */
    long extractEnergy(long amount, IEnergyNode to);
    
    /**
     * 网络扫描回调（每 5 秒调用一次，用于填满缓冲区）
     */
    void onNetworkScan();
    
    /**
     * 检查节点是否有效（未被破坏等）
     * @return true 如果节点有效
     */
    boolean isValid();
    
    /**
     * 获取节点优先级（用于能量分配）
     * @return 优先级值，越高越优先
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * 获取节点类型
     * @return 节点类型枚举
     */
    NodeType getNodeType();
    
    /**
     * 能源节点类型枚举
     */
    enum NodeType {
        /**
         * 协议核心（网络中心）
         */
        PROTOCOL_CORE,
        
        /**
         * 中继器（能量中转站）
         */
        REPEATER,
        
        /**
         * 产能设备（发电机等）
         */
        PRODUCER,
        
        /**
         * 耗能设备（机器等）
         */
        CONSUMER,
        
        /**
         * 储能设备（电池等）
         */
        STORAGE
    }
}
