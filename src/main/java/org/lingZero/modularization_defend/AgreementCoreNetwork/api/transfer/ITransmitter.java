package org.lingZero.modularization_defend.AgreementCoreNetwork.api.transfer;

import org.lingZero.modularization_defend.AgreementCoreNetwork.api.INetworkConnection;

/**
 * 传输器接口
 * 用于处理特定类型数据的传输逻辑
 * 
 * @param <T> 传输数据类型
 * @author Modularization Defend Team
 */
public interface ITransmitter<T extends ITransferData> {
    
    /**
     * 获取传输器类型
     * @return 数据类型
     */
    ITransferData.DataType getDataType();
    
    /**
     * 更新传输（每 tick 调用）
     * @param connections 连接列表
     */
    void update(java.util.List<INetworkConnection> connections);
    
    /**
     * 开始传输
     * @param connection 连接
     * @param data 数据
     */
    void startTransfer(INetworkConnection connection, T data);
    
    /**
     * 停止传输
     * @param connection 连接
     */
    void stopTransfer(INetworkConnection connection);
    
    /**
     * 获取当前传输速率
     * @param connection 连接
     * @return 传输速率
     */
    double getTransferRate(INetworkConnection connection);
}
