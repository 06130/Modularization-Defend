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
 * 整数值节点——输出一个可配置的整型常量值。
 *
 * <p>这是一个"源"节点：没有输入端口，仅输出一个整数值。</p>
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输出 value (int)</b> — 配置的整数值</li>
 * </ul>
 *
 * <h3>选项</h3>
 * <ul>
 *   <li><b>value (int)</b> — 输出值，默认 0</li>
 * </ul>
 */
@NodeAttribute(name = "整数值", group = "常量", graphTypes = {TurretLogicGraph.class})
public class TargetCountNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.int_value");
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("value", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.int_value_option"))
                .withDefaultValue(0);
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addOutputPort("value", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.int_value"))
                .build();
    }
}
