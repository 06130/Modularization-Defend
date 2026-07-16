package org.lingZero.modularization_defend.nodegraph.conv;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.Capabilities;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.NodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

/**
 * String→X 转换节点 —— 将 String 输入转换为指定目标类型。
 */
@NodeAttribute(name = "String→目标", group = "类型转换", graphTypes = {TurretLogicGraph.class})
public class StringToNode extends BaseConverterNode {

    public StringToNode() { super(String.class, "String"); }

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.conv.string_to");
    }
}
