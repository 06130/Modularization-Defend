package org.lingZero.modularization_defend.AgreementCoreNetwork.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.*;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.transfer.ITransferData;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.transfer.ITransmitter;
import org.lingZero.modularization_defend.AgreementCoreNetwork.impl.NetworkConnectionImpl;

import java.util.*;

/**
 * 局部网络
 * 表示一个连通的子网络
 * 
 * @author Modularization Defend Team
 */
public class LocalNetwork {
    private final GlobalNetwork globalNetwork;
    private final Map<IConnectionPoint, Collection<INetworkConnection>> connections = new HashMap<>();
    private final Map<BlockPos, INetworkConnector> connectors = new HashMap<>();
    private final Map<ITransferData.DataType, ITransmitter<?>> transmitters = new EnumMap<>(ITransferData.DataType.class);
    private boolean isValid = true;
    private int version = 0;
    
    public LocalNetwork(GlobalNetwork globalNetwork) {
        this.globalNetwork = globalNetwork;
    }
    
    /**
     * 添加连接器
     */
    void addConnector(IConnectionPoint cp, INetworkConnector iic) {
        ++version;
        connections.put(cp, new ArrayList<>());
        if (!connectors.containsKey(cp.position())) {
            connectors.put(cp.position(), iic);
        }
    }
    
    /**
     * 移除连接器
     */
    void removeConnector(BlockPos pos) {
        ++version;
        INetworkConnector iic = connectors.get(pos);
        if (iic == null) return;
        
        for (IConnectionPoint point : iic.getConnectionPoints()) {
            if (connections.containsKey(point)) {
                for (INetworkConnection c : getConnections(point)) {
                    IConnectionPoint other = c.getOtherEnd(point);
                    Collection<INetworkConnection> connsOther = connections.get(other);
                    if (connsOther != null) {
                        connsOther.remove(c);
                    }
                }
                connections.remove(point);
            }
        }
        connectors.remove(pos);
        isValid = false;
    }
    
    /**
     * 添加连接
     */
    void addConnection(INetworkConnection conn) {
        ++version;
        connections.get(conn.getEndA()).add(conn);
        connections.get(conn.getEndB()).add(conn);
    }
    
    /**
     * 移除连接
     */
    void removeConnection(INetworkConnection c) {
        ++version;
        for (IConnectionPoint end : new IConnectionPoint[]{c.getEndA(), c.getEndB()}) {
            Collection<INetworkConnection> conns = connections.get(end);
            if (conns != null) {
                conns.remove(c);
            }
        }
    }
    
    /**
     * 获取所有连接点
     */
    public Collection<IConnectionPoint> getConnectionPoints() {
        return Collections.unmodifiableCollection(connections.keySet());
    }
    
    /**
     * 获取指定位置的所有连接
     */
    public Collection<INetworkConnection> getConnections(IConnectionPoint at) {
        Collection<INetworkConnection> conns = connections.get(at);
        if (conns != null) {
            return Collections.unmodifiableCollection(conns);
        } else {
            return Collections.emptySet();
        }
    }
    
    /**
     * 获取连接器
     */
    public INetworkConnector getConnector(BlockPos pos) {
        return connectors.get(pos);
    }
    
    /**
     * 获取所有连接器位置
     */
    public Collection<BlockPos> getConnectors() {
        return Collections.unmodifiableCollection(connectors.keySet());
    }
    
    /**
     * 注册传输器
     */
    public <T extends ITransferData> void registerTransmitter(ITransferData.DataType type, ITransmitter<T> transmitter) {
        transmitters.put(type, transmitter);
    }
    
    /**
     * 获取传输器
     */
    @SuppressWarnings("unchecked")
    public <T extends ITransferData> ITransmitter<T> getTransmitter(ITransferData.DataType type) {
        return (ITransmitter<T>) transmitters.get(type);
    }
    
    /**
     * 更新网络（每 tick 调用）
     */
    public void update(Level level) {
        // 更新所有传输器
        for (ITransmitter<?> transmitter : transmitters.values()) {
            List<INetworkConnection> allConnections = new ArrayList<>();
            for (Collection<INetworkConnection> conns : connections.values()) {
                allConnections.addAll(conns);
            }
            transmitter.update(allConnections);
        }
    }
    
    /**
     * 合并另一个网络
     */
    LocalNetwork merge(LocalNetwork other) {
        LocalNetwork result = new LocalNetwork(globalNetwork);
        result.connectors.putAll(this.connectors);
        result.connectors.putAll(other.connectors);
        result.connections.putAll(this.connections);
        result.connections.putAll(other.connections);
        result.transmitters.putAll(this.transmitters);
        result.transmitters.putAll(other.transmitters);
        return result;
    }
    
    /**
     * 分割网络
     */
    Collection<LocalNetwork> split() {
        Set<IConnectionPoint> toVisit = new HashSet<>(getConnectionPoints());
        Collection<LocalNetwork> ret = new ArrayList<>();
        
        while (!toVisit.isEmpty()) {
            Iterator<IConnectionPoint> tmpIt = toVisit.iterator();
            Collection<IConnectionPoint> inComponent = getConnectedComponent(tmpIt.next(), toVisit);
            
            if (toVisit.size() == 0 && ret.size() == 0) {
                break;
            }
            
            LocalNetwork newNet = new LocalNetwork(globalNetwork);
            for (IConnectionPoint p : inComponent) {
                newNet.addConnector(p, connectors.get(p.position()));
            }
            for (IConnectionPoint p : inComponent) {
                for (INetworkConnection c : getConnections(p)) {
                    if (c.isPositiveEnd(p)) {
                        newNet.addConnection(c);
                    }
                }
            }
            ret.add(newNet);
        }
        return ret;
    }
    
    /**
     * 获取连通分量
     */
    private Collection<IConnectionPoint> getConnectedComponent(IConnectionPoint start, Set<IConnectionPoint> unvisited) {
        Deque<IConnectionPoint> open = new ArrayDeque<>();
        List<IConnectionPoint> inComponent = new ArrayList<>();
        open.push(start);
        unvisited.remove(start);
        
        while (!open.isEmpty()) {
            IConnectionPoint curr = open.pop();
            inComponent.add(curr);
            for (INetworkConnection c : getConnections(curr)) {
                IConnectionPoint otherEnd = c.getOtherEnd(curr);
                if (unvisited.contains(otherEnd)) {
                    unvisited.remove(otherEnd);
                    open.push(otherEnd);
                }
            }
        }
        return inComponent;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public void setInvalid() {
        ++version;
        isValid = false;
    }
    
    public int getVersion() {
        return version;
    }
    
    /**
     * 写入 NBT（待完善）
     */
    public CompoundTag writeToNBT() {
        // TODO: 实现完整的 NBT 序列化
        return new CompoundTag();
    }
}
