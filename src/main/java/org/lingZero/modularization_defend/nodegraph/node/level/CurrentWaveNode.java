package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

/**
 * 当前波次查询节点——输出已生成的波次数（从 0 开始，每次"生成波次"动作 +1）。
 *
 * <h3>端口</h3>
 * <ul><li><b>输出 wave (int)</b> — 当前波次（外部上下文 "level:wave"）</li></ul>
 */
@NodeAttribute(name = "当前波次", group = "关卡/查询", graphTypes = {TurretLogicGraph.class})
public class CurrentWaveNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.current_wave");
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addOutputPort("wave", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.wave_number"))
                .build();
    }
}
