package org.lingZero.modularization_defend.nodegraph.node.conv;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

/**
 * Float→X 转换节点 —— 将 Float 输入转换为指定目标类型。
 */
@NodeAttribute(name = "Float→目标", group = "类型转换", graphTypes = {TurretLogicGraph.class})
public class FloatToNode extends BaseConverterNode {

    public FloatToNode() { super(Float.class, "Float"); }

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.conv.float_to");
    }
}
