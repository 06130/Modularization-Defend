package org.lingZero.modularization_defend.AgreementCoreNetwork.impl;

import net.minecraft.nbt.CompoundTag;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.ICableType;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.IConnectionPoint;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.INetworkConnection;
import org.lingZero.modularization_defend.AgreementCoreNetwork.network.GlobalNetwork;

/**
 * 网络连接实现
 * 
 * @author Modularization Defend Team
 */
public class NetworkConnectionImpl implements INetworkConnection {
    private final ICableType type;
    private final IConnectionPoint endA;
    private final IConnectionPoint endB;
    private final boolean internal;
    private double length;
    
    public NetworkConnectionImpl(ICableType type, IConnectionPoint endA, IConnectionPoint endB, boolean internal) {
        this.type = type;
        // 确保端点顺序一致，便于比较
        if (endA.compareTo(endB) < 0) {
            this.endA = endB;
            this.endB = endA;
        } else {
            this.endA = endA;
            this.endB = endB;
        }
        this.internal = internal;
        this.length = Math.sqrt(endA.position().distSqr(endB.position()));
    }
    
    public NetworkConnectionImpl(ICableType type, IConnectionPoint endA, IConnectionPoint endB, GlobalNetwork netForOffsets) {
        this(type, endA, endB, false);
    }
    
    /**
     * 创建内部连接（同一方块内的连接）
     */
    public NetworkConnectionImpl(IConnectionPoint pos, int idA, int idB) {
        this(null, pos, new ConnectionPointImpl(pos.position(), idB), true);
    }
    
    /**
     * 从 NBT 读取
     */
    public NetworkConnectionImpl(CompoundTag nbt) {
        this(
            null, // TODO: 从字符串获取线缆类型
            IConnectionPoint.fromTag(nbt.getCompound("endA")),
            IConnectionPoint.fromTag(nbt.getCompound("endB")),
            nbt.getBoolean("internal")
        );
    }
    
    @Override
    public ICableType getType() {
        return type;
    }
    
    @Override
    public IConnectionPoint getEndA() {
        return endA;
    }
    
    @Override
    public IConnectionPoint getEndB() {
        return endB;
    }
    
    @Override
    public IConnectionPoint getOtherEnd(IConnectionPoint known) {
        if (known.equals(endA)) {
            return endB;
        } else {
            return endA;
        }
    }
    
    @Override
    public boolean isPositiveEnd(IConnectionPoint p) {
        return p.equals(endA);
    }
    
    @Override
    public boolean isInternal() {
        return internal;
    }
    
    @Override
    public double getLength() {
        return length;
    }
    
    /**
     * 转换为 NBT
     */
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("endA", endA.createTag());
        nbt.put("endB", endB.createTag());
        if (type != null) {
            nbt.putString("type", type.getUniqueName());
        }
        nbt.putBoolean("internal", internal);
        return nbt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NetworkConnectionImpl)) return false;
        NetworkConnectionImpl that = (NetworkConnectionImpl) obj;
        return internal == that.internal &&
               type.equals(that.type) &&
               endA.equals(that.endA) &&
               endB.equals(that.endB);
    }
    
    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + endA.hashCode();
        result = 31 * result + endB.hashCode();
        result = 31 * result + (internal ? 1 : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "NetworkConnection{" +
                "type=" + type +
                ", endA=" + endA +
                ", endB=" + endB +
                ", internal=" + internal +
                '}';
    }
}
