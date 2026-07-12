package org.lingZero.modularization_defend.nodegraph.node;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.Capabilities;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.NodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IOptionDefinitionContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

import java.util.Optional;

/**
 * 或门节点——任一输入为 true 时输出 true，全为 false 时输出 false。
 *
 * <h3>选项</h3>
 * <ul><li><b>inputs (int)</b> — 输入端口数量，默认 2</li></ul>
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输入 in1..inN (bool)</b> — 逻辑输入</li>
 *   <li><b>输出 result (bool)</b> — 或运算结果</li>
 * </ul>
 */
@NodeAttribute(name = "或门", group = "逻辑", graphTypes = {TurretLogicGraph.class})
public class OrGateNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.or_gate");
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("inputs", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.input_count"))
                .withDefaultValue(2);
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        Optional.ofNullable(getNodeOptionById("inputs")).ifPresent(o ->
            o.tryGetValue(Integer.class).ifSuccess(inputs -> {
                int count = inputs instanceof Integer i ? Math.max(1, Math.min(i, 16)) : 2;
                for (int i = 0; i < count; i++) {
                    context.addInputPort("in" + (i + 1), Boolean.class)
                            .withDisplayName(Component.literal("IN " + (i + 1)))
                            .build();
                }
            })
        );
        context.addOutputPort("result", Boolean.class)
                .withDisplayName(Component.translatable("node.modularization_defend.result"))
                .build();
    }
}
