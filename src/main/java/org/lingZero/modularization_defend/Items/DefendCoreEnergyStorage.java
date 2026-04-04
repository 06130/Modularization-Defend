package org.lingZero.modularization_defend.Items;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.lingZero.modularization_defend.DataComponents.DefendCoreData;

/**
 * 基于数据组件的能量存储实现
 * 参考 NeoForge 官方的 ComponentEnergyStorage 实现
 */
public class DefendCoreEnergyStorage implements IEnergyStorage {
    protected final MutableDataComponentHolder parent;
    protected final DataComponentType<DefendCoreData> dataComponent;
    
    public DefendCoreEnergyStorage(MutableDataComponentHolder parent, DataComponentType<DefendCoreData> dataComponent) {
        this.parent = parent;
        this.dataComponent = dataComponent;
    }
    
    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (!canReceive() || toReceive <= 0) {
            return 0;
        }
        
        DefendCoreData data = getCurrentData();
        long currentEnergy = data.energyCurrent();
        long maxEnergy = data.energyMax();
        
        // 计算可以接收的能量
        int energyReceived = (int) Mth.clamp(
            maxEnergy - currentEnergy,  // 剩余容量
            0, 
            Math.min(Integer.MAX_VALUE, toReceive)  // 最大接收量
        );
        
        if (!simulate && energyReceived > 0) {
            DefendCoreData newData = data.withEnergyCurrent(currentEnergy + energyReceived);
            parent.set(dataComponent, newData);
        }
        
        return energyReceived;
    }
    
    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (!canExtract() || toExtract <= 0) {
            return 0;
        }
        
        DefendCoreData data = getCurrentData();
        long currentEnergy = data.energyCurrent();
        
        // 计算可以提取的能量
        int energyExtracted = (int) Math.min(
            currentEnergy,
            Math.min(Integer.MAX_VALUE, toExtract)
        );
        
        if (!simulate && energyExtracted > 0) {
            DefendCoreData newData = data.withEnergyCurrent(currentEnergy - energyExtracted);
            parent.set(dataComponent, newData);
        }
        
        return energyExtracted;
    }
    
    @Override
    public int getEnergyStored() {
        DefendCoreData data = getCurrentData();
        return (int) Mth.clamp(data.energyCurrent(), 0, Integer.MAX_VALUE);
    }
    
    @Override
    public int getMaxEnergyStored() {
        DefendCoreData data = getCurrentData();
        return (int) Mth.clamp(data.energyMax(), 0, Integer.MAX_VALUE);
    }
    
    @Override
    public boolean canExtract() {
        return true;
    }
    
    @Override
    public boolean canReceive() {
        return true;
    }
    
    /**
     * 获取当前的数据组件
     */
    protected DefendCoreData getCurrentData() {
        DefendCoreData data = parent.get(dataComponent);
        return data != null ? data : DefendCoreData.createDefault();
    }
}
