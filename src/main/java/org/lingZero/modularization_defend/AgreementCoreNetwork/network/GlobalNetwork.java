package org.lingZero.modularization_defend.AgreementCoreNetwork.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.IConnectionPoint;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.ICableType;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.INetworkConnection;
import org.lingZero.modularization_defend.AgreementCoreNetwork.api.INetworkConnector;
import org.lingZero.modularization_defend.AgreementCoreNetwork.impl.ConnectionPointImpl;
import org.lingZero.modularization_defend.AgreementCoreNetwork.impl.NetworkConnectionImpl;

import java.util.*;

/**
 * 全局网络
 * 管理整个维度的所有网络连接
 * 
 * @author Modularization Defend Team
 */
public class GlobalNetwork extends SavedData {
    private final Map<IConnectionPoint, LocalNetwork> localNetsByPos = new HashMap<>();
    private final Set<LocalNetwork> localNetSet = new HashSet<>();
    private final NetworkSyncManager syncManager;
    
    public GlobalNetwork() {
        this.syncManager = new NetworkSyncManager(this);
    }
    
    /**
     * 添加连接
     */
    public void addConnection(org.lingZero.modularization_defend.AgreementCoreNetwork.api.INetworkConnection conn) {
        IConnectionPoint posA = conn.getEndA();
        IConnectionPoint posB = conn.getEndB();
        LocalNetwork netA = getLocalNet(posA);
        LocalNetwork netB = getLocalNet(posB);
        
        LocalNetwork joined;
        if (netA != netB) {
            joined = netA.merge(netB);
            for (IConnectionPoint p : joined.getConnectionPoints()) {
                putLocalNet(p, joined);
            }
        } else {
            joined = netA;
        }
        
        joined.addConnection(conn);
        syncManager.onConnectionAdded(conn);
        setDirty();
    }
    
    /**
     * 移除连接
     */
    public void removeConnection(org.lingZero.modularization_defend.AgreementCoreNetwork.api.INetworkConnection c) {
        LocalNetwork oldNet = getNullableLocalNet(c.getEndA());
        if (oldNet == null) return;
        
        oldNet.removeConnection(c);
        splitNet(oldNet);
        syncManager.onConnectionRemoved(c);
        setDirty();
    }
    
    /**
     * 移除指定位置的所有连接
     */
    public void removeAllConnectionsAt(IConnectionPoint pos) {
        LocalNetwork net = getLocalNet(pos);
        List<org.lingZero.modularization_defend.AgreementCoreNetwork.api.INetworkConnection> conns = 
            new ArrayList<>(net.getConnections(pos));
        
        for (org.lingZero.modularization_defend.AgreementCoreNetwork.api.INetworkConnection conn : conns) {
            removeConnection(conn);
        }
    }
    
    /**
     * 获取局部网络
     */
    public LocalNetwork getLocalNet(IConnectionPoint pos) {
        LocalNetwork ret = localNetsByPos.computeIfAbsent(pos, p -> {
            LocalNetwork newNet = new LocalNetwork(this);
            // 创建代理节点
            INetworkConnector proxy = createProxy(pos.position());
            newNet.addConnector(pos, proxy);
            localNetSet.add(newNet);
            return newNet;
        });
        return ret;
    }
    
    /**
     * 获取局部网络（可空）
     */
    public LocalNetwork getNullableLocalNet(IConnectionPoint pos) {
        return localNetsByPos.get(pos);
    }
    
    /**
     * 添加连接器
     */
    public void onConnectorLoad(INetworkConnector iic) {
        boolean isNew = false;
        Set<LocalNetwork> loadedInNets = new HashSet<>();
        
        for (IConnectionPoint cp : iic.getConnectionPoints()) {
            if (getNullableLocalNet(cp) == null) {
                isNew = true;
            }
            LocalNetwork local = getLocalNet(cp);
            if (loadedInNets.add(local)) {
                local.addConnector(cp, iic);
            }
        }
        
        if (isNew) {
            // TODO: 添加内部连接（如果需要）
            // 内部连接通常用于多方块结构的内部连接
        }
    }
    
    /**
     * 移除连接器
     */
    public void removeConnector(INetworkConnector iic) {
        Set<LocalNetwork> netsToRemoveFrom = new HashSet<>();
        
        for (IConnectionPoint c : iic.getConnectionPoints()) {
            LocalNetwork local = getNullableLocalNet(c);
            if (local != null) {
                putLocalNet(c, null);
                netsToRemoveFrom.add(local);
            }
        }
        
        for (LocalNetwork net : netsToRemoveFrom) {
            net.removeConnector(iic.getPosition());
            if (net.getConnectionPoints().isEmpty()) {
                localNetSet.remove(net);
            } else {
                splitNet(net);
            }
        }
        setDirty();
    }
    
    /**
     * 更新网络（每 tick 调用）
     */
    public void update(Level world) {
        if (world.isClientSide()) return;
        
        for (LocalNetwork net : localNetSet.toArray(new LocalNetwork[0])) {
            net.update(world);
        }
    }
    
    /**
     * 从 NBT 读取
     */
    public void readFromNBT(CompoundTag nbt) {
        localNetSet.forEach(LocalNetwork::setInvalid);
        localNetSet.clear();
        localNetsByPos.clear();
        
        ListTag locals = nbt.getList("locals", Tag.TAG_COMPOUND);
        for (Tag b : locals) {
            CompoundTag subnet = (CompoundTag) b;
            LocalNetwork localNet = new LocalNetwork(this);
            // TODO: 从 NBT 加载数据
            localNetSet.add(localNet);
        }
    }
    
    /**
     * 保存到 NBT
     */
    @Override
    public CompoundTag save(CompoundTag savedNBT, net.minecraft.core.HolderLookup.Provider provider) {
        ListTag locals = new ListTag();
        for (LocalNetwork local : localNetSet) {
            // TODO: 保存 LocalNetwork 到 NBT
            // locals.add(local.writeToNBT());
            locals.add(new CompoundTag());
        }
        savedNBT.put("locals", locals);
        return savedNBT;
    }
    
    /**
     * 分割网络（包访问权限）
     */
    void splitNet(LocalNetwork oldNet) {
        Collection<LocalNetwork> newNets = oldNet.split();
        for (LocalNetwork net : newNets) {
            for (IConnectionPoint p : net.getConnectionPoints()) {
                putLocalNet(p, net);
            }
        }
    }
    
    /**
     * 创建代理节点
     */
    private INetworkConnector createProxy(BlockPos pos) {
        // TODO: 实现代理节点创建
        // 这是一个虚拟节点，用于当连接器不存在时保持网络结构
        return new INetworkConnector() {
            @Override
            public boolean canConnect() {
                return false;
            }

            @Override
            public BlockPos getConnectionMaster(ICableType cableType, Object target) {
                return pos;
            }

            @Override
            public Collection<IConnectionPoint> getConnectionPoints() {
                return Collections.singletonList(new ConnectionPointImpl(pos, 0));
            }

            @Override
            public boolean canConnectCable(ICableType cableType, IConnectionPoint target) {
                return false;
            }

            @Override
            public void connectCable(ICableType cableType, IConnectionPoint target,
                                   INetworkConnector other, IConnectionPoint otherTarget) {
            }

            @Override
            public void removeCable(INetworkConnection connection, IConnectionPoint attachedPoint) {
            }

            @Override
            public net.minecraft.world.phys.Vec3 getConnectionOffset(IConnectionPoint here,
                                                                     IConnectionPoint other,
                                                                     ICableType type) {
                return net.minecraft.world.phys.Vec3.ZERO;
            }

            @Override
            public BlockPos getPosition() {
                return pos;
            }
        };
    }
    
    /**
     * 获取所有局部网络
     */
    public Collection<LocalNetwork> getLocalNets() {
        return Collections.unmodifiableCollection(localNetSet);
    }
    
    /**
     * 存储局部网络（包访问权限，供 NetworkManager 访问）
     */
    void putLocalNet(IConnectionPoint cp, LocalNetwork net) {
        LocalNetwork oldNet = localNetsByPos.get(cp);
        if (oldNet != null && net != null && oldNet.isValid()) {
            oldNet.setInvalid();
            localNetSet.remove(oldNet);
        }
        if (net != null) {
            localNetsByPos.put(cp, net);
            localNetSet.add(net);
        } else {
            localNetsByPos.remove(cp);
        }
    }
    
    /**
     * 写入 NBT
     */
    private CompoundTag writeToNBT() {
        ListTag wires = new ListTag();
        for (IConnectionPoint p : localNetsByPos.keySet()) {
            for (var conn : localNetsByPos.get(p).getConnections(p)) {
                if (conn.isPositiveEnd(p)) {
                    // 转换为实现类以调用 toNBT()
                    if (conn instanceof NetworkConnectionImpl impl) {
                        wires.add(impl.toNBT());
                    }
                }
            }
        }
        CompoundTag ret = new CompoundTag();
        ret.put("wires", wires);
        return ret;
    }
}
