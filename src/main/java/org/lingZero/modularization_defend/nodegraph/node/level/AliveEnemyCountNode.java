package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

/**
 * 存活敌人数查询节点——输出本关生成且仍存活的敌人数量。
 *
 * <h3>端口</h3>
 * <ul><li><b>输出 count (int)</b> — 存活敌人数（外部上下文 "level:enemies"）</li></ul>
 */
@NodeAttribute(name = "存活敌人数", group = "关卡/查询", graphTypes = {TurretLogicGraph.class})
public class AliveEnemyCountNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.alive_enemy_count");
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addOutputPort("count", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.enemy_count"))
                .build();
    }
}
