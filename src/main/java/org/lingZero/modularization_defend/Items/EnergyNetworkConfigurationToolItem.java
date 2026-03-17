package org.lingZero.modularization_defend.Items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.lingZero.modularization_defend.EnergyNetwork.*;

/**
 * 电网配置工具 - 用于连接能源网络设备
 * 左键点击两个设备可以建立连接（带距离检测）
 */
public class EnergyNetworkConfigurationToolItem extends Item {

    public EnergyNetworkConfigurationToolItem(Properties properties) {
        super(properties);
    }

    /**
     * 监测玩家手持此物品时的左键点击
     * 该方法会在事件处理器中被调用
     * @param player 玩家
     * @param hand 使用的手
     * @param firstPos 第一次点击的方块坐标，如果为 null 则不显示
     * @param secondPos 第二次点击的方块坐标，如果为 null 则只显示第一次点击
     */
    public void onLeftClick(Player player, InteractionHand hand, net.minecraft.core.BlockPos firstPos, net.minecraft.core.BlockPos secondPos) {
        if (player.level().isClientSide) {
            // 客户端显示消息
            if (firstPos != null) {
                if (secondPos != null) {
                    // 两次点击，计算距离并显示第二次坐标
                    double distance = calculateDistance(firstPos, secondPos);
                    net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable(
                        "message.modularization_defend.block_distance",
                        secondPos.getX(), secondPos.getY(), secondPos.getZ(),
                        String.format("%.2f", distance)
                    );
                    player.sendSystemMessage(message);
                } else {
                    // 只有第一次点击
                    net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.translatable(
                        "message.modularization_defend.first_click",
                        firstPos.getX(), firstPos.getY(), firstPos.getZ()
                    );
                    player.sendSystemMessage(message);
                }
            }
        } else {
            // 服务器端处理连接逻辑
            if (firstPos != null && secondPos != null) {
                tryConnectDevices(player.level(), firstPos, secondPos);
            }
        }
    }
    
    /**
     * 尝试连接两个能源设备
     */
    private void tryConnectDevices(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos1, net.minecraft.core.BlockPos pos2) {
        // 获取第一个节点的方块实体
        var blockEntity1 = level.getBlockEntity(pos1);
        if (!(blockEntity1 instanceof IEnergyNode node1)) {
            return;
        }
        
        // 获取第二个节点的方块实体
        var blockEntity2 = level.getBlockEntity(pos2);
        if (!(blockEntity2 instanceof IEnergyNode node2)) {
            return;
        }
        
        // 验证节点有效性
        if (!node1.isValid() || !node2.isValid()) {
            return;
        }
        
        // 获取管理器
        EnergyNetworkManager manager = EnergyNetworkManager.getInstance();
        
        // 查找或创建网络
        EnergyNetwork network1 = manager.getNetworkByPosition(pos1);
        EnergyNetwork network2 = manager.getNetworkByPosition(pos2);
        
        if (network1 == null && network2 == null) {
            // 两个都不在网络中，检查是否有协议核心
            if (node1.getNodeType() == IEnergyNode.NodeType.PROTOCOL_CORE) {
                // 以 node1 为核心创建网络
                network1 = manager.createNetwork(pos1);
                network1.setProtocolCore(node1);
                manager.registerNetwork(network1);
                network1.addNode(node2);
                network1.connectNodes(node1, node2);
                network1.connectNodes(node2, node1);
            } else if (node2.getNodeType() == IEnergyNode.NodeType.PROTOCOL_CORE) {
                // 以 node2 为核心创建网络
                network2 = manager.createNetwork(pos2);
                network2.setProtocolCore(node2);
                manager.registerNetwork(network2);
                network2.addNode(node1);
                network2.connectNodes(node2, node1);
                network2.connectNodes(node1, node2);
            } else {
                // 没有协议核心，无法创建网络
                return;
            }
        } else if (network1 != null && network2 == null) {
            // node2 加入 network1
            network1.addNode(node2);
            network1.connectNodes(node1, node2);
            network1.connectNodes(node2, node1);
        } else if (network1 == null && network2 != null) {
            // node1 加入 network2
            network2.addNode(node1);
            network2.connectNodes(node2, node1);
            network2.connectNodes(node1, node2);
        } else if (network1 != null && network2 != null) {
            // 两个都在网络中
            if (network1 == network2) {
                // 同一网络，直接连接
                network1.connectNodes(node1, node2);
                network1.connectNodes(node2, node1);
            } else {
                // 不同网络，合并（需要检查是否都是协议核心）
                if (node1.getNodeType() == IEnergyNode.NodeType.PROTOCOL_CORE ||
                    node2.getNodeType() == IEnergyNode.NodeType.PROTOCOL_CORE) {
                    // 不能合并两个都有协议核心的网络
                    return;
                }
                // 合并网络
                mergeNetworks(manager, network1, network2, node1, node2);
            }
        }
    }
    
    /**
     * 合并两个网络
     */
    private void mergeNetworks(EnergyNetworkManager manager, EnergyNetwork network1, 
                               EnergyNetwork network2, IEnergyNode node1, IEnergyNode node2) {
        // 将 network2 的所有节点移到 network1
        for (IEnergyNode node : network2.getAllNodes()) {
            network1.addNode(node);
        }
        
        // 删除 network2
        manager.unregisterNetwork(network2.getNetworkId());
        
        // 在新网络中建立连接
        network1.connectNodes(node1, node2);
        network1.connectNodes(node2, node1);
    }
    
    /**
     * 计算两个坐标之间的直线距离
     * @param pos1 第一个坐标
     * @param pos2 第二个坐标
     * @return 直线距离
     */
    private double calculateDistance(net.minecraft.core.BlockPos pos1, net.minecraft.core.BlockPos pos2) {
        int dx = pos2.getX() - pos1.getX();
        int dy = pos2.getY() - pos1.getY();
        int dz = pos2.getZ() - pos1.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
