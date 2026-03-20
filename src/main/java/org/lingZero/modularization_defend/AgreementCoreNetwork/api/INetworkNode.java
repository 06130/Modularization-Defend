package org.lingZero.modularization_defend.AgreementCoreNetwork.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Collection;

/**
 * 网络节点接口
 * 表示网络中的一个连接点
 * 
 * @author Modularization Defend Team
 */
public interface INetworkNode {
    
    /**
     * 获取节点位置
     * @return 方块位置
     */
    BlockPos getPosition();
    
    /**
     * 获取该节点的所有连接点
     * @return 连接点集合
     */
    Collection<IConnectionPoint> getConnectionPoints();
    
    /**
     * 检查是否可以连接指定类型的线缆
     * @param cableType 线缆类型
     * @param target 目标连接点
     * @return 是否可连接
     */
    boolean canConnectCable(ICableType cableType, IConnectionPoint target);
    
    /**
     * 连接线缆
     * @param cableType 线缆类型
     * @param target 目标连接点
     * @param other 另一个连接器
     * @param otherTarget 另一个连接点
     */
    void connectCable(ICableType cableType, IConnectionPoint target, 
                     INetworkConnector other, IConnectionPoint otherTarget);
    
    /**
     * 移除线缆
     * @param connection 连接
     * @param attachedPoint 附加的连接点
     */
    void removeCable(INetworkConnection connection, IConnectionPoint attachedPoint);
    
    /**
     * 获取连接偏移量（用于渲染）
     * @param here 当前位置
     * @param other 另一端位置
     * @param type 线缆类型
     * @return 偏移向量
     */
    net.minecraft.world.phys.Vec3 getConnectionOffset(IConnectionPoint here, 
                                                      IConnectionPoint other, 
                                                      ICableType type);
    
    /**
     * 是否为代理节点（虚拟节点）
     * @return 是否为代理
     */
    default boolean isProxy() {
        return false;
    }
}
