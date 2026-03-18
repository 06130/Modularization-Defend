package org.lingZero.modularization_defend.AgreementCoreNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 网络管理器
 * 管理网络节点的连接、状态和拓扑结构
 */
public class NetworkManager {
    
    // 单例实例
    private static NetworkManager instance;
    
    // 路由数据库引用
    private final NetworkDatabase database;
    
    // 路由处理器引用
    private final RoutingHandler routingHandler;
    
    // 活跃节点列表
    private final Map<String, NodeInfo> activeNodes;
    
    // 节点心跳超时时间（毫秒）
    private static final long HEARTBEAT_TIMEOUT = 30000; // 30 秒
    
    /**
     * 私有构造函数
     */
    private NetworkManager() {
        this.database = NetworkDatabase.getOrCreate();
        this.routingHandler = RoutingHandler.getOrCreate();
        this.activeNodes = new HashMap<>();
    }
    
    /**
     * 获取或创建网络管理器实例
     */
    @NotNull
    public static NetworkManager getOrCreate() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }
    
    /**
     * 注册网络节点
     */
    public void registerNode(@NotNull String nodeAddress,
                            @NotNull BlockPos position,
                            @NotNull Level level,
                            @NotNull BlockEntity blockEntity) {
        // 设置本地地址
        database.setLocalAddress(nodeAddress);
        
        // 添加到活跃节点列表
        NodeInfo nodeInfo = new NodeInfo(nodeAddress, position, level, blockEntity);
        activeNodes.put(nodeAddress, nodeInfo);
        
        // 在路由表中注册
        // 添加自环路由，metric 为 0
        database.addRoute(nodeAddress, nodeAddress, 0);
        
        // 广播节点上线消息
        broadcastNodeStatus(nodeAddress, true);
    }
    
    /**
     * 注销网络节点
     */
    public void unregisterNode(@NotNull String nodeAddress) {
        activeNodes.remove(nodeAddress);
        
        // 从路由表中移除
        database.removeRoute(nodeAddress);
        
        // 广播节点离线消息
        broadcastNodeStatus(nodeAddress, false);
    }
    
    /**
     * 更新节点状态
     */
    public void updateNodeStatus(@NotNull String nodeAddress, boolean isOnline) {
        NodeInfo nodeInfo = activeNodes.get(nodeAddress);
        if (nodeInfo != null) {
            nodeInfo.lastHeartbeat = System.currentTimeMillis();
            nodeInfo.isOnline = isOnline;
        }
    }
    
    /**
     * 获取节点信息
     */
    @Nullable
    public NodeInfo getNodeInfo(@NotNull String nodeAddress) {
        return activeNodes.get(nodeAddress);
    }
    
    /**
     * 获取所有活跃节点
     */
    @NotNull
    public Collection<NodeInfo> getActiveNodes() {
        return Collections.unmodifiableCollection(activeNodes.values());
    }
    
    /**
     * 检查节点是否在线
     */
    public boolean isNodeOnline(@NotNull String nodeAddress) {
        NodeInfo nodeInfo = activeNodes.get(nodeAddress);
        if (nodeInfo == null) return false;
        
        // 检查心跳是否超时
        long timeSinceHeartbeat = System.currentTimeMillis() - nodeInfo.lastHeartbeat;
        return nodeInfo.isOnline && timeSinceHeartbeat < HEARTBEAT_TIMEOUT;
    }
    
    /**
     * 发送心跳
     */
    public void sendHeartbeat() {
        String localAddress = database.getLocalAddress();
        if (localAddress == null) return;
        
        // 更新本地节点状态
        updateNodeStatus(localAddress, true);
        
        // 创建心跳数据包
        var heartbeatPacket = new RoutingPacket(
            RoutingPacket.PacketType.NODE_STATUS_QUERY,
            localAddress,
            "broadcast", // 广播地址
            localAddress,
            0,
            7, // 低优先级
            null
        );
        
        // 发送到所有邻居节点
        routingHandler.sendPacket(heartbeatPacket);
        
        // 检查超时节点
        checkNodeTimeouts();
    }
    
    /**
     * 检查节点超时
     */
    private void checkNodeTimeouts() {
        long now = System.currentTimeMillis();
        List<String> offlineNodes = new ArrayList<>();
        
        for (Map.Entry<String, NodeInfo> entry : activeNodes.entrySet()) {
            NodeInfo nodeInfo = entry.getValue();
            long timeSinceHeartbeat = now - nodeInfo.lastHeartbeat;
            
            if (timeSinceHeartbeat > HEARTBEAT_TIMEOUT && nodeInfo.isOnline) {
                // 节点超时，标记为离线
                nodeInfo.isOnline = false;
                offlineNodes.add(entry.getKey());
            }
        }
        
        // 移除超时节点的路由
        for (String nodeAddress : offlineNodes) {
            database.removeRoute(nodeAddress);
        }
    }
    
    /**
     * 获取网络拓扑信息
     */
    @NotNull
    public NetworkTopology getNetworkTopology() {
        NetworkTopology topology = new NetworkTopology();
        
        // 添加所有节点
        for (NodeInfo nodeInfo : activeNodes.values()) {
            topology.addNode(nodeInfo.address, nodeInfo.position);
        }
        
        // 添加所有连接
        Set<String> targets = database.getAllTargets();
        for (String target : targets) {
            String nextHop = database.getNextHop(target);
            if (nextHop != null) {
                topology.addConnection(database.getLocalAddress(), nextHop);
            }
        }
        
        return topology;
    }
    
    /**
     * 计算两个节点之间的最短路径
     */
    @NotNull
    public List<String> findPath(@NotNull String source,
                                 @NotNull String target) {
        List<String> path = new ArrayList<>();
        
        if (source.equals(target)) {
            path.add(source);
            return path;
        }
        
        // 使用 BFS 查找路径
        Queue<String> queue = new LinkedList<>();
        Map<String, String> cameFrom = new HashMap<>();
        
        queue.offer(source);
        cameFrom.put(source, null);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            if (current.equals(target)) {
                // 重建路径
                path.add(target);
                String node = cameFrom.get(target);
                while (node != null) {
                    path.add(0, node);
                    node = cameFrom.get(node);
                }
                break;
            }
            
            // 获取邻居节点
            List<String> neighbors = database.findRoutesVia(current);
            for (String neighbor : neighbors) {
                if (!cameFrom.containsKey(neighbor)) {
                    queue.offer(neighbor);
                    cameFrom.put(neighbor, current);
                }
            }
        }
        
        return path;
    }
    
    /**
     * 获取网络直径（最长最短路径）
     */
    public int getNetworkDiameter() {
        int maxDistance = 0;
        
        for (String source : activeNodes.keySet()) {
            for (String target : activeNodes.keySet()) {
                if (!source.equals(target)) {
                    List<String> path = findPath(source, target);
                    if (!path.isEmpty()) {
                        maxDistance = Math.max(maxDistance, path.size() - 1);
                    }
                }
            }
        }
        
        return maxDistance;
    }
    
    /**
     * 获取网络的连通分量数量
     */
    public int getConnectedComponentCount() {
        Set<String> visited = new HashSet<>();
        int components = 0;
        
        for (String node : activeNodes.keySet()) {
            if (!visited.contains(node)) {
                // BFS 遍历这个连通分量
                Queue<String> queue = new LinkedList<>();
                queue.offer(node);
                visited.add(node);
                
                while (!queue.isEmpty()) {
                    String current = queue.poll();
                    List<String> neighbors = database.findRoutesVia(current);
                    
                    for (String neighbor : neighbors) {
                        if (!visited.contains(neighbor)) {
                            queue.offer(neighbor);
                            visited.add(neighbor);
                        }
                    }
                }
                
                components++;
            }
        }
        
        return components;
    }
    
    /**
     * 广播节点状态
     */
    private void broadcastNodeStatus(@NotNull String nodeAddress, boolean isOnline) {
        String localAddress = database.getLocalAddress();
        if (localAddress == null) return;
        
        var payload = new net.minecraft.nbt.CompoundTag();
        payload.putString("NodeAddress", nodeAddress);
        payload.putBoolean("IsOnline", isOnline);
        
        var statusPacket = new RoutingPacket(
            RoutingPacket.PacketType.NODE_STATUS_RESPONSE,
            localAddress,
            "broadcast",
            localAddress,
            0,
            5,
            payload
        );
        
        routingHandler.sendPacket(statusPacket);
    }
    
    /**
     * 节点信息类
     */
    public static class NodeInfo {
        public final String address;
        public final BlockPos position;
        public final Level level;
        public final BlockEntity blockEntity;
        public boolean isOnline;
        public long lastHeartbeat;
        
        public NodeInfo(@NotNull String address,
                       @NotNull BlockPos position,
                       @NotNull Level level,
                       @NotNull BlockEntity blockEntity) {
            this.address = address;
            this.position = position;
            this.level = level;
            this.blockEntity = blockEntity;
            this.isOnline = true;
            this.lastHeartbeat = System.currentTimeMillis();
        }
        
        /**
         * 获取节点类型
         */
        @NotNull
        public String getNodeType() {
            String[] parts = NetworkDatabase.parseAddress(address);
            return parts != null ? parts[2] : "unknown";
        }
        
        /**
         * 获取网络 ID
         */
        @NotNull
        public String getNetworkId() {
            String[] parts = NetworkDatabase.parseAddress(address);
            return parts != null ? parts[0] : "unknown";
        }
        
        /**
         * 获取维度
         */
        @NotNull
        public String getDimension() {
            String[] parts = NetworkDatabase.parseAddress(address);
            return parts != null ? parts[1] : "unknown";
        }
    }
    
    /**
     * 网络拓扑类
     */
    public static class NetworkTopology {
        private final Map<String, BlockPos> nodes;
        private final Map<String, Set<String>> connections;
        
        public NetworkTopology() {
            this.nodes = new HashMap<>();
            this.connections = new HashMap<>();
        }
        
        public void addNode(@NotNull String address, @NotNull BlockPos position) {
            nodes.put(address, position);
        }
        
        public void addConnection(@NotNull String node1, @NotNull String node2) {
            connections.computeIfAbsent(node1, k -> new HashSet<>()).add(node2);
            connections.computeIfAbsent(node2, k -> new HashSet<>()).add(node1);
        }
        
        @NotNull
        public Map<String, BlockPos> getNodes() {
            return Collections.unmodifiableMap(nodes);
        }
        
        @NotNull
        public Map<String, Set<String>> getConnections() {
            return Collections.unmodifiableMap(connections);
        }
        
        @NotNull
        public Set<String> getNeighbors(@NotNull String node) {
            Set<String> neighbors = connections.get(node);
            return neighbors != null ? neighbors : Collections.emptySet();
        }
    }
}
