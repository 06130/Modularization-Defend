package org.lingZero.modularization_defend.AgreementCoreNetwork.transfer.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.IConnectionPoint;
import org.lingZero.modularization_defend.AgreementCoreNetwork.network.LocalNetwork;

/**
 * 能量连接器接口
 * 实现此接口的方块可以参与能量网络
 * 
 * @author Modularization Defend Team
 */
public interface IEnergyConnector {
    
    /**
     * 检查是否为能源（提供能量）
     * @param cp 连接点
     * @return 是否为能源
     */
    boolean isSource(IConnectionPoint cp);
    
    /**
     * 检查是否为用能器（消耗能量）
     * @param cp 连接点
     * @return 是否为用能器
     */
    boolean isSink(IConnectionPoint cp);
    
    /**
     * 获取可用能量
     * @return 可用能量值
     */
    default int getAvailableEnergy() {
        return 0;
    }
    
    /**
     * 获取请求能量
     * @return 请求的能量值
     */
    default int getRequestedEnergy() {
        return 0;
    }
    
    /**
     * 注入能量
     * @param amount 能量值
     */
    default void insertEnergy(int amount) {
    }
    
    /**
     * 提取能量
     * @param amount 能量值
     */
    default void extractEnergy(int amount) {
    }
    
    /**
     * 当能量通过时调用
     * @param amount 通过的能量值
     */
    default void onEnergyPassedThrough(double amount) {
    }
}
