package org.lingZero.modularization_defend.nodegraph.node.conv;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

/**
 * Bool→X 转换节点 —— 将 Boolean 输入转换为指定目标类型。
 */
@NodeAttribute(name = "Bool→目标", group = "类型转换", graphTypes = {TurretLogicGraph.class})
public class BoolToNode extends BaseConverterNode {

    public BoolToNode() { super(Boolean.class, "Bool"); }

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.conv.bool_to");
    }
}
