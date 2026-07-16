package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.type.TypeHandles;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.exec.LevelActionNode;

/**
 * 条件分支动作节点——if 判定：被触发时求值 condition 输入，
 * 为真沿 onTrue 出口继续，为假沿 onFalse 出口继续。
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输入 exec (exec)</b> — 执行流入口</li>
 *   <li><b>输入 condition (bool)</b> — 判定条件</li>
 *   <li><b>输出 onTrue (exec)</b> — 条件为真时触发</li>
 *   <li><b>输出 onFalse (exec)</b> — 条件为假时触发</li>
 * </ul>
 */
@NodeAttribute(name = "条件分支", group = "关卡/动作", graphTypes = {TurretLogicGraph.class})
public class BranchNode extends LevelActionNode {

    public static final String PORT_TRUE = "onTrue";
    public static final String PORT_FALSE = "onFalse";

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.branch");
    }

    @Override
    protected void defineExecOutputs(IPortDefinitionContext context) {
        context.addOutputPort(PORT_TRUE, TypeHandles.EXECUTION_FLOW)
                .withDisplayName(Component.translatable("node.modularization_defend.branch_true"))
                .build();
        context.addOutputPort(PORT_FALSE, TypeHandles.EXECUTION_FLOW)
                .withDisplayName(Component.translatable("node.modularization_defend.branch_false"))
                .build();
    }

    @Override
    protected void defineExtraPorts(IPortDefinitionContext context) {
        context.addInputPort("condition", Boolean.class)
                .withDisplayName(Component.translatable("node.modularization_defend.condition"))
                .build();
    }
}
