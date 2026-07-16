package org.lingZero.modularization_defend.nodegraph.exec;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.AbstractNodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.ICustomNodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.NodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.PortModel;
import org.lingZero.modularization_defend.nodegraph.eval.NodeGraphEvaluator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * 执行流引擎——维护事件条件与动作执行器注册表，并负责沿执行连线遍历动作链。
 *
 * <p>注册表 key 为节点 {@link NodeAttribute#name()}，与
 * {@link NodeGraphEvaluator} 的求值器注册方式保持一致。</p>
 */
public final class LevelExecEngine {

    /** 链式执行深度上限，防止图中出现执行环导致死循环 */
    private static final int MAX_CHAIN_DEPTH = 64;

    /** 事件节点触发条件注册表 */
    private static final Map<String, Predicate<LevelExecContext>> CONDITIONS = new HashMap<>();
    /** 动作节点执行器注册表 */
    private static final Map<String, ActionExecutor> EXECUTORS = new HashMap<>();

    private LevelExecEngine() {}

    public static void registerCondition(String nodeName, Predicate<LevelExecContext> condition) {
        CONDITIONS.put(nodeName, condition);
    }

    public static void registerExecutor(String nodeName, ActionExecutor executor) {
        EXECUTORS.put(nodeName, executor);
    }

    /** 判断事件节点当前条件是否满足（未注册条件视为不满足） */
    public static boolean testCondition(AbstractNodeModel eventNode, ILevelHost host, NodeGraphEvaluator evaluator) {
        var condition = CONDITIONS.get(nodeName(eventNode));
        return condition != null && condition.test(new LevelExecContext(host, evaluator, eventNode));
    }

    /** 从事件节点出发，沿 then 执行连线依次执行下游动作链 */
    public static void triggerChain(AbstractNodeModel eventNode, ILevelHost host, NodeGraphEvaluator evaluator) {
        followPort(eventNode, LevelEventNode.PORT_THEN, host, evaluator, new HashSet<>(), 0);
    }

    /** 从指定节点的指定输出端口恢复执行链（异步节点延迟/间隔波次完成后由宿主调用） */
    public static void resumeFrom(AbstractNodeModel node, String portId, ILevelHost host, NodeGraphEvaluator evaluator) {
        followPort(node, portId, host, evaluator, new HashSet<>(), 0);
    }

    /** 沿 node 的 portId 输出端口执行下游动作，每个动作返回下一个要跟随的端口 ID（null 中断） */
    private static void followPort(AbstractNodeModel node, String portId, ILevelHost host,
                                   NodeGraphEvaluator evaluator, Set<UUID> visited, int depth) {
        if (depth > MAX_CHAIN_DEPTH) return;
        if (!(node instanceof NodeModel nm)) return;
        PortModel outputPort = nm.getOutputsById().get(portId);
        if (outputPort == null) return;

        for (PortModel target : outputPort.getConnectedPorts()) {
            AbstractNodeModel targetNode = target.getNodeModel();
            if (targetNode == null || !visited.add(targetNode.getUid())) continue;

            var executor = EXECUTORS.get(nodeName(targetNode));
            if (executor == null) continue;
            String nextPort = executor.execute(new LevelExecContext(host, evaluator, targetNode));
            if (nextPort != null) {
                followPort(targetNode, nextPort, host, evaluator, visited, depth + 1);
            }
        }
    }

    /** 获取节点的 @NodeAttribute name（注册表 key） */
    public static String nodeName(AbstractNodeModel nodeModel) {
        if (nodeModel instanceof ICustomNodeModel cn && cn.getNode() != null) {
            var attr = cn.getNode().getClass().getAnnotation(NodeAttribute.class);
            if (attr != null) return attr.name();
        }
        return nodeModel.getClass().getSimpleName();
    }

    /** 判断节点模型是否为关卡事件节点 */
    public static boolean isEventNode(AbstractNodeModel nodeModel) {
        return nodeModel instanceof ICustomNodeModel cn && cn.getNode() instanceof LevelEventNode;
    }

    /**
     * 动作执行器函数接口。
     *
     * @return 执行后要继续跟随的输出端口 ID（通常为 {@code "then"}），
     *         返回 null 表示中断本链（异步节点应自行通过宿主调度恢复）
     */
    @FunctionalInterface
    public interface ActionExecutor {
        String execute(LevelExecContext ctx);
    }
}
