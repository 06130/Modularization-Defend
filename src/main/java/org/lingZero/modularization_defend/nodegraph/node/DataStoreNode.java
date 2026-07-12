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
 * 数据存储节点——存储最近一次输入的数值并持续输出，直到新数据到来。
 *
 * <p>行为类似锁存器：首次收到输入后，输出保持该值不变，即使输入断开也继续输出。
 * 新输入到达时更新存储值。支持可选默认初始值。</p>
 *
 * <h3>选项</h3>
 * <ul><li><b>initialValue (float)</b> — 初始输出值，默认 0</li></ul>
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输入 value (float)</b> — 待存储的数据</li>
 *   <li><b>输入 write (bool)</b> — 高电平时写入当前值（可选，不连接时只要有输入就写入）</li>
 *   <li><b>输出 stored (float)</b> — 当前存储的值</li>
 * </ul>
 */
@NodeAttribute(name = "数据存储", group = "数据", graphTypes = {TurretLogicGraph.class})
public class DataStoreNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.data_store");
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("initialValue", Float.class)
                .withDisplayName(Component.translatable("node.modularization_defend.initial_value"))
                .withDefaultValue(0f);
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addInputPort("value", Float.class)
                .withDisplayName(Component.translatable("node.modularization_defend.input_value"))
                .build();
        context.addInputPort("write", Boolean.class)
                .withDisplayName(Component.translatable("node.modularization_defend.write_enable"))
                .build();
        context.addOutputPort("stored", Float.class)
                .withDisplayName(Component.translatable("node.modularization_defend.stored_value"))
                .build();
    }
}
