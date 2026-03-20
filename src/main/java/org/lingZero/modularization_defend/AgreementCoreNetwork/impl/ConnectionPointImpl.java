package org.lingZero.modularization_defend.AgreementCoreNetwork.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.IConnectionPoint;

/**
 * 连接点实现
 * 
 * @author Modularization Defend Team
 */
public class ConnectionPointImpl implements IConnectionPoint {
    private final BlockPos position;
    private final int id;
    
    public ConnectionPointImpl(BlockPos position, int id) {
        this.position = position;
        this.id = id;
    }
    
    public ConnectionPointImpl(CompoundTag tag) {
        this.position = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        this.id = tag.getInt("id");
    }
    
    @Override
    public BlockPos position() {
        return position;
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", position.getX());
        tag.putInt("y", position.getY());
        tag.putInt("z", position.getZ());
        tag.putInt("id", id);
        return tag;
    }
    
    @Override
    public int compareTo(IConnectionPoint other) {
        int posCompare = position.compareTo(other.position());
        if (posCompare != 0) {
            return posCompare;
        }
        return Integer.compare(this.id, other.getId());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IConnectionPoint)) return false;
        IConnectionPoint other = (IConnectionPoint) obj;
        return position.equals(other.position()) && id == other.getId();
    }
    
    @Override
    public int hashCode() {
        return 31 * position.hashCode() + id;
    }
    
    @Override
    public String toString() {
        return "ConnectionPoint{" +
                "position=" + position +
                ", id=" + id +
                '}';
    }
}
