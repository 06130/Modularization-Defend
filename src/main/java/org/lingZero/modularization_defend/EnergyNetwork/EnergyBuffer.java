package org.lingZero.modularization_defend.EnergyNetwork;

/**
 * 能量缓冲区
 * 用于存储设备的能量数据，使用 volatile 保证线程安全
 * 针对高频访问优化，避免 GC 压力
 */
public class EnergyBuffer {
    
    // 当前存储的能量
    private volatile long energy;
    
    // 最大容量
    private final long maxCapacity;
    
    // 输入速率限制（FE/t）
    private volatile long maxInput;
    
    // 输出速率限制（FE/t）
    private volatile long maxOutput;
    
    /**
     * 创建能量缓冲区
     * @param maxCapacity 最大容量
     */
    public EnergyBuffer(long maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.energy = 0;
        this.maxInput = Long.MAX_VALUE;
        this.maxOutput = Long.MAX_VALUE;
    }
    
    /**
     * 创建能量缓冲区（带速率限制）
     * @param maxCapacity 最大容量
     * @param maxInput 最大输入速率
     * @param maxOutput 最大输出速率
     */
    public EnergyBuffer(long maxCapacity, long maxInput, long maxOutput) {
        this.maxCapacity = maxCapacity;
        this.energy = 0;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
    }
    
    /**
     * 获取当前能量
     * @return 当前能量值
     */
    public long getEnergy() {
        return energy;
    }
    
    /**
     * 设置能量值（扫描时调用）
     * @param energy 新的能量值
     */
    public void setEnergy(long energy) {
        this.energy = Math.max(0, Math.min(energy, maxCapacity));
    }
    
    /**
     * 添加能量
     * @param amount 要添加的能量
     * @return 实际添加的能量量
     */
    public long addEnergy(long amount) {
        if (amount <= 0) return 0;
        
        long actualAmount = Math.min(amount, maxInput);
        long space = maxCapacity - energy;
        actualAmount = Math.min(actualAmount, space);
        
        if (actualAmount > 0) {
            energy += actualAmount;
        }
        
        return actualAmount;
    }
    
    /**
     * 提取能量
     * @param amount 要提取的能量
     * @return 实际提取的能量量
     */
    public long extractEnergy(long amount) {
        if (amount <= 0) return 0;
        
        long actualAmount = Math.min(amount, maxOutput);
        actualAmount = Math.min(actualAmount, energy);
        
        if (actualAmount > 0) {
            energy -= actualAmount;
        }
        
        return actualAmount;
    }
    
    /**
     * 填满缓冲区
     */
    public void fill() {
        this.energy = maxCapacity;
    }
    
    /**
     * 清空缓冲区
     */
    public void clear() {
        this.energy = 0;
    }
    
    /**
     * 检查是否为空
     * @return true 如果能量为 0
     */
    public boolean isEmpty() {
        return energy == 0;
    }
    
    /**
     * 检查是否已满
     * @return true 如果能量等于最大容量
     */
    public boolean isFull() {
        return energy >= maxCapacity;
    }
    
    /**
     * 获取剩余容量
     * @return 可接收的能量量
     */
    public long getRemainingCapacity() {
        return maxCapacity - energy;
    }
    
    /**
     * 获取填充率（0-1）
     * @return 填充率
     */
    public double getFillRatio() {
        if (maxCapacity == 0) return 0.0;
        return (double) energy / maxCapacity;
    }
    
    /**
     * 获取最大容量
     * @return 最大容量
     */
    public long getMaxCapacity() {
        return maxCapacity;
    }
    
    /**
     * 获取最大输入速率
     * @return 最大输入 FE/t
     */
    public long getMaxInput() {
        return maxInput;
    }
    
    /**
     * 设置最大输入速率
     * @param maxInput 新的最大输入
     */
    public void setMaxInput(long maxInput) {
        this.maxInput = maxInput;
    }
    
    /**
     * 获取最大输出速率
     * @return 最大输出 FE/t
     */
    public long getMaxOutput() {
        return maxOutput;
    }
    
    /**
     * 设置最大输出速率
     * @param maxOutput 新的最大输出
     */
    public void setMaxOutput(long maxOutput) {
        this.maxOutput = maxOutput;
    }
    
    /**
     * 复制缓冲区数据（用于快照）
     * @return 新的缓冲区副本
     */
    public EnergyBuffer copy() {
        EnergyBuffer copy = new EnergyBuffer(this.maxCapacity, this.maxInput, this.maxOutput);
        copy.energy = this.energy;
        return copy;
    }
}
