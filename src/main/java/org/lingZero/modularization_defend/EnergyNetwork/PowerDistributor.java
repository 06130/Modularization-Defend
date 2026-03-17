package org.lingZero.modularization_defend.EnergyNetwork;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * 能量分配器
 * 负责在网络中智能分配能量，支持优先级和负载均衡
 * 
 * 分配策略：
 * 1. 按优先级分配（高优先级优先）
 * 2. 按比例分配（剩余能量平均分配）
 * 3. 按需分配（根据设备需求）
 */
public class PowerDistributor {
    
    private static final Logger LOGGER = LogManager.getLogger("EnergyNetwork.Distributor");
    
    // 默认分配策略
    private DistributionStrategy currentStrategy;
    
    /**
     * 创建能量分配器
     */
    public PowerDistributor() {
        this.currentStrategy = DistributionStrategy.PRIORITY_BASED;
    }
    
    /**
     * 设置分配策略
     * @param strategy 新的策略
     */
    public void setStrategy(DistributionStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("策略不能为空");
        }
        this.currentStrategy = strategy;
    }
    
    /**
     * 获取当前策略
     * @return 当前策略
     */
    public DistributionStrategy getStrategy() {
        return currentStrategy;
    }
    
    /**
     * 在网络中分配能量（简化版：直接从协议核心提取）
     * @param network 目标网络
     * @return 成功分配的设备数量
     */
    public int distributeEnergy(EnergyNetwork network) {
        if (network == null || !network.isValid()) {
            return 0;
        }
        
        IEnergyNode protocolCore = network.getProtocolCore();
        if (protocolCore == null) {
            return 0;
        }
        
        // 收集所有耗能设备
        List<IEnergyNode> consumers = new ObjectArrayList<>();
        List<IEnergyNode> producers = new ObjectArrayList<>();
        List<IEnergyNode> storages = new ObjectArrayList<>();
        
        for (IEnergyNode node : network.getAllNodes()) {
            switch (node.getNodeType()) {
                case CONSUMER -> consumers.add(node);
                case PRODUCER -> producers.add(node);
                case STORAGE -> storages.add(node);
                default -> {}
            }
        }
        
        // 简单分配策略：直接从协议核心提取能量给耗能设备
        return distributeFromProtocolCore(network, protocolCore, consumers, producers, storages);
    }
    
    /**
     * 从协议核心直接分配能量给耗能设备
     * @param network 网络
     * @param protocolCore 协议核心
     * @param consumers 耗能设备列表
     * @param producers 产能设备列表
     * @param storages 储能设备列表
     * @return 成功分配的数量
     */
    private int distributeFromProtocolCore(EnergyNetwork network, IEnergyNode protocolCore,
                                           List<IEnergyNode> consumers,
                                           List<IEnergyNode> producers, 
                                           List<IEnergyNode> storages) {
        int distributedCount = 0;
        
        // 首先收集所有生产者的能量到协议核心
        long totalProduced = 0;
        for (IEnergyNode producer : producers) {
            EnergyBuffer producerBuffer = producer.getEnergyBuffer();
            long energy = producerBuffer.getEnergy();
            if (energy > 0) {
                // 从生产者提取到协议核心
                long extracted = producer.extractEnergy(energy, protocolCore);
                protocolCore.receiveEnergy(extracted, producer);
                totalProduced += extracted;
            }
        }
        
        // 按优先级排序消费者（降序）
        consumers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        
        // 为每个耗能设备分配能量
        for (IEnergyNode consumer : consumers) {
            long demand = consumer.getEnergyDemand();
            if (demand <= 0) continue;
            
            // 直接从协议核心提取能量给消费者
            long canExtract = Math.min(demand, protocolCore.getEnergyBuffer().getEnergy());
            if (canExtract > 0) {
                long extracted = protocolCore.extractEnergy(canExtract, consumer);
                if (extracted > 0) {
                    consumer.receiveEnergy(extracted, protocolCore);
                    distributedCount++;
                }
            }
        }
        
        // 剩余能量存入储能设备
        if (!storages.isEmpty() && protocolCore.getEnergyBuffer().getEnergy() > 0) {
            storages.sort((a, b) -> Long.compare(b.getEnergyBuffer().getRemainingCapacity(), 
                                                  a.getEnergyBuffer().getRemainingCapacity()));
            
            for (IEnergyNode storage : storages) {
                long remainingCapacity = storage.getEnergyBuffer().getRemainingCapacity();
                if (remainingCapacity <= 0) continue;
                
                long canStore = Math.min(remainingCapacity, protocolCore.getEnergyBuffer().getEnergy());
                if (canStore > 0) {
                    long extracted = protocolCore.extractEnergy(canStore, storage);
                    if (extracted > 0) {
                        storage.receiveEnergy(extracted, protocolCore);
                    }
                }
            }
        }
        
        return distributedCount;
    }
    
    /**
     * 按优先级分配能量
     * @param network 网络
     * @param consumers 耗能设备列表
     * @param producers 产能设备列表
     * @param storages 储能设备列表
     * @return 成功分配的数量
     */
    private int distributeByPriority(EnergyNetwork network, List<IEnergyNode> consumers, 
                                     List<IEnergyNode> producers, List<IEnergyNode> storages) {
        int distributedCount = 0;
        
        // 按优先级排序（降序）
        consumers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        
        // 计算总可用能量
        long totalAvailableEnergy = 0;
        for (IEnergyNode producer : producers) {
            totalAvailableEnergy += producer.getEnergyBuffer().getEnergy();
        }
        
        // 为每个耗能设备分配
        for (IEnergyNode consumer : consumers) {
            long demand = consumer.getEnergyDemand();
            long actualAmount = Math.min(demand, totalAvailableEnergy);
            
            if (actualAmount > 0) {
                // 从生产者提取能量
                long extracted = extractFromProducers(producers, actualAmount);
                
                if (extracted > 0) {
                    consumer.receiveEnergy(extracted, null);
                    totalAvailableEnergy -= extracted;
                    distributedCount++;
                }
            }
        }
        
        // 剩余能量存入储能设备
        if (totalAvailableEnergy > 0 && !storages.isEmpty()) {
            storages.sort((a, b) -> Long.compare(b.getEnergyBuffer().getRemainingCapacity(), 
                                                  a.getEnergyBuffer().getRemainingCapacity()));
            
            for (IEnergyNode storage : storages) {
                if (totalAvailableEnergy <= 0) break;
                
                long remainingCapacity = storage.getEnergyBuffer().getRemainingCapacity();
                long toInsert = Math.min(remainingCapacity, totalAvailableEnergy);
                
                if (toInsert > 0) {
                    long extracted = extractFromProducers(producers, toInsert);
                    if (extracted > 0) {
                        storage.receiveEnergy(extracted, null);
                        totalAvailableEnergy -= extracted;
                    }
                }
            }
        }
        
        return distributedCount;
    }
    
    /**
     * 按比例分配能量
     * @param network 网络
     * @param consumers 耗能设备列表
     * @param producers 产能设备列表
     * @return 成功分配的数量
     */
    private int distributeProportionally(EnergyNetwork network, List<IEnergyNode> consumers, 
                                         List<IEnergyNode> producers) {
        if (consumers.isEmpty()) {
            return 0;
        }
        
        // 计算总需求
        long totalDemand = 0;
        for (IEnergyNode consumer : consumers) {
            totalDemand += consumer.getEnergyDemand();
        }
        
        if (totalDemand == 0) {
            return 0;
        }
        
        // 计算总可用能量
        long totalAvailable = 0;
        for (IEnergyNode producer : producers) {
            totalAvailable += producer.getEnergyBuffer().getEnergy();
        }
        
        // 按比例分配给每个消费者
        int distributedCount = 0;
        for (IEnergyNode consumer : consumers) {
            long demand = consumer.getEnergyDemand();
            double ratio = (double) demand / totalDemand;
            long allocation = (long) (totalAvailable * ratio);
            
            if (allocation > 0) {
                long extracted = extractFromProducers(producers, allocation);
                if (extracted > 0) {
                    consumer.receiveEnergy(extracted, null);
                    distributedCount++;
                }
            }
        }
        
        return distributedCount;
    }
    
    /**
     * 按需分配能量
     * @param network 网络
     * @param consumers 耗能设备列表
     * @param producers 产能设备列表
     * @param storages 储能设备列表
     * @return 成功分配的数量
     */
    private int distributeByDemand(EnergyNetwork network, List<IEnergyNode> consumers, 
                                   List<IEnergyNode> producers, List<IEnergyNode> storages) {
        int distributedCount = 0;
        
        // 首先满足紧急需求（缓冲区<20%）
        for (IEnergyNode consumer : consumers) {
            EnergyBuffer buffer = consumer.getEnergyBuffer();
            if (buffer.getFillRatio() < 0.2) {
                long needed = (long) (buffer.getMaxCapacity() * 0.5); // 填充到 50%
                long extracted = extractFromProducers(producers, needed);
                
                if (extracted > 0) {
                    consumer.receiveEnergy(extracted, null);
                    distributedCount++;
                }
            }
        }
        
        // 然后满足其他需求
        for (IEnergyNode consumer : consumers) {
            EnergyBuffer buffer = consumer.getEnergyBuffer();
            if (buffer.getFillRatio() >= 0.2 && buffer.getFillRatio() < 0.8) {
                long demand = consumer.getEnergyDemand();
                long extracted = extractFromProducers(producers, demand);
                
                if (extracted > 0) {
                    consumer.receiveEnergy(extracted, null);
                    distributedCount++;
                }
            }
        }
        
        // 最后填充储能设备
        for (IEnergyNode storage : storages) {
            EnergyBuffer buffer = storage.getEnergyBuffer();
            if (!buffer.isFull()) {
                long remainingCapacity = buffer.getRemainingCapacity();
                long extracted = extractFromProducers(producers, remainingCapacity);
                
                if (extracted > 0) {
                    storage.receiveEnergy(extracted, null);
                }
            }
        }
        
        return distributedCount;
    }
    
    /**
     * 从生产者提取能量
     * @param producers 生产者列表
     * @param amount 要提取的量
     * @return 实际提取的量
     */
    private long extractFromProducers(List<IEnergyNode> producers, long amount) {
        long extracted = 0;
        long remaining = amount;
        
        for (IEnergyNode producer : producers) {
            if (remaining <= 0) break;
            
            EnergyBuffer buffer = producer.getEnergyBuffer();
            long canExtract = Math.min(buffer.getEnergy(), remaining);
            
            if (canExtract > 0) {
                buffer.extractEnergy(canExtract);
                extracted += canExtract;
                remaining -= canExtract;
            }
        }
        
        return extracted;
    }
    
    /**
     * 传输能量从一个节点到另一个节点
     * @param from 源节点
     * @param to 目标节点
     * @param amount 传输量
     * @return 实际传输量
     */
    public long transferEnergy(IEnergyNode from, IEnergyNode to, long amount) {
        if (from == null || to == null || amount <= 0) {
            return 0;
        }
        
        // 从源节点提取
        long extracted = from.extractEnergy(amount, to);
        
        if (extracted > 0) {
            // 传输到目标节点
            long received = to.receiveEnergy(extracted, from);
            return received;
        }
        
        return 0;
    }
    
    /**
     * 能量分配策略枚举
     */
    public enum DistributionStrategy {
        /**
         * 基于优先级（高优先级优先）
         */
        PRIORITY_BASED,
        
        /**
         * 按比例分配（平均分配）
         */
        PROPORTIONAL,
        
        /**
         * 按需分配（紧急需求优先）
         */
        DEMAND_BASED
    }
}
