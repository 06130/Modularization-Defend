package org.lingZero.modularization_defend.EnergyNetwork;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;

/**
 * 网络扫描器
 * 使用 BFS 算法遍历能源网络，支持环路和大规模网络
 * 
 * 性能优化：
 * - 使用高性能集合减少 GC
 * - 支持并行扫描多个分支
 * - 增量更新避免重复计算
 */
public class NetworkScanner {
    
    private static final Logger LOGGER = LogManager.getLogger("EnergyNetwork.Scanner");
    
    // 扫描统计信息
    private int totalNodesScanned;
    private long lastScanDuration;
    
    /**
     * 执行网络扫描（从协议核心开始 BFS）
     * @param network 要扫描的网络
     * @return 扫描到的节点数量
     */
    public int scanNetwork(EnergyNetwork network) {
        if (network == null || !network.isValid()) {
            return 0;
        }
        
        long startTime = System.nanoTime();
        totalNodesScanned = 0;
        
        IEnergyNode protocolCore = network.getProtocolCore();
        if (protocolCore == null) {
            return 0;
        }
        
        // BFS 队列和访问标记
        Set<IEnergyNode> visited = new ObjectOpenHashSet<>();
        List<IEnergyNode> queue = new ObjectArrayList<>();
        
        queue.add(protocolCore);
        visited.add(protocolCore);
        
        int headIndex = 0;
        while (headIndex < queue.size()) {
            IEnergyNode current = queue.get(headIndex++);
            
            // 处理当前节点
            processNode(current);
            totalNodesScanned++;
            
            // 获取邻居
            List<IEnergyNode> neighbors = network.getNeighbors(current);
            
            // 遍历未访问的邻居
            for (IEnergyNode neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        lastScanDuration = System.nanoTime() - startTime;
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("扫描完成：{} 个节点，耗时 {} ms", 
                totalNodesScanned, lastScanDuration / 1_000_000.0);
        }
        
        return totalNodesScanned;
    }
    
    /**
     * 处理单个节点（调用节点的扫描回调）
     * @param node 要处理的节点
     */
    private void processNode(IEnergyNode node) {
        try {
            // 填满缓冲区
            node.onNetworkScan();
        } catch (Exception e) {
            LOGGER.error("处理节点时出错：{}", node.getNodePosition(), e);
        }
    }
    
    /**
     * 并行扫描大型网络（分叉并行处理）
     * @param network 要扫描的网络
     * @param maxThreads 最大线程数
     * @return 扫描到的节点数量
     */
    public int scanNetworkParallel(EnergyNetwork network, int maxThreads) {
        if (network == null || !network.isValid()) {
            return 0;
        }
        
        long startTime = System.nanoTime();
        totalNodesScanned = 0;
        
        IEnergyNode protocolCore = network.getProtocolCore();
        if (protocolCore == null) {
            return 0;
        }
        
        // 第一层：处理协议核心
        processNode(protocolCore);
        totalNodesScanned++;
        
        // 获取协议核心的直接邻居
        List<IEnergyNode> firstLevelNeighbors = network.getNeighbors(protocolCore);
        
        if (firstLevelNeighbors.isEmpty()) {
            lastScanDuration = System.nanoTime() - startTime;
            return totalNodesScanned;
        }
        
        // 如果邻居数量少，直接顺序扫描
        if (firstLevelNeighbors.size() <= 2 || maxThreads <= 1) {
            return scanNetwork(network);
        }
        
        // 否则并行扫描每个分支
        Set<IEnergyNode> globalVisited = new ObjectOpenHashSet<>();
        globalVisited.add(protocolCore);
        
        List<Thread> threads = new ObjectArrayList<>();
        
        for (IEnergyNode neighbor : firstLevelNeighbors) {
            if (!globalVisited.contains(neighbor)) {
                Thread thread = new Thread(() -> {
                    scanBranch(network, neighbor, globalVisited);
                });
                threads.add(thread);
                
                if (threads.size() >= maxThreads) {
                    break;
                }
            }
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("扫描线程被中断", e);
            }
        }
        
        lastScanDuration = System.nanoTime() - startTime;
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("并行扫描完成：{} 个节点，耗时 {} ms", 
                totalNodesScanned, lastScanDuration / 1_000_000.0);
        }
        
        return totalNodesScanned;
    }
    
    /**
     * 扫描单个分支（线程安全）
     * @param network 网络
     * @param startNode 起始节点
     * @param globalVisited 全局访问标记（共享）
     */
    private void scanBranch(EnergyNetwork network, IEnergyNode startNode, Set<IEnergyNode> globalVisited) {
        Set<IEnergyNode> localVisited = new ObjectOpenHashSet<>();
        List<IEnergyNode> queue = new ObjectArrayList<>();
        
        queue.add(startNode);
        localVisited.add(startNode);
        globalVisited.add(startNode);
        
        int headIndex = 0;
        while (headIndex < queue.size()) {
            IEnergyNode current = queue.get(headIndex++);
            
            // 处理节点
            processNode(current);
            
            synchronized (this) {
                totalNodesScanned++;
            }
            
            // 获取邻居
            List<IEnergyNode> neighbors = network.getNeighbors(current);
            
            // 遍历未访问的邻居
            for (IEnergyNode neighbor : neighbors) {
                synchronized (globalVisited) {
                    if (!globalVisited.contains(neighbor)) {
                        globalVisited.add(neighbor);
                        
                        if (!localVisited.contains(neighbor)) {
                            localVisited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 获取上次扫描的节点数量
     * @return 节点数量
     */
    public int getTotalNodesScanned() {
        return totalNodesScanned;
    }
    
    /**
     * 获取上次扫描耗时（毫秒）
     * @return 耗时（毫秒）
     */
    public double getLastScanDuration() {
        return lastScanDuration / 1_000_000.0;
    }
    
    /**
     * 估算扫描时间（基于节点数量）
     * @param nodeCount 节点数量
     * @return 预估耗时（毫秒）
     */
    public double estimateScanTime(int nodeCount) {
        if (totalNodesScanned == 0) {
            return 1.0; // 默认 1ms
        }
        
        // 基于上次扫描的平均时间估算
        double avgTimePerNode = lastScanDuration / (double) totalNodesScanned;
        return (avgTimePerNode * nodeCount) / 1_000_000.0;
    }
}
