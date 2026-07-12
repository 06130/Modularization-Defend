package org.lingZero.modularization_defend.nodegraph.node;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.Capabilities;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.NodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

/**
 * 异或门节点——两个输入不同时输出 true，相同时输出 false。
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输入 a, b (bool)</b></li>
 *   <li><b>输出 out (bool)</b> — 异或结果</li>
 * </ul>
 */
@NodeAttribute(name = "异或门", group = "逻辑", graphTypes = {TurretLogicGraph.class})
public class XorNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.xor_gate");
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addInputPort("a", Boolean.class)
                .withDisplayName(Component.literal("A"))
                .build();
        context.addInputPort("b", Boolean.class)
                .withDisplayName(Component.literal("B"))
                .build();
        context.addOutputPort("out", Boolean.class)
                .withDisplayName(Component.literal("OUT"))
                .build();
    }
}
