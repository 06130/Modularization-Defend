package org.lingZero.modularization_defend.AgreementCoreNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 路由数据包
 * 用于在网络节点之间传输数据和路由信息
 */
public class RoutingPacket {
    
    // 数据包类型枚举
    public enum PacketType {
        /** 路由发现请求 */
        ROUTE_REQUEST,
        /** 路由发现响应 */
        ROUTE_RESPONSE,
        /** 数据传输 */
        DATA_TRANSFER,
        /** 路由表更新 */
        ROUTING_UPDATE,
        /** 节点状态查询 */
        NODE_STATUS_QUERY,
        /** 节点状态响应 */
        NODE_STATUS_RESPONSE,
        /** 网络拓扑广播 */
        TOPOLOGY_BROADCAST
    }
    
    // 数据包头部
    private final PacketType packetType;
    private final String sourceAddress;      // 源地址
    private final String targetAddress;      // 目标地址
    private final String nextHop;            // 下一跳地址
    private final int hopCount;              // 跳数
    private final UUID packetId;             // 数据包唯一 ID
    private final long timestamp;            // 时间戳
    private final int priority;              // 优先级 (0-7, 0 最高)
    
    // 数据包负载
    @Nullable
    private final CompoundTag payload;
    
    // 路由路径记录（用于追踪已走过的路径）
    private final List<String> pathHistory;
    
    /**
     * 创建路由数据包
     */
    public RoutingPacket(@NotNull PacketType packetType,
                        @NotNull String sourceAddress,
                        @NotNull String targetAddress,
                        @NotNull String nextHop,
                        int hopCount,
                        int priority,
                        @Nullable CompoundTag payload) {
        this.packetType = packetType;
        this.sourceAddress = sourceAddress;
        this.targetAddress = targetAddress;
        this.nextHop = nextHop;
        this.hopCount = hopCount;
        this.packetId = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
        this.priority = priority;
        this.payload = payload;
        this.pathHistory = new ArrayList<>();
        this.pathHistory.add(sourceAddress);
    }
    
    /**
     * 从字节缓冲区读取数据包
     */
    public static RoutingPacket fromBytes(FriendlyByteBuf buf) {
        PacketType type = PacketType.values()[buf.readVarInt()];
        String source = buf.readUtf(128);
        String target = buf.readUtf(128);
        String nextHop = buf.readUtf(128);
        int hopCount = buf.readVarInt();
        UUID packetId = buf.readUUID();
        long timestamp = buf.readLong();
        int priority = buf.readVarInt();
        
        CompoundTag payload = buf.readNbt();
        
        RoutingPacket packet = new RoutingPacket(
            type, source, target, nextHop, hopCount, priority, payload
        );
        
        // 读取路径历史
        int pathSize = buf.readVarInt();
        for (int i = 0; i < pathSize; i++) {
            packet.pathHistory.add(buf.readUtf(128));
        }
        
        return packet;
    }
    
    /**
     * 将数据包写入字节缓冲区
     */
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(packetType.ordinal());
        buf.writeUtf(sourceAddress, 128);
        buf.writeUtf(targetAddress, 128);
        buf.writeUtf(nextHop, 128);
        buf.writeVarInt(hopCount);
        buf.writeUUID(packetId);
        buf.writeLong(timestamp);
        buf.writeVarInt(priority);
        buf.writeNbt(payload);
        
        // 写入路径历史
        buf.writeVarInt(pathHistory.size());
        for (String addr : pathHistory) {
            buf.writeUtf(addr, 128);
        }
    }
    
    /**
     * 将数据包保存到 NBT
     */
    @NotNull
    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("PacketType", packetType.name());
        tag.putString("SourceAddress", sourceAddress);
        tag.putString("TargetAddress", targetAddress);
        tag.putString("NextHop", nextHop);
        tag.putInt("HopCount", hopCount);
        tag.putUUID("PacketId", packetId);
        tag.putLong("Timestamp", timestamp);
        tag.putInt("Priority", priority);
        
        if (payload != null) {
            tag.put("Payload", payload);
        }
        
        // 保存路径历史
        ListTag pathTag = new ListTag();
        for (String addr : pathHistory) {
            pathTag.add(net.minecraft.nbt.StringTag.valueOf(addr));
        }
        tag.put("PathHistory", pathTag);
        
        return tag;
    }
    
    /**
     * 从 NBT 加载数据包
     */
    @Nullable
    public static RoutingPacket loadFromNBT(@NotNull CompoundTag tag) {
        try {
            PacketType type = PacketType.valueOf(tag.getString("PacketType"));
            String source = tag.getString("SourceAddress");
            String target = tag.getString("TargetAddress");
            String nextHop = tag.getString("NextHop");
            int hopCount = tag.getInt("HopCount");
            UUID packetId = tag.getUUID("PacketId");
            long timestamp = tag.getLong("Timestamp");
            int priority = tag.getInt("Priority");
            
            CompoundTag payload = tag.getCompound("Payload");
            
            RoutingPacket packet = new RoutingPacket(
                type, source, target, nextHop, hopCount, priority,
                payload.isEmpty() ? null : payload
            );
            
            // 加载路径历史
            ListTag pathTag = tag.getList("PathHistory", Tag.TAG_STRING);
            packet.pathHistory.clear();
            for (int i = 0; i < pathTag.size(); i++) {
                packet.pathHistory.add(pathTag.getString(i));
            }
            
            return packet;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 复制数据包并更新下一跳
     */
    @NotNull
    public RoutingPacket copyWithNextHop(@NotNull String newNextHop) {
        RoutingPacket newPacket = new RoutingPacket(
            packetType, sourceAddress, targetAddress, newNextHop,
            hopCount + 1, priority, payload != null ? payload.copy() : null
        );
        newPacket.pathHistory.addAll(this.pathHistory);
        return newPacket;
    }
    
    // ==================== Getter 方法 ====================
    
    @NotNull
    public PacketType getPacketType() {
        return packetType;
    }
    
    @NotNull
    public String getSourceAddress() {
        return sourceAddress;
    }
    
    @NotNull
    public String getTargetAddress() {
        return targetAddress;
    }
    
    @NotNull
    public String getNextHop() {
        return nextHop;
    }
    
    public int getHopCount() {
        return hopCount;
    }
    
    @NotNull
    public UUID getPacketId() {
        return packetId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public int getPriority() {
        return priority;
    }
    
    @Nullable
    public CompoundTag getPayload() {
        return payload;
    }
    
    @NotNull
    public List<String> getPathHistory() {
        return pathHistory;
    }
    
    /**
     * 检查数据包是否过期
     * @param maxAge 最大年龄（毫秒）
     */
    public boolean isExpired(long maxAge) {
        return System.currentTimeMillis() - timestamp > maxAge;
    }
    
    /**
     * 检查是否形成路由环路
     * @param address 要检查的地址
     */
    public boolean hasLoop(@NotNull String address) {
        return pathHistory.contains(address);
    }
    
    @Override
    public String toString() {
        return String.format("RoutingPacket{%s: %s -> %s (hops=%d, id=%s)}",
            packetType, sourceAddress, targetAddress, hopCount, packetId);
    }
}
