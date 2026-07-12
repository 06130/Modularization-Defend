package org.lingZero.modularization_defend.nodegraph.node;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.Capabilities;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.NodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IOptionDefinitionContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

import java.util.Optional;

/**
 * 计时器节点——收到高电平输入后开始计时，持续输出经过的秒数。
 *
 * <h3>选项</h3>
 * <ul><li><b>autoReset (bool)</b> — 输入变为低电平时是否归零，默认开启</li></ul>
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输入 trigger (bool)</b> — 高电平时计时</li>
 *   <li><b>输入 reset (bool)</b> — 高电平时强制归零（优先级高于计时）</li>
 *   <li><b>输出 time (float)</b> — 当前计时秒数</li>
 * </ul>
 */
@NodeAttribute(name = "计时器", group = "计时", graphTypes = {TurretLogicGraph.class})
public class TimerNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.timer");
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("autoReset", Boolean.class)
                .withDisplayName(Component.translatable("node.modularization_defend.timer_auto_reset"));
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addInputPort("trigger", Boolean.class)
                .withDisplayName(Component.translatable("node.modularization_defend.trigger"))
                .build();
        context.addInputPort("reset", Boolean.class)
                .withDisplayName(Component.translatable("node.modularization_defend.timer_reset"))
                .build();
        context.addOutputPort("time", Float.class)
                .withDisplayName(Component.translatable("node.modularization_defend.elapsed_time"))
                .build();
    }
}
