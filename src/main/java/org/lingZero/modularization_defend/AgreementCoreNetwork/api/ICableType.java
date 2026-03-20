package org.lingZero.modularization_defend.AgreementCoreNetwork.api;

/**
 * 线缆类型接口
 * 定义不同类型的传输线缆
 * 
 * @author Modularization Defend Team
 */
public interface ICableType {
    
    /**
     * 获取唯一名称
     * @return 唯一名称
     */
    String getUniqueName();
    
    /**
     * 获取类别（如电力、数据、流体等）
     * @return 类别名称
     */
    String getCategory();
    
    /**
     * 获取最大长度
     * @return 最大长度
     */
    int getMaxLength();
    
    /**
     * 获取松弛度（用于渲染）
     * @return 松弛度
     */
    double getSlack();
    
    /**
     * 获取渲染直径
     * @return 渲染直径
     */
    double getRenderDiameter();
    
    /**
     * 获取颜色
     * @param connection 连接
     * @return 颜色值
     */
    int getColour(INetworkConnection connection);
}
