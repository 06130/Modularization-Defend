package org.lingZero.modularization_defend.EnergyNetwork;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 能源网络核心类
 * 使用邻接表存储图结构，支持环路和大规模网络
 * 
 * 性能优化：
 * - 使用 Object* 集合减少装箱
 * - 增量更新机制
 * - 懒加载邻居列表
 */
public class EnergyNetwork {
    
    // 网络唯一 ID
    private final String networkId;
    
    // 协议核心节点（网络中心）
    private IEnergyNode protocolCore;
    
    // 所有节点集合（使用高性能集合）
    private final Set<IEnergyNode> allNodes;
    
    // 邻接表存储图结构（节点 -> 邻居列表）
    private final java.util.Map<IEnergyNode, List<IEnergyNode>> adjacencyList;
    
    // 网络是否有效（用于标记是否需要重建）
    private final AtomicBoolean isValid;
    
    // 最后扫描时间戳
    private long lastScanTime;
    
    // 扫描间隔（5 秒 = 5000ms）
    private static final long SCAN_INTERVAL = 5000L;
    
    // 网络总能量统计
    private volatile long totalNetworkEnergy;
    
    // 网络总容量统计
    private volatile long totalNetworkCapacity;
    
    /**
     * 创建能源网络
     * @param networkId 网络唯一标识
     */
    public EnergyNetwork(String networkId) {
        this.networkId = networkId;
        this.allNodes = new ObjectOpenHashSet<>();
        this.adjacencyList = new java.util.HashMap<>();
        this.isValid = new AtomicBoolean(true);
        this.lastScanTime = System.currentTimeMillis();
        this.totalNetworkEnergy = 0;
        this.totalNetworkCapacity = 0;
    }
    
    /**
     * 设置协议核心节点
     * @param core 协议核心
     */
    public void setProtocolCore(IEnergyNode core) {
        if (core == null || core.getNodeType() != IEnergyNode.NodeType.PROTOCOL_CORE) {
            throw new IllegalArgumentException("必须设置协议核心节点");
        }
        this.protocolCore = core;
        addNode(core);
    }
    
    /**
     * 添加节点到网络
     * @param node 要添加的节点
     * @return 是否添加成功
     */
    public boolean addNode(IEnergyNode node) {
        if (node == null || !node.isValid()) {
            return false;
        }
        
        if (allNodes.add(node)) {
            // 初始化邻居列表
            adjacencyList.putIfAbsent(node, new ObjectArrayList<>());
            
            // 更新统计数据
            updateStatistics();
            
            // 标记网络需要重新验证
            markDirty();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 从网络移除节点
     * @param node 要移除的节点
     * @return 是否移除成功
     */
    public boolean removeNode(IEnergyNode node) {
        if (node == null) {
            return false;
        }
        
        // 从所有节点集合移除
        if (allNodes.remove(node)) {
            // 从邻接表移除
            adjacencyList.remove(node);
            
            // 从其他节点的邻居列表中移除
            for (List<IEnergyNode> neighbors : adjacencyList.values()) {
                neighbors.remove(node);
            }
            
            // 如果移除的是协议核心，清空网络
            if (node.equals(protocolCore)) {
                this.protocolCore = null;
                clearNetwork();
            }
            
            // 更新统计数据
            updateStatistics();
            
            // 标记网络需要重新验证
            markDirty();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 连接两个节点（建立边）
     * @param from 源节点
     * @param to 目标节点
     * @return 是否连接成功
     */
    public boolean connectNodes(IEnergyNode from, IEnergyNode to) {
        if (from == null || to == null || !allNodes.contains(from) || !allNodes.contains(to)) {
            return false;
        }
        
        List<IEnergyNode> neighbors = adjacencyList.computeIfAbsent(from, k -> new ObjectArrayList<>());
        
        if (!neighbors.contains(to)) {
            neighbors.add(to);
            markDirty();
            return true;
        }
        
        return false;
    }
    
    /**
     * 断开两个节点的连接
     * @param from 源节点
     * @param to 目标节点
     * @return 是否断开成功
     */
    public boolean disconnectNodes(IEnergyNode from, IEnergyNode to) {
        if (from == null) {
            return false;
        }
        
        List<IEnergyNode> neighbors = adjacencyList.get(from);
        if (neighbors != null) {
            if (neighbors.remove(to)) {
                markDirty();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取节点的所有邻居
     * @param node 节点
     * @return 邻居列表（只读）
     */
    public List<IEnergyNode> getNeighbors(IEnergyNode node) {
        List<IEnergyNode> neighbors = adjacencyList.get(node);
        return neighbors != null ? neighbors : List.of();
    }
    
    /**
     * 获取网络中所有节点
     * @return 节点集合（只读）
     */
    public Set<IEnergyNode> getAllNodes() {
        return Set.copyOf(allNodes);
    }
    
    /**
     * 获取协议核心
     * @return 协议核心节点
     */
    @Nullable
    public IEnergyNode getProtocolCore() {
        return protocolCore;
    }
    
    /**
     * 执行网络扫描（BFS 遍历）
     * 每 5 秒调用一次，填满所有设备的缓冲区
     */
    public void scanNetwork() {
        if (!isValid.get() || protocolCore == null) {
            return;
        }
        
        // 检查是否需要重新扫描
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime < SCAN_INTERVAL) {
            return;
        }
        
        // BFS 遍历网络
        Set<IEnergyNode> visited = new ObjectOpenHashSet<>();
        List<IEnergyNode> queue = new ObjectArrayList<>();
        
        queue.add(protocolCore);
        visited.add(protocolCore);
        
        int headIndex = 0;
        while (headIndex < queue.size()) {
            IEnergyNode current = queue.get(headIndex++);
            
            // 调用节点的扫描回调（填满缓冲区）
            current.onNetworkScan();
            
            // 遍历邻居
            for (IEnergyNode neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    
                    // 确保反向连接也存在（支持环路）
                    connectNodes(neighbor, current);
                }
            }
        }
        
        // 更新扫描时间
        lastScanTime = currentTime;
        
        // 更新统计数据
        updateStatistics();
    }
    
    /**
     * 更新网络统计数据
     */
    private void updateStatistics() {
        long totalEnergy = 0;
        long totalCapacity = 0;
        
        for (IEnergyNode node : allNodes) {
            EnergyBuffer buffer = node.getEnergyBuffer();
            if (buffer != null) {
                totalEnergy += buffer.getEnergy();
                totalCapacity += buffer.getMaxCapacity();
            }
        }
        
        this.totalNetworkEnergy = totalEnergy;
        this.totalNetworkCapacity = totalCapacity;
    }
    
    /**
     * 标记网络为脏数据（需要重建）
     */
    public void markDirty() {
        isValid.set(false);
    }
    
    /**
     * 验证网络有效性
     * @return true 如果网络有效
     */
    public boolean validate() {
        if (protocolCore == null || !protocolCore.isValid()) {
            isValid.set(false);
            return false;
        }
        
        // 检查所有节点是否仍然有效
        List<IEnergyNode> invalidNodes = new ObjectArrayList<>();
        for (IEnergyNode node : allNodes) {
            if (!node.isValid()) {
                invalidNodes.add(node);
            }
        }
        
        // 移除无效节点
        for (IEnergyNode node : invalidNodes) {
            removeNode(node);
        }
        
        isValid.set(true);
        return true;
    }
    
    /**
     * 清空网络
     */
    public void clearNetwork() {
        allNodes.clear();
        adjacencyList.clear();
        protocolCore = null;
        totalNetworkEnergy = 0;
        totalNetworkCapacity = 0;
        isValid.set(true);
    }
    
    /**
     * 获取网络 ID
     * @return 网络唯一标识
     */
    public String getNetworkId() {
        return networkId;
    }
    
    /**
     * 检查网络是否有效
     * @return true 如果网络有效且可以扫描
     */
    public boolean isValid() {
        return isValid.get() && protocolCore != null;
    }
    
    /**
     * 获取最后扫描时间
     * @return 时间戳（毫秒）
     */
    public long getLastScanTime() {
        return lastScanTime;
    }
    
    /**
     * 获取网络总能量
     * @return 总能量值
     */
    public long getTotalNetworkEnergy() {
        return totalNetworkEnergy;
    }
    
    /**
     * 获取网络总容量
     * @return 总容量值
     */
    public long getTotalNetworkCapacity() {
        return totalNetworkCapacity;
    }
    
    /**
     * 获取网络填充率
     * @return 填充率（0-1）
     */
    public double getNetworkFillRatio() {
        if (totalNetworkCapacity == 0) return 0.0;
        return (double) totalNetworkEnergy / totalNetworkCapacity;
    }
    
    /**
     * 获取节点数量
     * @return 节点总数
     */
    public int getNodeCount() {
        return allNodes.size();
    }
    
    /**
     * 检查是否包含节点
     * @param node 节点
     * @return true 如果包含
     */
    public boolean containsNode(IEnergyNode node) {
        return allNodes.contains(node);
    }
    
    /**
     * 查找指定位置的节点
     * @param pos 方块坐标
     * @return 找到的节点，未找到返回 null
     */
    @Nullable
    public IEnergyNode findNodeAt(BlockPos pos) {
        for (IEnergyNode node : allNodes) {
            if (node.getNodePosition().equals(pos)) {
                return node;
            }
        }
        return null;
    }
}
