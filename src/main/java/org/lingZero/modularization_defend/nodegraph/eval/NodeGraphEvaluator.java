package org.lingZero.modularization_defend.nodegraph.eval;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.port.IPort;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.graph.GraphModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.*;

import java.util.*;

/**
 * 节点图求值引擎——递归遍历连线，计算图中各节点的输出值。
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * var evaluator = new NodeGraphEvaluator(graphModel);
 * evaluator.setExternalInput("trigger", true);
 * Object result = evaluator.evaluatePort(outputPort);
 * evaluator.clearCache();
 * }</pre>
 */
public class NodeGraphEvaluator {

    /** 节点求值器注册表 */
    static final Map<String, NodeEvaluator> EVALUATORS = new HashMap<>();

    private final GraphModel graph;
    private final Map<String, Object> externalInputs = new HashMap<>();
    private final Map<String, Object> evalCache = new HashMap<>();
    private final Set<String> evaluating = new HashSet<>();

    public NodeGraphEvaluator(GraphModel graph) {
        this.graph = graph;
    }

    public void setExternalInput(String key, Object value) { externalInputs.put(key, value); }
    public Object getExternalInput(String key) { return externalInputs.get(key); }

    public void clearCache() {
        evalCache.clear();
        evaluating.clear();
    }

    // ==================== 端口求值 ====================

    /** 求值指定端口 */
    public Object evaluatePort(PortModel port) {
        if (port == null) return null;
        return switch (port.getDirection()) {
            case OUTPUT -> evaluateNodeOutput(port);
            case INPUT -> evaluateInputPort(port);
            default -> null;
        };
    }

    /** 求值输入端口：有连线追溯源端口，否则取内嵌常量 */
    private Object evaluateInputPort(PortModel inputPort) {
        if (inputPort.isConnected()) {
            IPort src = inputPort.getFirstConnectedPort();
            if (src instanceof PortModel pm) {
                return evaluatePort(pm);
            }
        }
        var embedded = inputPort.getEmbeddedValue();
        if (embedded != null && inputPort.getDataTypeHandle() != null) {
            var result = embedded.tryGetValue(inputPort.getDataTypeHandle().resolve());
            if (result.result().isPresent()) return result.result().get();
        }
        return null;
    }

    /** 求值节点输出端口 */
    private Object evaluateNodeOutput(PortModel outputPort) {
        AbstractNodeModel node = outputPort.getNodeModel();
        if (node == null) return null;

        String cacheKey = node.getUid() + ":" + outputPort.getPortId();
        if (evalCache.containsKey(cacheKey)) return evalCache.get(cacheKey);
        if (evaluating.contains(cacheKey)) return null;

        evaluating.add(cacheKey);
        String nodeName = getNodeName(node);
        var ctx = new EvalCtx(this, node, outputPort.getPortId());

        Object result;
        if (EVALUATORS.containsKey(nodeName)) {
            result = EVALUATORS.get(nodeName).evaluate(ctx);
        } else {
            result = null;
        }

        evaluating.remove(cacheKey);
        evalCache.put(cacheKey, result);
        return result;
    }

    /** 获取节点 @NodeAttribute name */
    private String getNodeName(AbstractNodeModel nodeModel) {
        if (nodeModel instanceof ICustomNodeModel cn && cn.getNode() != null) {
            var attr = cn.getNode().getClass().getAnnotation(
                    com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute.class);
            if (attr != null) return attr.name();
        }
        return nodeModel.getClass().getSimpleName();
    }

    // ==================== 辅助方法 ====================

    public Object getInputPortValue(AbstractNodeModel node, String portId) {
        if (node instanceof NodeModel nm) {
            PortModel port = nm.getInputsById().get(portId);
            if (port != null) return evaluateInputPort(port);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOptionValue(AbstractNodeModel node, String optionId) {
        if (node instanceof InputOutputPortsNodeModel iop) {
            var option = iop.getNodeOptionById(optionId);
            if (option != null) {
                var type = option.getDataType();
                if (type instanceof Class<?> clazz) {
                    var r = option.tryGetValue(clazz);
                    if (r.result().isPresent()) return (T) r.result().get();
                }
            }
        }
        return null;
    }

    public List<PortModel> getDynamicInputPorts(AbstractNodeModel node) {
        if (node instanceof NodeModel nm) {
            return nm.getInputsByDisplayOrder();
        }
        return List.of();
    }

    public static void registerEvaluator(String nodeName, NodeEvaluator evaluator) {
        EVALUATORS.put(nodeName, evaluator);
    }

    // ==================== 类型 ====================

    /** 节点求值器函数接口 */
    @FunctionalInterface
    public interface NodeEvaluator {
        Object evaluate(EvalCtx ctx);
    }

    /** 求值上下文 */
    public record EvalCtx(NodeGraphEvaluator evaluator, AbstractNodeModel node, String portId) {}
}
