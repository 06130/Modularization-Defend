package org.lingZero.modularization_defend.EnergyNetwork;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 能源网络全局管理器
 * 单例模式，管理世界中所有的能源网络
 * 
 * 功能：
 * - 创建和销毁网络
 * - 按坐标查找网络
 * - 定期扫描所有网络
 */
public class EnergyNetworkManager {
    
    // 单例实例（使用 volatile 保证多线程可见性）
    private static volatile EnergyNetworkManager instance;
    
    // 存储所有网络（使用 ConcurrentHashMap 支持并发访问）
    private final Map<String, EnergyNetwork> networks;
    
    // 位置到网络的映射（用于快速查找）
    private final Map<BlockPos, EnergyNetwork> positionToNetwork;
    
    // 无效网络集合（待清理）
    private final Set<String> invalidNetworks;
    
    // 当前等级（客户端/服务器各一个实例）
    private Level currentLevel;
    
    /**
     * 私有构造函数（单例模式）
     */
    private EnergyNetworkManager() {
        this.networks = new ConcurrentHashMap<>();
        this.positionToNetwork = new ConcurrentHashMap<>();
        this.invalidNetworks = new ObjectOpenHashSet<>();
    }
    
    /**
     * 获取单例实例（懒加载 + 双重检查锁）
     * @return 管理器实例
     */
    public static EnergyNetworkManager getInstance() {
        if (instance == null) {
            synchronized (EnergyNetworkManager.class) {
                if (instance == null) {
                    instance = new EnergyNetworkManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 设置当前等级（在玩家进入世界时调用）
     * @param level 等级对象
     */
    public void setCurrentLevel(Level level) {
        this.currentLevel = level;
    }
    
    /**
     * 获取当前等级
     * @return 等级对象
     */
    @Nullable
    public Level getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * 创建新的能源网络
     * @param protocolCorePos 协议核心位置
     * @return 新创建的网络
     */
    public EnergyNetwork createNetwork(BlockPos protocolCorePos) {
        String networkId = generateNetworkId(protocolCorePos);
        
        // 检查是否已存在
        if (networks.containsKey(networkId)) {
            return networks.get(networkId);
        }
        
        EnergyNetwork network = new EnergyNetwork(networkId);
        networks.put(networkId, network);
        
        return network;
    }
    
    /**
     * 注册网络到管理器
     * @param network 要注册的网络
     */
    public void registerNetwork(EnergyNetwork network) {
        if (network == null) {
            throw new IllegalArgumentException("网络不能为空");
        }
        
        networks.put(network.getNetworkId(), network);
        
        // 注册所有节点的位置映射
        for (IEnergyNode node : network.getAllNodes()) {
            positionToNetwork.put(node.getNodePosition(), network);
        }
    }
    
    /**
     * 注销网络
     * @param networkId 网络 ID
     */
    public void unregisterNetwork(String networkId) {
        EnergyNetwork network = networks.remove(networkId);
        if (network != null) {
            // 清除位置映射
            for (IEnergyNode node : network.getAllNodes()) {
                positionToNetwork.remove(node.getNodePosition());
            }
            
            // 清空网络
            network.clearNetwork();
        }
    }
    
    /**
     * 根据位置查找网络
     * @param pos 方块坐标
     * @return 找到的网络，未找到返回 null
     */
    @Nullable
    public EnergyNetwork getNetworkByPosition(BlockPos pos) {
        return positionToNetwork.get(pos);
    }
    
    /**
     * 根据网络 ID 查找网络
     * @param networkId 网络 ID
     * @return 找到的网络，未找到返回 null
     */
    @Nullable
    public EnergyNetwork getNetworkById(String networkId) {
        return networks.get(networkId);
    }
    
    /**
     * 获取所有网络
     * @return 网络集合（只读）
     */
    public Set<EnergyNetwork> getAllNetworks() {
        return Set.copyOf(networks.values());
    }
    
    /**
     * 扫描所有网络（每 5 秒调用一次）
     * 应在服务器 tick 中调用
     */
    public void scanAllNetworks() {
        long currentTime = System.currentTimeMillis();
        
        for (EnergyNetwork network : networks.values()) {
            try {
                // 先验证网络有效性
                if (!network.isValid()) {
                    network.validate();
                }
                
                // 执行扫描
                if (network.isValid()) {
                    network.scanNetwork();
                } else {
                    // 标记为无效，等待清理
                    invalidNetworks.add(network.getNetworkId());
                }
            } catch (Exception e) {
                // 记录错误但不影响其他网络
                e.printStackTrace();
            }
        }
        
        // 清理无效网络
        cleanupInvalidNetworks();
    }
    
    /**
     * 清理无效网络
     */
    private void cleanupInvalidNetworks() {
        for (String networkId : invalidNetworks) {
            unregisterNetwork(networkId);
        }
        invalidNetworks.clear();
    }
    
    /**
     * 将节点添加到网络（自动查找或创建网络）
     * @param node 要添加的节点
     * @return 是否添加成功
     */
    public boolean addNodeToNetwork(IEnergyNode node) {
        if (node == null || !node.isValid()) {
            return false;
        }
        
        BlockPos pos = node.getNodePosition();
        EnergyNetwork existingNetwork = getNetworkByPosition(pos);
        
        if (existingNetwork != null) {
            // 添加到现有网络
            return existingNetwork.addNode(node);
        } else if (node.getNodeType() == IEnergyNode.NodeType.PROTOCOL_CORE) {
            // 如果是协议核心，创建新网络
            EnergyNetwork newNetwork = createNetwork(pos);
            newNetwork.setProtocolCore(node);
            registerNetwork(newNetwork);
            return true;
        }
        
        // 其他设备且不在网络中，不处理
        return false;
    }
    
    /**
     * 从网络移除节点
     * @param node 要移除的节点
     * @return 是否移除成功
     */
    public boolean removeNodeFromNetwork(IEnergyNode node) {
        if (node == null) {
            return false;
        }
        
        EnergyNetwork network = getNetworkByPosition(node.getNodePosition());
        if (network != null) {
            boolean removed = network.removeNode(node);
            
            // 如果网络为空，删除网络
            if (network.getNodeCount() == 0) {
                unregisterNetwork(network.getNetworkId());
            }
            
            return removed;
        }
        
        return false;
    }
    
    /**
     * 连接两个节点（自动处理网络合并）
     * @param node1 第一个节点
     * @param node2 第二个节点
     * @return 是否连接成功
     */
    public boolean connectNodes(IEnergyNode node1, IEnergyNode node2) {
        if (node1 == null || node2 == null) {
            return false;
        }
        
        EnergyNetwork network1 = getNetworkByPosition(node1.getNodePosition());
        EnergyNetwork network2 = getNetworkByPosition(node2.getNodePosition());
        
        if (network1 == null && network2 == null) {
            // 两个都不在网络中，不处理
            return false;
        } else if (network1 != null && network2 == null) {
            // node2 加入 node1 的网络
            network1.addNode(node2);
            network1.connectNodes(node1, node2);
            return true;
        } else if (network1 == null && network2 != null) {
            // node1 加入 node2 的网络
            network2.addNode(node1);
            network2.connectNodes(node2, node1);
            return true;
        } else if (network1 != null && network2 != null) {
            // 两个都在网络中
            if (network1 == network2) {
                // 同一网络，直接连接
                network1.connectNodes(node1, node2);
                return true;
            } else {
                // 不同网络，需要合并
                mergeNetworks(network1, network2);
                network1.connectNodes(node1, node2);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 合并两个网络
     * @param network1 第一个网络
     * @param network2 第二个网络
     */
    private void mergeNetworks(EnergyNetwork network1, EnergyNetwork network2) {
        if (network1 == network2) {
            return;
        }
        
        // 将 network2 的所有节点移到 network1
        for (IEnergyNode node : network2.getAllNodes()) {
            network1.addNode(node);
            positionToNetwork.put(node.getNodePosition(), network1);
        }
        
        // 删除 network2
        unregisterNetwork(network2.getNetworkId());
    }
    
    /**
     * 生成网络唯一 ID
     * @param corePos 协议核心位置
     * @return 网络 ID
     */
    private String generateNetworkId(BlockPos corePos) {
        return "network_" + corePos.getX() + "_" + corePos.getY() + "_" + corePos.getZ();
    }
    
    /**
     * 获取网络数量
     * @return 网络总数
     */
    public int getNetworkCount() {
        return networks.size();
    }
    
    /**
     * 获取所有节点的总能量
     * @return 总能量值
     */
    public long getTotalEnergyInAllNetworks() {
        long total = 0;
        for (EnergyNetwork network : networks.values()) {
            total += network.getTotalNetworkEnergy();
        }
        return total;
    }
}
