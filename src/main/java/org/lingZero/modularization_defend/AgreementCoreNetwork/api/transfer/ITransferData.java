package org.lingZero.modularization_defend.AgreementCoreNetwork.api.transfer;

/**
 * 传输数据接口
 * 表示在网络中传输的数据
 * 
 * @author Modularization Defend Team
 */
public interface ITransferData {
    
    /**
     * 获取数据类型
     * @return 数据类型
     */
    DataType getType();
    
    /**
     * 获取数据量
     * @return 数据量
     */
    double getAmount();
    
    /**
     * 设置数据量
     * @param amount 数据量
     */
    void setAmount(double amount);
    
    /**
     * 数据类型枚举
     */
    enum DataType {
        ENERGY,      // 能量
        FLUID,       // 流体
        ITEM,        // 物品
        DATA         // 数据
    }
}
