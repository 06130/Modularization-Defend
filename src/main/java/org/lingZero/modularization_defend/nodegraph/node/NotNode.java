package org.lingZero.modularization_defend.nodegraph.node;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.Capabilities;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.NodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

/**
 * 非门节点——对输入取反，true→false，false→true。
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输入 in (bool)</b></li>
 *   <li><b>输出 out (bool)</b> — 取反结果</li>
 * </ul>
 */
@NodeAttribute(name = "非门", group = "逻辑", graphTypes = {TurretLogicGraph.class})
public class NotNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.not_gate");
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addInputPort("in", Boolean.class)
                .withDisplayName(Component.literal("IN"))
                .build();
        context.addOutputPort("out", Boolean.class)
                .withDisplayName(Component.literal("OUT"))
                .build();
    }
}
