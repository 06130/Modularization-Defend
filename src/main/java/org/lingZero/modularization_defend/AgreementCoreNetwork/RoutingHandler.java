package org.lingZero.modularization_defend.AgreementCoreNetwork;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

/**
 * 路由处理器
 * 负责处理数据包的接收、转发和路由决策
 */
public class RoutingHandler {
    
    // 单例实例
    private static RoutingHandler instance;
    
    // 路由数据库引用
    private final NetworkDatabase database;
    
    // 待发送的数据包队列（按优先级排序）
    private final PriorityBlockingQueue<RoutingPacket> packetQueue;
    
    // 已接收的数据包缓存（用于防止重复处理）
    private final Map<UUID, Long> receivedPackets;
    private static final int PACKET_CACHE_SIZE = 10000;
    private static final long PACKET_CACHE_EXPIRY = 60000; // 60 秒
    
    // 数据包处理器注册表
    private final Map<RoutingPacket.PacketType, Consumer<RoutingPacket>> packetHandlers;
    
    // 网络节点监听器
    private final Map<String, List<Consumer<RoutingPacket>>> nodeListeners;
    
    /**
     * 私有构造函数
     */
    private RoutingHandler() {
        this.database = NetworkDatabase.getOrCreate();
        this.packetQueue = new PriorityBlockingQueue<>(100, Comparator.comparingInt(RoutingPacket::getPriority));
        this.receivedPackets = new ConcurrentHashMap<>();
        this.packetHandlers = new EnumMap<>(RoutingPacket.PacketType.class);
        this.nodeListeners = new ConcurrentHashMap<>();
        
        // 注册内置处理器
        registerBuiltInHandlers();
    }
    
    /**
     * 获取或创建路由处理器实例
     */
    @NotNull
    public static RoutingHandler getOrCreate() {
        if (instance == null) {
            instance = new RoutingHandler();
        }
        return instance;
    }
    
    /**
     * 注册内置的数据包处理器
     */
    private void registerBuiltInHandlers() {
        // 路由发现请求处理器
        registerHandler(RoutingPacket.PacketType.ROUTE_REQUEST, this::handleRouteRequest);
        
        // 路由发现响应处理器
        registerHandler(RoutingPacket.PacketType.ROUTE_RESPONSE, this::handleRouteResponse);
        
        // 数据传输处理器
        registerHandler(RoutingPacket.PacketType.DATA_TRANSFER, this::handleDataTransfer);
        
        // 路由表更新处理器
        registerHandler(RoutingPacket.PacketType.ROUTING_UPDATE, this::handleRoutingUpdate);
    }
    
    /**
     * 注册数据包处理器
     */
    public void registerHandler(@NotNull RoutingPacket.PacketType type,
                               @NotNull Consumer<RoutingPacket> handler) {
        packetHandlers.put(type, handler);
    }
    
    /**
     * 注册节点监听器
     */
    public void registerNodeListener(@NotNull String nodeAddress,
                                    @NotNull Consumer<RoutingPacket> listener) {
        nodeListeners.computeIfAbsent(nodeAddress, k -> new ArrayList<>()).add(listener);
    }
    
    /**
     * 移除节点监听器
     */
    public void removeNodeListener(@NotNull String nodeAddress,
                                  @NotNull Consumer<RoutingPacket> listener) {
        List<Consumer<RoutingPacket>> listeners = nodeListeners.get(nodeAddress);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    
    /**
     * 接收数据包
     */
    public void receivePacket(@NotNull RoutingPacket packet) {
        // 检查是否已处理过（防止环路和重复）
        if (receivedPackets.containsKey(packet.getPacketId())) {
            return;
        }
        
        // 检查数据包是否过期
        if (packet.isExpired(PACKET_CACHE_EXPIRY)) {
            return;
        }
        
        // 检查是否形成路由环路
        String localAddress = database.getLocalAddress();
        if (localAddress != null && packet.hasLoop(localAddress)) {
            return;
        }
        
        // 添加到已接收缓存
        cachePacketId(packet.getPacketId());
        
        // 检查是否是发送给本节点的
        if (isPacketForLocal(packet)) {
            // 触发本地监听器
            notifyListeners(packet);
            
            // 处理数据包
            processPacket(packet);
        } else {
            // 转发数据包
            forwardPacket(packet);
        }
    }
    
    /**
     * 发送数据包
     */
    public void sendPacket(@NotNull RoutingPacket packet) {
        // 如果目标就是本地，直接处理
        if (isPacketForLocal(packet)) {
            processPacket(packet);
            return;
        }
        
        // 查找下一跳
        String nextHop = findNextHop(packet.getTargetAddress());
        if (nextHop == null) {
            // 没有找到路由，尝试路由发现
            initiateRouteDiscovery(packet.getTargetAddress());
            return;
        }
        
        // 更新下一跳并加入队列
        RoutingPacket packetWithNextHop = packet.copyWithNextHop(nextHop);
        packetQueue.offer(packetWithNextHop);
    }
    
    /**
     * 处理数据包队列（每 tick 调用）
     */
    public void tick() {
        // 处理优先级队列中的数据包（限制每 tick 处理数量，避免卡顿）
        int maxPacketsPerTick = 10;
        int processed = 0;
        
        while (processed < maxPacketsPerTick && !packetQueue.isEmpty()) {
            RoutingPacket packet = packetQueue.poll();
            if (packet != null) {
                sendToNextHop(packet);
                processed++;
            }
        }
        
        // 清理过期的已接收缓存
        cleanupReceivedCache();
    }
    
    /**
     * 处理路由发现请求
     */
    private void handleRouteRequest(@NotNull RoutingPacket packet) {
        String targetAddress = packet.getTargetAddress();
        String localAddress = database.getLocalAddress();
        
        // 如果本节点就是目标，发送响应
        if (targetAddress.equals(localAddress)) {
            RoutingPacket response = new RoutingPacket(
                RoutingPacket.PacketType.ROUTE_RESPONSE,
                localAddress,
                packet.getSourceAddress(),
                packet.getNextHop(),
                packet.getHopCount(),
                0, // 最高优先级
                null
            );
            sendPacket(response);
        } else {
            // 否则转发请求
            forwardPacket(packet);
        }
    }
    
    /**
     * 处理路由发现响应
     */
    private void handleRouteResponse(@NotNull RoutingPacket packet) {
        // 更新路由表
        String sourceAddress = packet.getSourceAddress();
        String nextHop = packet.getNextHop();
        int metric = packet.getHopCount();
        
        database.addRoute(sourceAddress, nextHop, metric);
        
        // 如果是发给本节点的响应，继续处理
        if (isPacketForLocal(packet)) {
            // 路由已建立，可以发送数据了
            notifyListeners(packet);
        } else {
            // 否则转发响应
            forwardPacket(packet);
        }
    }
    
    /**
     * 处理数据传输
     */
    private void handleDataTransfer(@NotNull RoutingPacket packet) {
        // 提取负载数据并处理
        var payload = packet.getPayload();
        if (payload != null) {
            // 这里可以根据具体业务逻辑处理数据
            // 例如：能量传输、物品传输、红石信号等
        }
        
        notifyListeners(packet);
    }
    
    /**
     * 处理路由表更新
     */
    private void handleRoutingUpdate(@NotNull RoutingPacket packet) {
        // 更新本地路由表
        var payload = packet.getPayload();
        if (payload != null) {
            // 解析并更新路由表
        }
        
        // 广播更新给其他节点
        forwardPacket(packet);
    }
    
    /**
     * 处理数据包（调用注册的处理器）
     */
    private void processPacket(@NotNull RoutingPacket packet) {
        Consumer<RoutingPacket> handler = packetHandlers.get(packet.getPacketType());
        if (handler != null) {
            handler.accept(packet);
        }
    }
    
    /**
     * 转发数据包
     */
    private void forwardPacket(@NotNull RoutingPacket packet) {
        String nextHop = findNextHop(packet.getTargetAddress());
        if (nextHop != null) {
            RoutingPacket forwardedPacket = packet.copyWithNextHop(nextHop);
            packetQueue.offer(forwardedPacket);
        }
    }
    
    /**
     * 发送数据包到下一跳
     */
    private void sendToNextHop(@NotNull RoutingPacket packet) {
        String nextHop = packet.getNextHop();
        
        // 获取下一跳的方块实体
        Level level = getLevelFromAddress(nextHop);
        net.minecraft.core.BlockPos pos = getPositionFromAddress(nextHop);
        
        if (level != null && pos != null) {
            // 这里应该通过网络协议发送到目标 BlockEntity
            // 具体实现取决于网络层的设计
            
            // 通知目标节点
            RoutingHandler targetHandler = getHandlerForNode(nextHop);
            if (targetHandler != null) {
                targetHandler.receivePacket(packet);
            }
        }
    }
    
    /**
     * 查找下一跳地址
     */
    @Nullable
    private String findNextHop(@NotNull String targetAddress) {
        // 首先查路由表
        String nextHop = database.getNextHop(targetAddress);
        if (nextHop != null) {
            return nextHop;
        }
        
        // 如果是直连节点，直接返回
        if (isDirectlyConnected(targetAddress)) {
            return targetAddress;
        }
        
        return null;
    }
    
    /**
     * 检查数据包是否是发送给本节点的
     */
    private boolean isPacketForLocal(@NotNull RoutingPacket packet) {
        String localAddress = database.getLocalAddress();
        return localAddress != null && 
               (packet.getTargetAddress().equals(localAddress) || 
                packet.getNextHop().equals(localAddress));
    }
    
    /**
     * 通知监听器
     */
    private void notifyListeners(@NotNull RoutingPacket packet) {
        String nextHop = packet.getNextHop();
        List<Consumer<RoutingPacket>> listeners = nodeListeners.get(nextHop);
        if (listeners != null) {
            for (Consumer<RoutingPacket> listener : listeners) {
                try {
                    listener.accept(packet);
                } catch (Exception e) {
                    // 记录错误但不影响其他监听器
                }
            }
        }
    }
    
    /**
     * 发起路由发现
     */
    private void initiateRouteDiscovery(@NotNull String targetAddress) {
        String localAddress = database.getLocalAddress();
        if (localAddress == null) return;
        
        // 创建路由发现请求
        RoutingPacket request = new RoutingPacket(
            RoutingPacket.PacketType.ROUTE_REQUEST,
            localAddress,
            targetAddress,
            localAddress, // 初始下一跳是自己
            0,
            3, // 中等优先级
            null
        );
        
        // 广播到所有直连节点
        broadcastToNeighbors(request);
    }
    
    /**
     * 广播到相邻节点
     */
    private void broadcastToNeighbors(@NotNull RoutingPacket packet) {
        // 获取所有直连节点
        List<String> neighbors = database.findRoutesVia(database.getLocalAddress());
        for (String neighbor : neighbors) {
            RoutingPacket neighborPacket = packet.copyWithNextHop(neighbor);
            packetQueue.offer(neighborPacket);
        }
    }
    
    /**
     * 缓存数据包 ID
     */
    private void cachePacketId(@NotNull UUID packetId) {
        receivedPackets.put(packetId, System.currentTimeMillis());
        
        // 限制缓存大小
        if (receivedPackets.size() > PACKET_CACHE_SIZE) {
            cleanupReceivedCache();
        }
    }
    
    /**
     * 清理过期的已接收缓存
     */
    private void cleanupReceivedCache() {
        long now = System.currentTimeMillis();
        receivedPackets.entrySet().removeIf(entry -> 
            now - entry.getValue() > PACKET_CACHE_EXPIRY
        );
    }
    
    /**
     * 检查是否是直连节点
     */
    private boolean isDirectlyConnected(@NotNull String address) {
        // 检查反向索引中是否有通过本地地址到达的路由
        String localAddress = database.getLocalAddress();
        if (localAddress == null) return false;
        
        List<String> routes = database.findRoutesVia(localAddress);
        return routes.contains(address);
    }
    
    /**
     * 从地址获取世界等级
     */
    @Nullable
    private Level getLevelFromAddress(@NotNull String address) {
        String[] parts = NetworkDatabase.parseAddress(address);
        if (parts == null) return null;
        
        String dimension = parts[1];
        // 根据维度名称获取对应的世界
        // 这里需要服务器引用，暂时返回 null
        return null;
    }
    
    /**
     * 从地址获取坐标
     */
    @Nullable
    private net.minecraft.core.BlockPos getPositionFromAddress(@NotNull String address) {
        return NetworkDatabase.parseBlockPos(address);
    }
    
    /**
     * 获取节点的路由处理器
     */
    @Nullable
    private RoutingHandler getHandlerForNode(@NotNull String address) {
        // 这需要根据实际的网络拓扑来实现
        // 暂时返回当前实例
        return this;
    }
}
