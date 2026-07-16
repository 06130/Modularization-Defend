package org.lingZero.modularization_defend.nodegraph.exec;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.type.TypeHandles;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;

/**
 * 关卡事件节点基类——执行流的起点。
 *
 * <p>宿主每 tick 检查其触发条件（通过 {@link LevelExecEngine} 注册的条件谓词求值），
 * 条件由假变真（上升沿）时，沿 {@code then} 执行端口触发下游动作链。</p>
 */
public abstract class LevelEventNode extends Node {

    /** 执行流输出端口 ID */
    public static final String PORT_THEN = "then";

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addOutputPort(PORT_THEN, TypeHandles.EXECUTION_FLOW)
                .withDisplayName(Component.translatable("node.modularization_defend.exec_then"))
                .build();
        defineExtraPorts(context);
    }

    /** 子类在此定义额外的数据端口（如条件输入） */
    protected void defineExtraPorts(IPortDefinitionContext context) {
    }
}
