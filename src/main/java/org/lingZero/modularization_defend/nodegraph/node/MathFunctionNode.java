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
 * 函数计算节点——通过下拉选择数学函数，根据所选函数动态生成输入端口并输出结果。
 *
 * <p>选项使用 {@link MathFunction} 枚举类型，LDLib2 自动渲染为下拉选择器。</p>
 */
@NodeAttribute(name = "函数计算", group = "运算", graphTypes = {TurretLogicGraph.class})
public class MathFunctionNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.math_function");
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("function", TurretLogicGraph.MATH_FUNCTION_TYPE)
                .withDisplayName(Component.translatable("node.modularization_defend.math_func_type"))
                .withDefaultValue(MathFunction.ADD);
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);

        Optional.ofNullable(getNodeOptionById("function")).ifPresent(o ->
            o.tryGetValue(MathFunction.class).ifSuccess(func -> {
                MathFunction fn = func instanceof MathFunction f ? f : MathFunction.ADD;

                context.addInputPort("a", Float.class)
                        .withDisplayName(Component.literal("A"))
                        .build();
                if (fn.isDualInput()) {
                    context.addInputPort("b", Float.class)
                            .withDisplayName(Component.literal("B"))
                            .build();
                }
            })
        );
        context.addOutputPort("result", Float.class)
                .withDisplayName(Component.translatable("node.modularization_defend.result"))
                .build();
    }
}
