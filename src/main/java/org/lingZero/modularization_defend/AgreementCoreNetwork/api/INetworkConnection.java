package org.lingZero.modularization_defend.AgreementCoreNetwork.api;

/**
 * 网络连接接口
 * 表示两个连接点之间的连接关系
 * 
 * @author Modularization Defend Team
 */
public interface INetworkConnection {
    
    /**
     * 获取线缆类型
     * @return 线缆类型
     */
    ICableType getType();
    
    /**
     * 获取端点 A
     * @return 连接点 A
     */
    IConnectionPoint getEndA();
    
    /**
     * 获取端点 B
     * @return 连接点 B
     */
    IConnectionPoint getEndB();
    
    /**
     * 获取另一端（已知一端的条件下）
     * @param known 已知的连接点
     * @return 另一端的连接点
     */
    IConnectionPoint getOtherEnd(IConnectionPoint known);
    
    /**
     * 是否为正端
     * @param point 连接点
     * @return 是否为正端
     */
    boolean isPositiveEnd(IConnectionPoint point);
    
    /**
     * 是否为内部连接
     * @return 是否为内部连接
     */
    boolean isInternal();
    
    /**
     * 获取长度
     * @return 连接长度
     */
    double getLength();
}
