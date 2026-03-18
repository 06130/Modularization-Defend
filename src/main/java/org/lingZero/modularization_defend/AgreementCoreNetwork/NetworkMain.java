package org.lingZero.modularization_defend.AgreementCoreNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * AgreementCore 网络路由系统入口
 * 提供静态方法方便访问路由功能
 */
public class NetworkMain {
    
    // 路由数据库
    private static NetworkDatabase database;
    
    // 路由处理器
    private static RoutingHandler routingHandler;
    
    // 网络管理器
    private static NetworkManager networkManager;
    
    /**
     * 初始化路由系统（服务器启动时调用）
     */
    public static void initialize() {
        database = NetworkDatabase.getOrCreate();
        routingHandler = RoutingHandler.getOrCreate();
        networkManager = NetworkManager.getOrCreate();
    }
    
    /**
     * 获取路由数据库
     */
    @NotNull
    public static NetworkDatabase getDatabase() {
        if (database == null) {
            database = NetworkDatabase.getOrCreate();
        }
        return database;
    }
    
    /**
     * 获取路由处理器
     */
    @NotNull
    public static RoutingHandler getRoutingHandler() {
        if (routingHandler == null) {
            routingHandler = RoutingHandler.getOrCreate();
        }
        return routingHandler;
    }
    
    /**
     * 获取网络管理器
     */
    @NotNull
    public static NetworkManager getNetworkManager() {
        if (networkManager == null) {
            networkManager = NetworkManager.getOrCreate();
        }
        return networkManager;
    }
    
    /**
     * 注册网络节点
     * @param nodeAddress 节点地址
     * @param blockEntity 节点方块实体
     */
    public static void registerNode(@NotNull String nodeAddress,
                                   @NotNull BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        Level level = blockEntity.getLevel();
        if (level != null) {
            networkManager.registerNode(nodeAddress, pos, level, blockEntity);
        }
    }
    
    /**
     * 注销网络节点
     */
    public static void unregisterNode(@NotNull String nodeAddress) {
        networkManager.unregisterNode(nodeAddress);
    }
    
    /**
     * 构建节点地址
     * @param networkId 网络 ID（通常是 modid）
     * @param structureId 结构 ID（如 "core_001"）
     * @param pos 方块坐标
     * @return 完整的节点地址
     */
    @NotNull
    public static String buildNodeAddress(@NotNull String networkId,
                                         @NotNull String structureId,
                                         @NotNull BlockPos pos) {
        return NetworkDatabase.buildAddress(
            networkId,
            getDimensionName(pos),
            structureId,
            pos
        );
    }
    
    /**
     * 获取维度名称
     */
    @NotNull
    private static String getDimensionName(@NotNull BlockPos pos) {
        // 简化实现，默认返回主世界
        // 实际应该根据维度资源位置返回
        return "minecraft:overworld";
    }
    
    /**
     * 发送 Tick（每 tick 调用）
     */
    public static void tick() {
        if (routingHandler != null) {
            routingHandler.tick();
        }
        if (networkManager != null) {
            // 定期发送心跳
            networkManager.sendHeartbeat();
        }
    }
    
    /**
     * 获取本地节点地址
     */
    @NotNull
    public static String getLocalAddress() {
        String address = database != null ? database.getLocalAddress() : null;
        return address != null ? address : "";
    }
}
