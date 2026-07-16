package org.lingZero.modularization_defend.nodegraph.node;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.Capabilities;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.NodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IOptionDefinitionContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

/**
 * 阈值门节点——当输入值超过设定阈值时输出 true。
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输入 value (int)</b> — 待比较的数值</li>
 *   <li><b>输出 result (bool)</b> — 是否超过阈值</li>
 * </ul>
 *
 * <h3>选项</h3>
 * <ul>
 *   <li><b>threshold (int)</b> — 触发阈值，默认 3</li>
 * </ul>
 */
@NodeAttribute(name = "阈值门", group = "判断", graphTypes = {TurretLogicGraph.class})
public class ThresholdGateNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.threshold_gate");
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("threshold", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.threshold"))
                .withDefaultValue(3);
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addInputPort("value", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.input_value"))
                .build();
        context.addOutputPort("result", Boolean.class)
                .withDisplayName(Component.translatable("node.modularization_defend.result"))
                .build();
    }
}
