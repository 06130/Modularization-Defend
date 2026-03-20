package org.lingZero.modularization_defend.AgreementCoreNetwork.network;

import org.lingZero.modularization_defend.AgreementCoreNetwork.api.INetworkConnection;

/**
 * 网络同步管理器
 * 负责网络数据的客户端同步
 * 
 * @author Modularization Defend Team
 */
public class NetworkSyncManager {
    private final GlobalNetwork globalNetwork;
    
    public NetworkSyncManager(GlobalNetwork globalNetwork) {
        this.globalNetwork = globalNetwork;
    }
    
    /**
     * 连接添加时调用
     */
    public void onConnectionAdded(INetworkConnection conn) {
        // TODO: 发送到客户端
        // 将来在这里实现网络包同步逻辑
    }
    
    /**
     * 连接移除时调用
     */
    public void onConnectionRemoved(INetworkConnection conn) {
        // TODO: 发送到客户端
    }
    
    /**
     * 连接端点变化时调用
     */
    public void onConnectionEndpointsChanged(INetworkConnection conn) {
        // TODO: 发送到客户端
    }
}
