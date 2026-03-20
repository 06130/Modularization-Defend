package org.lingZero.modularization_defend.AgreementCoreNetwork;

import net.minecraft.world.level.Level;
import org.lingZero.modularization_defend.AgreementCoreNetwork.network.GlobalNetwork;
import org.lingZero.modularization_defend.AgreementCoreNetwork.network.LocalNetwork;
import org.lingZero.modularization_defend.AgreementCoreNetwork.transfer.energy.EnergyTransmitter;

/**
 * 网络管理器
 * 提供访问和管理网络的便捷方法
 * 
 * @author Modularization Defend Team
 */
public class NetworkManager {
    
    /**
     * 获取或创建全局网络
     * @param level 世界
     * @return 全局网络实例
     */
    public static GlobalNetwork getOrCreateNetwork(Level level) {
        if (level == null || level.isClientSide()) {
            return null;
        }
        
        // TODO: 从数据存储中获取或创建网络
        // 可以使用 Level.getData() 或自定义的存储机制
        return new GlobalNetwork();
    }
    
    /**
     * 注册能量传输器到网络
     * @param network 网络
     * @param level 世界
     */
    public static void registerEnergyTransmitter(GlobalNetwork network, Level level) {
        // 为所有局部网络注册能量传输器
        for (LocalNetwork localNet : network.getLocalNets()) {
            localNet.registerTransmitter(
                org.lingZero.modularization_defend.AgreementCoreNetwork.api.transfer.ITransferData.DataType.ENERGY,
                new EnergyTransmitter(level)
            );
        }
    }
}
