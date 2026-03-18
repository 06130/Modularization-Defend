package org.lingZero.modularization_defend.AgreementCoreNetwork;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.lingZero.modularization_defend.ModularizationDefend;

/**
 * 路由网络调试命令处理器
 * <p>
 * 提供完整的调试命令集，用于测试和验证路由网络系统的各项功能
 * 命令结构：/md networkdebug <子命令> [参数]
 * </p>
 * 
 * <p>支持的子命令：</p>
 * <ul>
 *     <li>status - 显示路由系统状态</li>
 *     <li>topology - 显示网络拓扑结构</li>
 *     <li>sendpacket - 发送测试数据包</li>
 *     <li>register - 注册当前节点到网络</li>
 *     <li>unregister - 从网络注销当前节点</li>
 *     <li>path - 显示两节点间的路径</li>
 *     <li>discoveroute - 测试路由发现功能</li>
 *     <li>clearroutes - 清除所有路由条目</li>
 *     <li>nodeinfo - 显示指定节点的详细信息</li>
 *     <li>heartbeat - 手动发送心跳包</li>
 * </ul>
 * 
 * @author Modularization Defend Team
 * @version 1.0
 */
public class NetworkDebugCommand {
    
    /**
     * 注册所有调试命令到命令分发器
     * <p>
     * 命令层级结构：
     * <pre>
     * /md
     * └── networkdebug
     *     ├── status
     *     ├── topology
     *     ├── sendpacket <target>
     *     ├── register
     *     ├── unregister
     *     ├── path <source> <target>
     *     ├── discoveroute <target>
     *     ├── clearroutes
     *     ├── nodeinfo <address>
     *     └── heartbeat
     * </pre>
     * </p>
     * 
     * @param dispatcher 命令分发器，由服务器启动时提供
     * @apiNote 需要权限等级 2（管理员）才能执行命令
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 创建主命令 /md，需要管理员权限（等级 2）
        LiteralArgumentBuilder<CommandSourceStack> mdCommand = Commands.literal("md")
            .requires((source) -> source.hasPermission(2));
        
        // 创建子命令 networkdebug，同样需要管理员权限
        LiteralArgumentBuilder<CommandSourceStack> networkDebugCommand = Commands.literal("networkdebug")
            .requires((source) -> source.hasPermission(2));
        
        // ========== 子命令：status - 显示路由系统状态 ==========
        // 用法：/md networkdebug status
        // 返回：本地地址、路由表大小、活跃节点数、网络直径、连通分量数
        networkDebugCommand.then(Commands.literal("status")
            .executes(NetworkDebugCommand::showStatus));
        
        // ========== 子命令：topology - 显示网络拓扑结构 ==========
        // 用法：/md networkdebug topology
        // 返回：所有节点列表及其连接关系
        networkDebugCommand.then(Commands.literal("topology")
            .executes(NetworkDebugCommand::showTopology));
        
        // ========== 子命令：sendpacket - 发送测试数据包 ==========
        // 用法：/md networkdebug sendpacket <目标地址>
        // 参数：target - 目标节点的完整地址（支持自动补全）
        // 功能：创建并发送一个 DATA_TRANSFER 类型的测试数据包
        networkDebugCommand.then(Commands.literal("sendpacket")
            .then(Commands.argument("target", StringArgumentType.string())
                // 添加自动补全建议，列出所有已知的活跃节点地址
                .suggests((context, builder) -> {
                    NetworkManager manager = NetworkMain.getNetworkManager();
                    for (NetworkManager.NodeInfo node : manager.getActiveNodes()) {
                        builder.suggest(node.address);
                    }
                    return builder.buildFuture();
                })
                // 执行发送测试数据包逻辑
                .executes(NetworkDebugCommand::sendTestPacket)));
        
        // ========== 子命令：register - 注册当前节点 ==========
        // 用法：/md networkdebug register
        // 功能：将玩家当前位置注册为调试节点
        networkDebugCommand.then(Commands.literal("register")
            .executes(NetworkDebugCommand::registerCurrentNode));
        
        // ========== 子命令：unregister - 注销当前节点 ==========
        // 用法：/md networkdebug unregister
        // 功能：从路由网络中注销当前本地节点
        networkDebugCommand.then(Commands.literal("unregister")
            .executes(NetworkDebugCommand::unregisterCurrentNode));
        
        // ========== 子命令：path - 显示路径信息 ==========
        // 用法：/md networkdebug path <源地址> <目标地址>
        // 参数：source - 起始节点地址
        //      target - 目标节点地址
        // 返回：两点之间的完整路径（BFS 算法计算）
        networkDebugCommand.then(Commands.literal("path")
            .then(Commands.argument("source", StringArgumentType.string())
                .then(Commands.argument("target", StringArgumentType.string())
                    .executes(NetworkDebugCommand::showPath))));
        
        // ========== 子命令：discoveroute - 测试路由发现 ==========
        // 用法：/md networkdebug discoveroute <目标地址>
        // 功能：发起路由发现请求，用于测试动态路由发现协议
        networkDebugCommand.then(Commands.literal("discoveroute")
            .then(Commands.argument("target", StringArgumentType.string())
                .executes(NetworkDebugCommand::testRouteDiscovery)));
        
        // ========== 子命令：clearroutes - 清除所有路由 ==========
        // 用法：/md networkdebug clearroutes
        // 功能：清空路由表中的所有条目，用于重置网络状态
        networkDebugCommand.then(Commands.literal("clearroutes")
            .executes(NetworkDebugCommand::clearAllRoutes));
        
        // ========== 子命令：nodeinfo - 显示节点信息 ==========
        // 用法：/md networkdebug nodeinfo <节点地址>
        // 参数：address - 要查询的节点完整地址
        // 返回：节点的详细信息（位置、在线状态、类型等）
        networkDebugCommand.then(Commands.literal("nodeinfo")
            .then(Commands.argument("address", StringArgumentType.string())
                .executes(NetworkDebugCommand::showNodeInfo)));
        
        // ========== 子命令：heartbeat - 发送心跳 ==========
        // 用法：/md networkdebug heartbeat
        // 功能：手动触发心跳包发送，用于测试节点存活检测机制
        networkDebugCommand.then(Commands.literal("heartbeat")
            .executes(NetworkDebugCommand::sendHeartbeat));
        
        // 将 networkdebug 作为子命令添加到 md 命令下
        mdCommand.then(networkDebugCommand);
        
        // 注册到命令分发器，完成命令注册流程
        dispatcher.register(mdCommand);
    }
    
    /**
     * 显示路由系统当前状态
     * <p>
     * 输出信息包括：
     * - 本地节点地址
     * - 路由表条目数量
     * - 活跃节点数量
     * - 网络直径（最长最短路径）
     * - 连通分量数量
     * </p>
     * 
     * @param context 命令上下文，包含命令源和参数
     * @return 命令执行结果代码（1=成功，0=失败）
     */
    private static int showStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            // 获取路由系统的核心组件实例
            var database = NetworkMain.getDatabase();
            var handler = NetworkMain.getRoutingHandler();
            var manager = NetworkMain.getNetworkManager();
            
            // 输出格式化的状态信息
            source.sendSuccess(() -> Component.literal("========== 路由网络状态 =========="), true);
            source.sendSuccess(() -> Component.literal("本地地址：" + NetworkMain.getLocalAddress()), true);
            source.sendSuccess(() -> Component.literal("路由表大小：" + database.getRoutingTableSize()), true);
            source.sendSuccess(() -> Component.literal("活跃节点数：" + manager.getActiveNodes().size()), true);
            source.sendSuccess(() -> Component.literal("网络直径：" + manager.getNetworkDiameter()), true);
            source.sendSuccess(() -> Component.literal("连通分量数：" + manager.getConnectedComponentCount()), true);
            source.sendSuccess(() -> Component.literal("================================"), true);
            
            return 1; // 返回成功
        } catch (Exception e) {
            // 捕获异常并输出错误信息
            source.sendSuccess(() -> Component.literal("获取状态失败：" + e.getMessage()), true);
            return 0; // 返回失败
        }
    }
    
    /**
     * 显示当前网络的拓扑结构
     * <p>
     * 遍历所有活跃节点，显示每个节点的：
     * - 完整地址
     * - 坐标位置
     * - 邻居节点列表（直接连接的节点）
     * </p>
     * 
     * @param context 命令上下文
     * @return 命令执行结果代码
     */
    private static int showTopology(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            var manager = NetworkMain.getNetworkManager();
            var topology = manager.getNetworkTopology();
            
            source.sendSuccess(() -> Component.literal("========== 网络拓扑 =========="), true);
            source.sendSuccess(() -> Component.literal("节点数量：" + topology.getNodes().size()), true);
            
            // 遍历所有节点
            for (var entry : topology.getNodes().entrySet()) {
                String address = entry.getKey();
                BlockPos pos = entry.getValue();
                source.sendSuccess(() -> Component.literal("  节点：" + address + " @ " + pos), true);
                
                // 显示该节点的邻居节点
                var neighbors = topology.getNeighbors(address);
                if (!neighbors.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("    邻居：" + String.join(", ", neighbors)), true);
                }
            }
            
            source.sendSuccess(() -> Component.literal("================================"), true);
            
            return 1;
        } catch (Exception e) {
            source.sendSuccess(() -> Component.literal("获取拓扑失败：" + e.getMessage()), true);
            return 0;
        }
    }
    
    /**
     * 发送测试数据包到指定目标节点
     * <p>
     * 创建一个 DATA_TRANSFER 类型的数据包，包含：
     * - 测试消息
     * - 时间戳
     * - 优先级：3（中等）
     * </p>
     * 
     * @param context 命令上下文
     * @return 命令执行结果代码
     */
    private static int sendTestPacket(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String targetAddress = StringArgumentType.getString(context, "target");
        
        try {
            var handler = NetworkMain.getRoutingHandler();
            String localAddress = NetworkMain.getLocalAddress();
            
            // 检查本地是否已注册
            if (localAddress.isEmpty()) {
                source.sendSuccess(() -> Component.literal("错误：本地未注册，请先使用 /networkdebug register"), true);
                return 0;
            }
            
            // 创建测试数据包负载（NBT 格式）
            var payload = new net.minecraft.nbt.CompoundTag();
            payload.putString("Message", "Test packet from " + localAddress);
            payload.putLong("Timestamp", System.currentTimeMillis());
            
            // 构建数据包对象
            RoutingPacket packet = new RoutingPacket(
                RoutingPacket.PacketType.DATA_TRANSFER,  // 数据类型：数据传输
                localAddress,      // 源地址
                targetAddress,     // 目标地址
                localAddress,      // 初始下一跳（自己）
                0,                 // 初始跳数
                3,                 // 优先级（0-7，3 为中等）
                payload            // 负载数据
            );
            
            // 通过路由处理器发送数据包
            handler.sendPacket(packet);
            
            source.sendSuccess(() -> Component.literal("✓ 测试数据包已发送：" + packet), true);
            
            return 1;
        } catch (Exception e) {
            source.sendSuccess(() -> Component.literal("发送数据包失败：" + e.getMessage()), true);
            return 0;
        }
    }
    
    /**
     * 将玩家当前位置注册为网络节点
     * <p>
     * 执行流程：
     * 1. 获取玩家当前位置
     * 2. 构建标准节点地址（五段式格式）
     * 3. 获取当前位置的方块实体
     * 4. 注册到路由网络
     * </p>
     * 
     * @param context 命令上下文
     * @return 命令执行结果代码
     */
    private static int registerCurrentNode(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            // 获取执行命令的玩家
            ServerPlayer player = source.getPlayerOrException();
            BlockPos pos = player.blockPosition();
            
            // 使用 NetworkMain 工具类构建标准地址
            // 格式：modularization_defend:minecraft:overworld:debug_node:x_y_z:uid
            String nodeAddress = NetworkMain.buildNodeAddress(
                ModularizationDefend.MODID,  // 网络 ID（modid）
                "debug_node",                 // 结构类型（调试节点）
                pos                           // 坐标
            );
            
            // 获取当前位置的方块实体
            var blockEntity = player.level().getBlockEntity(pos);
            
            // 如果有方块实体，则注册到网络
            if (blockEntity != null) {
                NetworkMain.registerNode(nodeAddress, blockEntity);
                source.sendSuccess(() -> Component.literal("✓ 节点已注册：" + nodeAddress), true);
            } else {
                source.sendSuccess(() -> Component.literal("错误：当前位置没有方块实体"), true);
                return 0;
            }
            
            return 1;
        } catch (Exception e) {
            source.sendSuccess(() -> Component.literal("注册节点失败：" + e.getMessage()), true);
            return 0;
        }
    }
    
    /**
     * 从路由网络注销当前节点
     * <p>
     * 执行流程：
     * 1. 获取本地节点地址
     * 2. 调用 NetworkMain.unregisterNode 注销
     * 3. 清除本地地址
     * </p>
     * 
     * @param context 命令上下文
     * @return 命令执行结果代码
     */
    private static int unregisterCurrentNode(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            String localAddress = NetworkMain.getLocalAddress();
            
            // 检查是否已注册
            if (!localAddress.isEmpty()) {
                NetworkMain.unregisterNode(localAddress);
                source.sendSuccess(() -> Component.literal("✓ 节点已注销：" + localAddress), true);
            } else {
                source.sendSuccess(() -> Component.literal("错误：未注册任何节点"), true);
                return 0;
            }
            
            return 1;
        } catch (Exception e) {
            source.sendSuccess(() -> Component.literal("注销节点失败：" + e.getMessage()), true);
            return 0;
        }
    }
    
    /**
     * 显示两个节点之间的路径信息
     * <p>
     * 使用 BFS（广度优先搜索）算法计算最短路径
     * 输出包括：
     * - 起点和终点
     * - 路径长度（跳数）
     * - 路径上经过的所有节点
     * </p>
     * 
     * @param context 命令上下文
     * @return 命令执行结果代码
     */
    private static int showPath(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String src = StringArgumentType.getString(context, "source");
        String target = StringArgumentType.getString(context, "target");
        
        try {
            var manager = NetworkMain.getNetworkManager();
            // 调用网络管理器的路径查找方法
            var path = manager.findPath(src, target);
            
            source.sendSuccess(() -> Component.literal("========== 路径信息 =========="), true);
            source.sendSuccess(() -> Component.literal("从：" + src), true);
            source.sendSuccess(() -> Component.literal("到：" + target), true);
            
            if (path.isEmpty()) {
                source.sendSuccess(() -> Component.literal("路径：无可用路径"), true);
            } else {
                source.sendSuccess(() -> Component.literal("路径长度：" + (path.size() - 1) + " 跳"), true);
                source.sendSuccess(() -> Component.literal("路径："), true);
                // 遍历路径上的每个节点
                for (int i = 0; i < path.size(); i++) {
                    final int index = i; // 使变量在 lambda 中有效 final
                    source.sendSuccess(() -> Component.literal("  " + index + ". " + path.get(index)), true);
                }
            }
            
            source.sendSuccess(() -> Component.literal("================================"), true);
            
            return 1;
        } catch (Exception e) {
            source.sendSuccess(() -> Component.literal("获取路径失败：" + e.getMessage()), true);
            return 0;
        }
    }
    
    /**
     * 测试路由发现功能
     * <p>
     * 创建一个 ROUTE_REQUEST 类型的数据包并广播
     * 用于测试动态路由发现协议
     * </p>
     * 
     * @param context 命令上下文
     * @return 命令执行结果代码
     */
    private static int testRouteDiscovery(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String targetAddress = StringArgumentType.getString(context, "target");
        
        try {
            var handler = NetworkMain.getRoutingHandler();
            String localAddress = NetworkMain.getLocalAddress();
            
            if (localAddress.isEmpty()) {
                source.sendSuccess(() -> Component.literal("错误：本地未注册"), true);
                return 0;
            }
            
            // 创建路由发现请求数据包
            RoutingPacket request = new RoutingPacket(
                RoutingPacket.PacketType.ROUTE_REQUEST,  // 类型：路由发现请求
                localAddress,      // 源地址
                targetAddress,     // 目标地址
                localAddress,      // 初始下一跳
                0,                 // 初始跳数
                3,                 // 优先级
                null               // 无负载
            );
            
            // 发送数据包
            handler.sendPacket(request);
            
            source.sendSuccess(() -> Component.literal("✓ 路由发现请求已发送：" + request), true);
            
            return 1;
        } catch (Exception e) {
            source.sendSuccess(() -> Component.literal("路由发现失败：" + e.getMessage()), true);
            return 0;
        }
    }
    
    /**
     * 清除路由表中的所有条目
     * <p>
     * 用于重置网络状态，测试路由重建功能
     * </p>
     * 
     * @param context 命令上下文
     * @return 命令执行结果代码
     */
    private static int clearAllRoutes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            var database = NetworkMain.getDatabase();
            // 调用数据库的清除方法
            database.clearAllRoutes();
            
            source.sendSuccess(() -> Component.literal("✓ 已清除所有路由"), true);
            
            return 1;
        } catch (Exception e) {
            source.sendSuccess(() -> Component.literal("清除路由失败：" + e.getMessage()), true);
            return 0;
        }
    }
    
    /**
     * 显示指定节点的详细信息
     * <p>
     * 输出信息包括：
     * - 节点地址
     * - 坐标位置
     * - 在线状态
     * - 节点类型
     * - 网络 ID
     * - 所属维度
     * </p>
     * 
     * @param context 命令上下文
     * @return 命令执行结果代码
     */
    private static int showNodeInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String address = StringArgumentType.getString(context, "address");
        
        try {
            var manager = NetworkMain.getNetworkManager();
            var nodeInfo = manager.getNodeInfo(address);
            
            if (nodeInfo != null) {
                source.sendSuccess(() -> Component.literal("========== 节点信息 =========="), true);
                source.sendSuccess(() -> Component.literal("地址：" + nodeInfo.address), true);
                source.sendSuccess(() -> Component.literal("位置：" + nodeInfo.position), true);
                source.sendSuccess(() -> Component.literal("在线：" + (nodeInfo.isOnline ? "是" : "否")), true);
                source.sendSuccess(() -> Component.literal("类型：" + nodeInfo.getNodeType()), true);
                source.sendSuccess(() -> Component.literal("网络 ID: " + nodeInfo.getNetworkId()), true);
                source.sendSuccess(() -> Component.literal("维度：" + nodeInfo.getDimension()), true);
                source.sendSuccess(() -> Component.literal("================================"), true);
            } else {
                source.sendSuccess(() -> Component.literal("未找到节点：" + address), true);
                return 0;
            }
            
            return 1;
        } catch (Exception e) {
            source.sendSuccess(() -> Component.literal("获取节点信息失败：" + e.getMessage()), true);
            return 0;
        }
    }
    
    /**
     * 手动发送心跳包
     * <p>
     * 用于测试节点存活检测机制
     * 心跳包会广播到所有邻居节点
     * </p>
     * 
     * @param context 命令上下文
     * @return 命令执行结果代码
     */
    private static int sendHeartbeat(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            var manager = NetworkMain.getNetworkManager();
            // 调用网络管理器的心跳方法
            manager.sendHeartbeat();
            
            source.sendSuccess(() -> Component.literal("✓ 心跳已发送"), true);
            
            return 1;
        } catch (Exception e) {
            source.sendSuccess(() -> Component.literal("发送心跳失败：" + e.getMessage()), true);
            return 0;
        }
    }
}
