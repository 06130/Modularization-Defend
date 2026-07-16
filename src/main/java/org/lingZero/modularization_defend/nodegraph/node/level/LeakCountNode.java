package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

/**
 * 漏怪数查询节点——输出本关进入蓝门（漏过防线）的敌人累计数量。
 *
 * <h3>端口</h3>
 * <ul><li><b>输出 count (int)</b> — 累计漏怪数（外部上下文 "level:leaks"）</li></ul>
 */
@NodeAttribute(name = "漏怪数", group = "关卡/查询", graphTypes = {TurretLogicGraph.class})
public class LeakCountNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.leak_count");
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addOutputPort("count", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.leak_count_port"))
                .build();
    }
}
