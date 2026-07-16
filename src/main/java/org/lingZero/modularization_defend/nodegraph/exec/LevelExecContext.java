package org.lingZero.modularization_defend.nodegraph.exec;

import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.AbstractNodeModel;
import org.lingZero.modularization_defend.nodegraph.eval.NodeGraphEvaluator;

/**
 * 执行流上下文——事件条件判断与动作执行时传入，
 * 提供对宿主、求值器和当前节点的访问，以及常用取值辅助方法。
 */
public record LevelExecContext(ILevelHost host, NodeGraphEvaluator evaluator, AbstractNodeModel node) {

    /** 求值当前节点的输入端口（连线值或内嵌常量） */
    public Object input(String portId) {
        return evaluator.getInputPortValue(node, portId);
    }

    /** 读取当前节点的选项值 */
    public <T> T option(String optionId) {
        return evaluator.getOptionValue(node, optionId);
    }

    public boolean inputAsBool(String portId) {
        return input(portId) instanceof Boolean b && b;
    }

    public int asInt(Object val, int fallback) {
        return val instanceof Number n ? n.intValue() : fallback;
    }

    public float asFloat(Object val, float fallback) {
        return val instanceof Number n ? n.floatValue() : fallback;
    }

    /** 当前节点的 uid 字符串（异步任务恢复执行链时定位节点用） */
    public String nodeUid() {
        return node.getUid().toString();
    }
}
