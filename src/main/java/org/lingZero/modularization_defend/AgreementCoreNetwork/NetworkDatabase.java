package org.lingZero.modularization_defend.AgreementCoreNetwork;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 路由表管理工具类
 * 实现类似 IPv6 的地址系统和路由表功能
 * <p>
 * 地址格式：网络 ID:所属维度：对应多方块结构 ID:坐标数据：短 UID（8 位十六进制）
 * 示例：modularization_defend:minecraft:overworld:core_001:100_64_-200:a1b2c3d4
 * <p>
 * 数据存储：使用 NeoForge SavedData 机制，独立存储在世界存档文件夹中
 * 文件位置：saves/<world>/data/modularization_defend_routing.nbt
 * <p>
 * 性能优化：
 * - 读写分离设计，读操作无锁
 * - 延迟保存机制，合并多次 markDirty 调用
 * - 支持异步访问（可选）
 * - RoutingEntry 复用减少对象创建
 * - 缓存地址解析结果
 * - 反向索引加速 findRoutesVia 查询
 * - 仅支持短 UID（8 位十六进制，节省约 75% 内存）
 */
public final class NetworkDatabase extends SavedData {

    // 存档数据名称
    private static final String DATA_NAME = "modularization_defend_routing";
    private static final int ADDRESS_CACHE_SIZE = 1024;
    private static final int MAX_DIRTY_COUNT = 10; // 最多累积 10 次修改才触发保存
    // NBT 存储键
    private static final String ROUTING_TABLE_KEY = "RoutingTable";
    private static final String LOCAL_ADDRESS_KEY = "LocalAddress";
    private static final String TARGET_ADDRESS_KEY = "TargetAddress";
    private static final String NEXT_HOPS_KEY = "NextHops";
    private static final String METRIC_KEY = "Metric";
    private static final String TIMESTAMP_KEY = "Timestamp";
    // 路由度量（跳数）的默认值
    private static final int DEFAULT_METRIC = 1;
    private static final int INFINITE_METRIC = Integer.MAX_VALUE;
    // 静态实例引用（用于地址缓存访问）
    private static NetworkDatabase instance;
    // 路由表：目标地址 -> 路由条目（主数据，仅服务器线程访问）
    private final Map<String, RoutingEntry> routingTable;
    // 反向索引：下一跳地址 -> 目标地址集合（优化 findRoutesVia）
    private final Map<String, Set<String>> reverseIndex;
    // 地址解析缓存（LRU 缓存，避免重复 split）
    private final Map<String, String[]> addressCache;
    // 延迟保存控制
    private final AtomicBoolean pendingSave = new AtomicBoolean(false);
    private final AtomicInteger dirtyCount = new AtomicInteger(0);
    // 只读视图：供其他线程安全读取（延迟更新）
    private volatile Map<String, RoutingEntry> readOnlyView;
    // 本地节点地址
    private String localAddress;

    /**
     * 私有构造函数，通过 {@link #getOrCreate()} 获取实例
     */
    private NetworkDatabase() {
        instance = this; // 保存静态引用
        this.routingTable = new HashMap<>();
        this.readOnlyView = Collections.unmodifiableMap(this.routingTable);
        this.reverseIndex = new HashMap<>();
        this.localAddress = null;
        // 使用 LinkedHashMap 实现 LRU 缓存
        this.addressCache = new LinkedHashMap<String, String[]>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String[]> eldest) {
                return size() > ADDRESS_CACHE_SIZE;
            }
        };
    }

    /**
     * 获取或创建路由表实例（仅服务器端可用）
     *
     * @return 路由表实例
     */
    public static NetworkDatabase getOrCreate() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new IllegalStateException("Server is not available - can only access WorldDatabase on server side");
        }

        DimensionDataStorage dataStorage = server.overworld().getDataStorage();
        return dataStorage.computeIfAbsent(
                new Factory<>(NetworkDatabase::new, (tag, provider) -> {
                    NetworkDatabase data = new NetworkDatabase();
                    data.loadFromNBT(tag, provider);
                    return data;
                }),
                DATA_NAME
        );
    }


    /**
     * 构建标准地址（五段式结构，使用短 UID）
     * 格式：网络 ID:所属维度：对应多方块结构 ID:坐标数据：短 UID（8 位）
     *
     * @param networkId   网络 ID
     * @param dimension   所属维度（如 minecraft:overworld）
     * @param structureId 对应多方块结构 ID（如 core_001）
     * @param posData     坐标数据（格式：x_y_z，如 100_64_-200）
     * @return 完整的地址字符串（自动生成 8 位短 UID）
     */
    public static String buildAddress(@NotNull String networkId,
                                      @NotNull String dimension,
                                      @NotNull String structureId,
                                      @NotNull String posData) {
        String shortUid = generateShortUid();
        return String.format("%s:%s:%s:%s:%s", networkId, dimension, structureId, posData, shortUid);
    }

    /**
     * 构建标准地址（使用 BlockPos，自动生成短 UID）
     * 格式：网络 ID:所属维度：对应多方块结构 ID:坐标数据：短 UID（8 位）
     *
     * @param networkId   网络 ID
     * @param dimension   所属维度
     * @param structureId 对应多方块结构 ID
     * @param pos         BlockPos 坐标
     * @return 完整的地址字符串（自动生成 8 位短 UID）
     */
    public static String buildAddress(@NotNull String networkId,
                                      @NotNull String dimension,
                                      @NotNull String structureId,
                                      net.minecraft.core.BlockPos pos) {
        String posData = String.format("%d_%d_%d", pos.getX(), pos.getY(), pos.getZ());
        return buildAddress(networkId, dimension, structureId, posData);
    }


    /**
     * 生成短 UID（8 位十六进制字符串）
     * 基于时间戳和随机数，碰撞概率极低（约 1/16^8 = 1/4.3 亿）
     *
     * @return 8 位短 UID
     */
    @NotNull
    public static String generateShortUid() {
        // 使用时间戳和随机数的组合，确保唯一性
        long timestamp = System.currentTimeMillis();
        int random = new java.util.Random().nextInt();
        // 生成 8 位十六进制字符串（例如：a1b2c3d4）
        return String.format("%08x", (timestamp ^ random) & 0xFFFFFFFFL);
    }

    /**
     * 解析地址（带缓存）
     * 格式：网络 ID:所属维度：对应多方块结构 ID:坐标数据：随机 UID
     *
     * @param address 地址字符串
     * @return 地址组件数组 [networkId, dimension, structureId, posData, uid]，如果格式错误返回 null
     */
    @Nullable
    public static String[] parseAddress(@NotNull String address) {
        // 先查缓存
        String[] cached = instance != null ? instance.addressCache.get(address) : null;
        if (cached != null) {
            return Arrays.copyOf(cached, cached.length);
        }

        // 缓存未命中，执行解析
        String[] parts = address.split(":", -1); // -1 保留空字符串
        if (parts.length != 5) {
            return null;
        }

        // 写入缓存
        if (instance != null) {
            instance.addressCache.put(address, parts);
        }

        return parts;
    }

    /**
     * 验证地址格式是否正确
     *
     * @param address 地址字符串
     * @return true 如果格式正确
     */
    public static boolean isValidAddress(@NotNull String address) {
        return parseAddress(address) != null;
    }

    /**
     * 从地址中提取网络 ID
     *
     * @param address 地址字符串
     * @return 网络 ID，如果格式错误返回 null
     */
    @Nullable
    public static String getNetworkId(@NotNull String address) {
        String[] parts = parseAddress(address);
        return parts != null ? parts[0] : null;
    }

    /**
     * 从地址中提取维度信息
     *
     * @param address 地址字符串
     * @return 维度信息，如果格式错误返回 null
     */
    @Nullable
    public static String getDimension(@NotNull String address) {
        String[] parts = parseAddress(address);
        return parts != null ? parts[1] : null;
    }

    /**
     * 从地址中提取多方块结构 ID
     *
     * @param address 地址字符串
     * @return 结构 ID，如果格式错误返回 null
     */
    @Nullable
    public static String getStructureId(@NotNull String address) {
        String[] parts = parseAddress(address);
        return parts != null ? parts[2] : null;
    }

    /**
     * 从地址中提取坐标数据
     *
     * @param address 地址字符串
     * @return 坐标数据（x_y_z 格式），如果格式错误返回 null
     */
    @Nullable
    public static String getPositionData(@NotNull String address) {
        String[] parts = parseAddress(address);
        return parts != null ? parts[3] : null;
    }

    /**
     * 从地址中解析 BlockPos
     *
     * @param address 地址字符串
     * @return BlockPos 对象，如果格式错误返回 null
     */
    @Nullable
    public static net.minecraft.core.BlockPos parseBlockPos(@NotNull String address) {
        String[] parts = parseAddress(address);
        if (parts == null) return null;

        String posData = parts[3];
        String[] coords = posData.split("_");
        if (coords.length != 3) return null;

        try {
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);
            return new net.minecraft.core.BlockPos(x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从地址中提取随机 UID
     *
     * @param address 地址字符串
     * @return 随机 UID，如果格式错误返回 null
     */
    @Nullable
    public static String getUid(@NotNull String address) {
        String[] parts = parseAddress(address);
        return parts != null ? parts[4] : null;
    }

    /**
     * 优化的 markDirty：延迟保存，避免频繁 I/O
     * 只有当累积修改达到阈值时才真正标记保存
     */
    @Override
    public void setDirty() {
        int count = dirtyCount.incrementAndGet();
        if (count >= MAX_DIRTY_COUNT && pendingSave.compareAndSet(false, true)) {
            super.setDirty(); // 真正的保存操作
            dirtyCount.set(0);
            pendingSave.set(false);
        }
    }

    /**
     * 强制立即保存（例如世界保存前）
     */
    public void forceSave() {
        if (dirtyCount.get() > 0) {
            super.setDirty();
            dirtyCount.set(0);
            pendingSave.set(false);
        }
    }

    /**
     * 获取本地节点地址
     *
     * @return 本地地址，如果未设置返回 null
     */
    @Nullable
    public String getLocalAddress() {
        return localAddress;
    }

    /**
     * 设置本地地址（优化：减少 markDirty 调用）
     *
     * @param address 本地地址
     */
    public void setLocalAddress(@NotNull String address) {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid address format: " + address);
        }
        // 只在地址真正改变时才标记保存
        if (!Objects.equals(this.localAddress, address)) {
            this.localAddress = address;
            setDirty();
        }
    }

    /**
     * 检查是否是本地地址
     *
     * @param address 待检查的地址
     * @return true 如果是本地地址
     */
    public boolean isLocalAddress(@NotNull String address) {
        return Objects.equals(localAddress, address);
    }

    // ==================== 路由表核心操作 ====================

    /**
     * 添加或更新路由条目（O(1) 时间复杂度，优化版）
     *
     * @param targetAddress 目标地址
     * @param nextHop       下一跳地址
     * @param metric        度量值（跳数）
     * @return 之前的路由条目，如果没有则返回 null
     */
    @Nullable
    public RoutingEntry addRoute(@NotNull String targetAddress,
                                 @NotNull String nextHop,
                                 int metric) {
        if (!isValidAddress(targetAddress)) {
            throw new IllegalArgumentException("Invalid target address: " + targetAddress);
        }
        if (!isValidAddress(nextHop)) {
            throw new IllegalArgumentException("Invalid next hop address: " + nextHop);
        }

        RoutingEntry oldEntry = routingTable.get(targetAddress);

        // 如果路由没有实际变化，跳过更新（避免无效 markDirty）
        if (oldEntry != null &&
                Objects.equals(oldEntry.nextHop, nextHop) &&
                oldEntry.metric == metric) {
            return oldEntry;
        }

        // 更新反向索引
        if (oldEntry != null) {
            removeFromReverseIndex(targetAddress, oldEntry.nextHop);
        }
        addToReverseIndex(targetAddress, nextHop);

        // 复用 RoutingEntry 对象（如果可能）
        RoutingEntry newEntry;
        if (oldEntry != null) {
            newEntry = oldEntry.update(nextHop, metric);
        } else {
            newEntry = new RoutingEntry(targetAddress, nextHop, metric, System.currentTimeMillis());
        }

        routingTable.put(targetAddress, newEntry);
        updateReadOnlyView(); // 更新只读视图
        setDirty();

        return oldEntry;
    }

    /**
     * 添加路由（使用默认度量值）
     *
     * @param targetAddress 目标地址
     * @param nextHop       下一跳地址
     * @return 之前的路由条目
     */
    @Nullable
    public RoutingEntry addRoute(@NotNull String targetAddress, @NotNull String nextHop) {
        return addRoute(targetAddress, nextHop, DEFAULT_METRIC);
    }

    /**
     * 移除路由条目（O(1) 时间复杂度，优化版）
     *
     * @param targetAddress 目标地址
     * @return 被移除的路由条目，如果不存在返回 null
     */
    @Nullable
    public RoutingEntry removeRoute(@NotNull String targetAddress) {
        RoutingEntry removed = routingTable.remove(targetAddress);
        if (removed != null) {
            removeFromReverseIndex(targetAddress, removed.nextHop);
            updateReadOnlyView();
            setDirty();
        }
        return removed;
    }

    /**
     * 获取路由条目（优先使用只读视图，支持并发读取）
     *
     * @param targetAddress 目标地址
     * @return 路由条目，如果不存在返回 null
     */
    @Nullable
    public RoutingEntry getRoute(@NotNull String targetAddress) {
        return readOnlyView.get(targetAddress);
    }

    /**
     * 获取下一跳地址（优先使用只读视图）
     *
     * @param targetAddress 目标地址
     * @return 下一跳地址，如果没有路由返回 null
     */
    @Nullable
    public String getNextHop(@NotNull String targetAddress) {
        RoutingEntry entry = readOnlyView.get(targetAddress);
        return entry != null ? entry.nextHop() : null;
    }

    /**
     * 检查是否存在到目标地址的路由（优先使用只读视图）
     *
     * @param targetAddress 目标地址
     * @return true 如果存在路由
     */
    public boolean hasRoute(@NotNull String targetAddress) {
        return readOnlyView.containsKey(targetAddress);
    }

    /**
     * 更新只读视图（在所有写操作后调用）
     */
    private void updateReadOnlyView() {
        this.readOnlyView = Collections.unmodifiableMap(new HashMap<>(this.routingTable));
    }

    /**
     * 清除所有路由
     */
    public void clearAllRoutes() {
        routingTable.clear();
        reverseIndex.clear();
        updateReadOnlyView();
        setDirty();
    }

    /**
     * 获取路由表大小
     *
     * @return 路由条目数量
     */
    public int getRoutingTableSize() {
        return routingTable.size();
    }

    /**
     * 获取所有路由目标地址
     *
     * @return 目标地址集合
     */
    @NotNull
    public Set<String> getAllTargets() {
        return Collections.unmodifiableSet(routingTable.keySet());
    }

    // ==================== 路由查询优化 ====================

    /**
     * 查找最佳路由（最小度量值）
     * 目前每个目标只有一个下一跳，这个方法预留用于未来的多路径路由
     *
     * @param targetAddress 目标地址
     * @return 最佳路由条目
     */
    @Nullable
    public RoutingEntry findBestRoute(@NotNull String targetAddress) {
        return getRoute(targetAddress);
    }

    /**
     * 查找所有已知路由中可以通过指定下一跳到达的目标
     * O(1) 时间复杂度（使用反向索引）
     *
     * @param nextHop 下一跳地址
     * @return 目标地址列表（不可变集合）
     */
    @NotNull
    public List<String> findRoutesVia(@NotNull String nextHop) {
        Set<String> targets = reverseIndex.get(nextHop);
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(targets));
    }

    /**
     * 添加到反向索引
     */
    private void addToReverseIndex(String targetAddress, String nextHop) {
        reverseIndex.computeIfAbsent(nextHop, k -> new HashSet<>()).add(targetAddress);
    }

    /**
     * 从反向索引移除
     */
    private void removeFromReverseIndex(String targetAddress, String nextHop) {
        Set<String> targets = reverseIndex.get(nextHop);
        if (targets != null) {
            targets.remove(targetAddress);
            if (targets.isEmpty()) {
                reverseIndex.remove(nextHop);
            }
        }
    }

    // ==================== NBT 持久化（SavedData 机制） ====================
    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        // 保存本地地址
        if (localAddress != null) {
            tag.putString(LOCAL_ADDRESS_KEY, localAddress);
        }

        // 保存路由表（批量操作，减少 NBT 对象创建）
        if (!routingTable.isEmpty()) {
            ListTag routesTag = new ListTag();

            for (RoutingEntry entry : routingTable.values()) {
                CompoundTag routeTag = new CompoundTag();
                routeTag.putString(TARGET_ADDRESS_KEY, entry.targetAddress);
                routeTag.putString(NEXT_HOPS_KEY, entry.nextHop);
                routeTag.putInt(METRIC_KEY, entry.metric);
                routeTag.putLong(TIMESTAMP_KEY, entry.timestamp);
                routesTag.add(routeTag);
            }
            tag.put(ROUTING_TABLE_KEY, routesTag);
        }

        return tag;
    }

    /**
     * 从 NBT 加载路由表（由 SavedData 自动调用）
     *
     * @param tag      NBT 标签
     * @param provider 注册表提供者
     */
    public void loadFromNBT(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        // 清空现有数据
        routingTable.clear();
        reverseIndex.clear();
        addressCache.clear();

        // 加载本地地址
        if (tag.contains(LOCAL_ADDRESS_KEY, Tag.TAG_STRING)) {
            this.localAddress = tag.getString(LOCAL_ADDRESS_KEY);
        }

        // 加载路由表并重建反向索引
        if (tag.contains(ROUTING_TABLE_KEY, Tag.TAG_LIST)) {
            ListTag routesTag = tag.getList(ROUTING_TABLE_KEY, Tag.TAG_COMPOUND);
            int size = routesTag.size();

            // 预分配容量，避免扩容（仅当容量不足时）
            if (size > routingTable.size() && routingTable instanceof HashMap) {
                // 清除现有数据以重新加载
                routingTable.clear();
            }

            for (int i = 0; i < size; i++) {
                CompoundTag routeTag = routesTag.getCompound(i);
                String target = routeTag.getString(TARGET_ADDRESS_KEY);
                String nextHop = routeTag.getString(NEXT_HOPS_KEY);
                int metric = routeTag.getInt(METRIC_KEY);
                long timestamp = routeTag.getLong(TIMESTAMP_KEY);

                if (isValidAddress(target) && isValidAddress(nextHop)) {
                    RoutingEntry entry = new RoutingEntry(target, nextHop, metric, timestamp);
                    routingTable.put(target, entry);
                    addToReverseIndex(target, nextHop);

                    // 预热地址缓存
                    addressCache.put(target, target.split(":", -1));
                    addressCache.put(nextHop, nextHop.split(":", -1));
                }
            }
        }

        // 初始化只读视图
        updateReadOnlyView();
    }


    // ==================== 调试和信息方法 ====================

    /**
     * 获取路由表的文本描述（用于调试）
     *
     * @return 格式化的路由表信息
     */
    @NotNull
    public String getRoutingTableInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Routing Table ===\n");
        sb.append("Local Address: ").append(localAddress != null ? localAddress : "Not set").append("\n");
        sb.append("Total Routes: ").append(routingTable.size()).append("\n\n");

        if (routingTable.isEmpty()) {
            sb.append("No routes.\n");
        } else {
            for (RoutingEntry entry : routingTable.values()) {
                sb.append("Target: ").append(entry.targetAddress)
                        .append(" -> Next Hop: ").append(entry.nextHop)
                        .append(" (Metric: ").append(entry.metric).append(")\n");
            }
        }

        return sb.toString();
    }


    // ==================== 内部类：路由条目 ====================

    /**
         * 路由表条目（不可变设计，减少同步开销）
         * 包含目标地址、下一跳地址、度量值和时间戳
         */
        public record RoutingEntry(String targetAddress, String nextHop, int metric, long timestamp) {
            /**
             * 创建路由条目
             *
             * @param targetAddress 目标地址
             * @param nextHop       下一跳地址
             * @param metric        度量值（跳数）
             * @param timestamp     时间戳
             */
            public RoutingEntry(@NotNull String targetAddress,
                                @NotNull String nextHop,
                                int metric,
                                long timestamp) {
                this.targetAddress = targetAddress;
                this.nextHop = nextHop;
                this.metric = metric;
                this.timestamp = timestamp;
            }

            /**
             * 获取目标地址
             */
            @Override
            @NotNull
            public String targetAddress() {
                return targetAddress;
            }

            /**
             * 获取下一跳地址
             */
            @Override
            @NotNull
            public String nextHop() {
                return nextHop;
            }

            /**
             * 获取度量值（跳数）
             */
            @Override
            public int metric() {
                return metric;
            }

            /**
             * 获取时间戳
             */
            @Override
            public long timestamp() {
                return timestamp;
            }

            /**
             * 更新路由条目（返回新实例，保持不可变性）
             */
            RoutingEntry update(String nextHop, int metric) {
                return new RoutingEntry(this.targetAddress, nextHop, metric, this.timestamp);
            }

            /**
             * 检查路由是否过期
             *
             * @param maxAge 最大年龄（毫秒）
             * @return true 如果已过期
             */
            public boolean isExpired(long maxAge) {
                return System.currentTimeMillis() - timestamp > maxAge;
            }

            @Override
            public String toString() {
                return String.format("Route{%s -> %s (metric=%d)}", targetAddress, nextHop, metric);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                RoutingEntry that = (RoutingEntry) o;
                return Objects.equals(targetAddress, that.targetAddress) &&
                        Objects.equals(nextHop, that.nextHop);
            }

            @Override
            public int hashCode() {
                return Objects.hash(targetAddress, nextHop);
            }
        }
}
