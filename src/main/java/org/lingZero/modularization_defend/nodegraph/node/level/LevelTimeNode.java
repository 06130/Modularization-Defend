package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

/**
 * 关卡时间查询节点——输出关卡启动以来经过的秒数。
 *
 * <h3>端口</h3>
 * <ul><li><b>输出 time (float)</b> — 关卡运行秒数（外部上下文 "level:time"）</li></ul>
 */
@NodeAttribute(name = "关卡时间", group = "关卡/查询", graphTypes = {TurretLogicGraph.class})
public class LevelTimeNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.level_time");
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addOutputPort("time", Float.class)
                .withDisplayName(Component.translatable("node.modularization_defend.elapsed_time"))
                .build();
    }
}
