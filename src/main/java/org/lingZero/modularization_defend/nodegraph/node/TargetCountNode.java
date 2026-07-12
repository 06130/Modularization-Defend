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
 * 目标计数节点——输出范围内检测到的目标数量。
 *
 * <p>这是一个"源"节点：只有输出端口，没有输入端口。
 * 通过"range"选项控制检测半径。</p>
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输出 count (int)</b> — 范围内的目标数量</li>
 * </ul>
 *
 * <h3>选项</h3>
 * <ul>
 *   <li><b>range (float)</b> — 检测半径，默认 10.0</li>
 * </ul>
 */
@NodeAttribute(name = "目标计数", group = "检测", graphTypes = {TurretLogicGraph.class})
public class TargetCountNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.target_count");
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("range", Float.class)
                .withDisplayName(Component.translatable("node.modularization_defend.range"))
                .withDefaultValue(10.0f);
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addOutputPort("count", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.target_count_port"))
                .build();
    }
}
