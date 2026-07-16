package org.lingZero.modularization_defend.nodegraph.node.conv;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.Capabilities;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.NodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IOptionDefinitionContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.node.ConversionTarget;

/**
 * 类型转换节点基类——固定输入类型，通过选项选择输出类型。
 *
 * <p>子类在构造函数中指定输入类型和注册名后缀，LDLib2 自动发现。
 * 由于每个子类有独立的 @NodeAttribute，输入类型在注册时即确定，
 * 不会发生运行时端口类型切换导致的 UI 崩溃。</p>
 */
public abstract class BaseConverterNode extends Node {

    private final Class<?> inputType;
    private final String kind;

    protected BaseConverterNode(Class<?> inputType, String kind) {
        this.inputType = inputType;
        this.kind = kind;
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("target", TurretLogicGraph.CONVERSION_TARGET_TYPE)
                .withDisplayName(Component.translatable("node.modularization_defend.conv_target"))
                .withDefaultValue(ConversionTarget.INT);
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);

        Class<?> outputType = Float.class;
        var opt = getNodeOptionById("target");
        if (opt != null) {
            var result = opt.tryGetValue(ConversionTarget.class);
            if (result.result().isPresent() && result.result().get() instanceof ConversionTarget ct) {
                outputType = ct.getJavaType();
            }
        }

        context.addInputPort("value", inputType)
                .withDisplayName(Component.translatable("node.modularization_defend.input_value"))
                .build();
        context.addOutputPort("result", outputType)
                .withDisplayName(Component.translatable("node.modularization_defend.result"))
                .build();
    }
}
