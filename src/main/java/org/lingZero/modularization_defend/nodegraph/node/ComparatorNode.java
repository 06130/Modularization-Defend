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
 * 比较器节点——根据选择的比较函数对两个输入值进行比较，输出布尔结果。
 *
 * <p>选项使用 {@link CompareFunction} 枚举类型，LDLib2 自动渲染为下拉选择器。</p>
 */
@NodeAttribute(name = "比较器", group = "判断", graphTypes = {TurretLogicGraph.class})
public class ComparatorNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.comparator");
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("function", TurretLogicGraph.COMPARE_FUNCTION_TYPE)
                .withDisplayName(Component.translatable("node.modularization_defend.math_func_type"))
                .withDefaultValue(CompareFunction.GREATER_THAN);
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        context.addInputPort("a", Float.class)
                .withDisplayName(Component.literal("A"))
                .build();
        context.addInputPort("b", Float.class)
                .withDisplayName(Component.literal("B"))
                .build();
        context.addOutputPort("result", Boolean.class)
                .withDisplayName(Component.translatable("node.modularization_defend.result"))
                .build();
    }
}
