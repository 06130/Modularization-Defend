package org.lingZero.modularization_defend.EnergyNetwork;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 连接检测器
 * 用于检测和建立能源设备间的连接
 * 
 * 功能：
 * - 检测相邻设备
 * - 检测指定范围内的设备
 * - 自动建立连接
 */
public class ConnectionDetector {
    
    private static final Logger LOGGER = LogManager.getLogger("EnergyNetwork.Connection");
    
    // 默认检测范围（方块数）
    private static final int DEFAULT_RANGE = 1;
    
    /**
     * 检测并连接相邻的能源设备
     * @param node 起始节点
     * @return 成功连接的节点数量
     */
    public int detectAndConnectAdjacentNodes(IEnergyNode node) {
        if (node == null || !node.isValid()) {
            return 0;
        }
        
        Level level = node.getLevel();
        BlockPos pos = node.getNodePosition();
        int connectedCount = 0;
        
        // 检查六个方向
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            IEnergyNode neighbor = getEnergyNodeAt(level, neighborPos);
            
            if (neighbor != null && neighbor.isValid()) {
                // 尝试建立连接
                if (connectNodes(node, neighbor)) {
                    connectedCount++;
                }
            }
        }
        
        if (LOGGER.isDebugEnabled() && connectedCount > 0) {
            LOGGER.debug("在 {} 检测到 {} 个相邻设备", pos, connectedCount);
        }
        
        return connectedCount;
    }
    
    /**
     * 检测并连接指定范围内的所有能源设备
     * @param node 起始节点
     * @param range 检测范围（半径）
     * @return 成功连接的节点数量
     */
    public int detectAndConnectInRange(IEnergyNode node, int range) {
        if (node == null || !node.isValid()) {
            return 0;
        }
        
        Level level = node.getLevel();
        BlockPos centerPos = node.getNodePosition();
        int connectedCount = 0;
        
        // 遍历立方体区域
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = centerPos.offset(x, y, z);
                    
                    // 跳过中心位置
                    if (checkPos.equals(centerPos)) {
                        continue;
                    }
                    
                    // 检查距离（球形范围）
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance > range) {
                        continue;
                    }
                    
                    IEnergyNode neighbor = getEnergyNodeAt(level, checkPos);
                    if (neighbor != null && neighbor.isValid()) {
                        if (connectNodes(node, neighbor)) {
                            connectedCount++;
                        }
                    }
                }
            }
        }
        
        if (LOGGER.isDebugEnabled() && connectedCount > 0) {
            LOGGER.debug("在范围 {} 内检测到 {} 个设备", range, connectedCount);
        }
        
        return connectedCount;
    }
    
    /**
     * 获取指定位置的能源节点
     * @param level 等级
     * @param pos 位置
     * @return 找到的节点，未找到返回 null
     */
    @Nullable
    private IEnergyNode getEnergyNodeAt(Level level, BlockPos pos) {
        if (level == null || !level.isLoaded(pos)) {
            return null;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        // 检查是否实现了 IEnergyNode 接口
        if (blockEntity instanceof IEnergyNode) {
            return (IEnergyNode) blockEntity;
        }
        
        return null;
    }
    
    /**
     * 连接两个节点
     * @param node1 第一个节点
     * @param node2 第二个节点
     * @return 是否连接成功
     */
    private boolean connectNodes(IEnergyNode node1, IEnergyNode node2) {
        EnergyNetwork network = EnergyNetworkManager.getInstance().getNetworkByPosition(node1.getNodePosition());
        
        if (network == null) {
            // 如果 node1 不在网络中，尝试将其加入
            EnergyNetworkManager.getInstance().addNodeToNetwork(node1);
            network = EnergyNetworkManager.getInstance().getNetworkByPosition(node1.getNodePosition());
        }
        
        if (network != null) {
            // 将 node2 加入网络
            network.addNode(node2);
            
            // 建立双向连接
            network.connectNodes(node1, node2);
            network.connectNodes(node2, node1);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 断开与相邻节点的连接
     * @param node 要断开的节点
     * @return 断开的连接数量
     */
    public int disconnectAdjacentNodes(IEnergyNode node) {
        if (node == null) {
            return 0;
        }
        
        EnergyNetwork network = EnergyNetworkManager.getInstance().getNetworkByPosition(node.getNodePosition());
        if (network == null) {
            return 0;
        }
        
        int disconnectedCount = 0;
        BlockPos pos = node.getNodePosition();
        
        // 检查六个方向
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            IEnergyNode neighbor = network.findNodeAt(neighborPos);
            
            if (neighbor != null) {
                network.disconnectNodes(node, neighbor);
                network.disconnectNodes(neighbor, node);
                disconnectedCount++;
            }
        }
        
        if (LOGGER.isDebugEnabled() && disconnectedCount > 0) {
            LOGGER.debug("断开了 {} 个相邻连接", disconnectedCount);
        }
        
        return disconnectedCount;
    }
    
    /**
     * 查找指定类型的最近设备
     * @param startPos 起始位置
     * @param level 等级
     * @param targetType 目标类型
     * @param maxRange 最大范围
     * @return 找到的最近节点
     */
    @Nullable
    public IEnergyNode findNearestNodeOfType(BlockPos startPos, Level level, 
                                              IEnergyNode.NodeType targetType, int maxRange) {
        if (level == null || startPos == null) {
            return null;
        }
        
        double minDistance = Double.MAX_VALUE;
        IEnergyNode nearestNode = null;
        
        // 遍历周围区域
        for (int x = -maxRange; x <= maxRange; x++) {
            for (int y = -maxRange; y <= maxRange; y++) {
                for (int z = -maxRange; z <= maxRange; z++) {
                    BlockPos checkPos = startPos.offset(x, y, z);
                    
                    if (!level.isLoaded(checkPos)) {
                        continue;
                    }
                    
                    BlockEntity blockEntity = level.getBlockEntity(checkPos);
                    
                    if (blockEntity instanceof IEnergyNode node) {
                        if (node.getNodeType() == targetType && node.isValid()) {
                            double distance = checkPos.distSqr(startPos);
                            
                            if (distance < minDistance) {
                                minDistance = distance;
                                nearestNode = node;
                            }
                        }
                    }
                }
            }
        }
        
        return nearestNode;
    }
    
    /**
     * 查找指定类型的所有设备
     * @param centerPos 中心位置
     * @param level 等级
     * @param targetType 目标类型
     * @param maxRange 最大范围
     * @return 找到的所有节点
     */
    public List<IEnergyNode> findAllNodesOfType(BlockPos centerPos, Level level, 
                                                 IEnergyNode.NodeType targetType, int maxRange) {
        List<IEnergyNode> foundNodes = new ObjectArrayList<>();
        
        if (level == null || centerPos == null) {
            return foundNodes;
        }
        
        // 遍历周围区域
        for (int x = -maxRange; x <= maxRange; x++) {
            for (int y = -maxRange; y <= maxRange; y++) {
                for (int z = -maxRange; z <= maxRange; z++) {
                    BlockPos checkPos = centerPos.offset(x, y, z);
                    
                    if (!level.isLoaded(checkPos)) {
                        continue;
                    }
                    
                    BlockEntity blockEntity = level.getBlockEntity(checkPos);
                    
                    if (blockEntity instanceof IEnergyNode node) {
                        if (node.getNodeType() == targetType && node.isValid()) {
                            foundNodes.add(node);
                        }
                    }
                }
            }
        }
        
        if (LOGGER.isDebugEnabled() && !foundNodes.isEmpty()) {
            LOGGER.debug("找到 {} 个类型为 {} 的设备", foundNodes.size(), targetType);
        }
        
        return foundNodes;
    }
}
