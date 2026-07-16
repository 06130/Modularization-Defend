package org.lingZero.modularization_defend.nodegraph.exec;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.type.TypeHandles;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;

/**
 * 关卡动作节点基类——执行流的中间/末端环节。
 *
 * <p>持有 {@code exec} 执行输入端口和 {@code then} 执行输出端口。
 * 被上游触发时执行注册在 {@link LevelExecEngine} 中的执行器，
 * 随后继续触发 {@code then} 下游。参数可来自选项或数据输入端口。</p>
 */
public abstract class LevelActionNode extends Node {

    /** 执行流输入端口 ID */
    public static final String PORT_EXEC = "exec";
    /** 执行流输出端口 ID */
    public static final String PORT_THEN = "then";

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addInputPort(PORT_EXEC, TypeHandles.EXECUTION_FLOW)
                .withDisplayName(Component.translatable("node.modularization_defend.exec_in"))
                .build();
        defineExecOutputs(context);
        defineExtraPorts(context);
    }

    /** 定义执行流输出端口，默认为单个 then；分支类节点可覆写为多个出口 */
    protected void defineExecOutputs(IPortDefinitionContext context) {
        context.addOutputPort(PORT_THEN, TypeHandles.EXECUTION_FLOW)
                .withDisplayName(Component.translatable("node.modularization_defend.exec_then"))
                .build();
    }

    /** 子类在此定义额外的数据端口（如参数输入） */
    protected void defineExtraPorts(IPortDefinitionContext context) {
    }
}
