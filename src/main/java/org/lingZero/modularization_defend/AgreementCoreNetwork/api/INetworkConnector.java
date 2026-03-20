package org.lingZero.modularization_defend.AgreementCoreNetwork.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * 网络连接器接口
 * 实现此接口的方块可以连接到网络
 * 
 * @author Modularization Defend Team
 */
public interface INetworkConnector extends INetworkNode {
    
    /**
     * 检查是否可以连接
     * @return 是否可连接
     */
    boolean canConnect();
    
    /**
     * 获取连接主控点（用于多方块结构）
     * @param cableType 线缆类型
     * @param target 目标信息
     * @return 主控点位置
     */
    BlockPos getConnectionMaster(ICableType cableType, Object target);
}
